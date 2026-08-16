package org.unitedlands.unitedUtils.Modules;

import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.unitedlands.unitedUtils.UnitedUtils;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class HeadDrops implements Listener {

    private final UnitedUtils plugin;
    private HeadDatabaseAPI hdbApi;
    private boolean hdbAvailable = false;

    private boolean enabled;
    private String playerPerm;
    private double playerChance;

    private double lootingBonus;
    private boolean preventSpawnerDrops;

    private final Map<EntityType, MobHeadInfo> activeMobs = new EnumMap<>(EntityType.class);

    public record MobHeadInfo(String hdbId, double chance, String permission) {
    }

    public HeadDrops(UnitedUtils plugin) {
        this.plugin = plugin;
        loadConfig(plugin.getConfig());
        checkHdbHook();
    }

    private void checkHdbHook() {
        if (Bukkit.getPluginManager().isPluginEnabled("HeadDatabase")) {
            try {
                this.hdbApi = new HeadDatabaseAPI();
                this.hdbAvailable = true;
            } catch (Exception e) {
                plugin.getLogger().warning("[HeadDrops] Failed to initialize HeadDatabase API: " + e.getMessage());
            }
        }
    }

    public void loadConfig(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("head-drops");
        if (section == null) {
            this.enabled = false;
            return;
        }

        this.enabled = section.getBoolean("enabled");
        this.playerPerm = section.getString("permissions.player");
        this.playerChance = section.getDouble("player.chance");
        this.lootingBonus = section.getDouble("looting-bonus");
        this.preventSpawnerDrops = section.getBoolean("prevent-spawner-drops");
        activeMobs.clear();

        loadMobSection(section.getConfigurationSection("hostile.mobs"),
                section.getDouble("hostile.chance"),
                section.getString("permissions.hostile"));

        loadMobSection(section.getConfigurationSection("passive.mobs"),
                section.getDouble("passive.chance"),
                section.getString("permissions.passive"));
    }

    private void loadMobSection(ConfigurationSection section, double chance, String permission) {
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                EntityType type = EntityType.valueOf(key.toUpperCase());
                String hdbId = section.getString(key + ".hdb-id");

                if (hdbId != null && !hdbId.isBlank()) {
                    activeMobs.put(type, new MobHeadInfo(hdbId, chance, permission));
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("[HeadDrops] Unknown EntityType in config: " + key);
            }
        }
    }

    @EventHandler
    public void onDatabaseLoad(DatabaseLoadEvent event) {
        this.hdbApi = new HeadDatabaseAPI();
        this.hdbAvailable = true;
        plugin.getLogger().info("[HeadDrops] Hooked into HeadDatabase API successfully.");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!enabled) return;

        LivingEntity victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) return;

        // Player Kills.
        if (victim instanceof Player victimPlayer) {
            handlePlayerHeadDrop(killer, victimPlayer, event);
            return;
        }

        // Prevent spawner farming.
        if (preventSpawnerDrops && victim.getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return;
        }

        // Mob Kills.
        MobHeadInfo info = activeMobs.get(victim.getType());

        if (info != null && info.permission() != null && killer.hasPermission(info.permission())) {
            rollAndDrop(killer, info, event);
        }
    }

    private void handlePlayerHeadDrop(Player killer, Player victim, EntityDeathEvent event) {
        if (playerPerm == null || !killer.hasPermission(playerPerm)) return;

        double finalChance = calculateFinalChance(killer, playerChance);

        double roll = ThreadLocalRandom.current().nextDouble(0.0, 100.0);
        if (roll <= finalChance) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(victim);
                meta.displayName(Component.text(victim.getName() + "'s Head", NamedTextColor.YELLOW));
                head.setItemMeta(meta);
            }

            victim.getWorld().dropItemNaturally(victim.getLocation(), head);
        }
    }

    private void rollAndDrop(Player killer, MobHeadInfo info, EntityDeathEvent event) {

        double finalChance = calculateFinalChance(killer, info.chance());

        double roll = ThreadLocalRandom.current().nextDouble(0.0, 100.0);
        if (roll > finalChance) return;

        if (hdbAvailable && hdbApi != null) {
            try {
                ItemStack headItem = hdbApi.getItemHead(info.hdbId());
                if (headItem != null) {
                    event.getDrops().add(headItem);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("[HeadDrops] Invalid head ID, dropping nothing.");
            }
        }
    }

    private double calculateFinalChance(Player killer, double baseChance) {
        int lootingLevel = killer.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOTING);
        return baseChance + (lootingLevel * lootingBonus);
    }
}