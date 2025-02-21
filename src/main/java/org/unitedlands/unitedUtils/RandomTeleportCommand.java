package org.unitedlands.unitedUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RandomTeleportCommand implements CommandExecutor {

    private final UnitedUtils plugin;

    private List<UUID> activeTeleports;
    private Map<UUID, Long> cooldownList;

    public RandomTeleportCommand(UnitedUtils plugin) {
        this.plugin = plugin;
        activeTeleports = new ArrayList<UUID>();
        cooldownList = new HashMap<UUID, Long>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            String[] args) {

        var player = (Player) sender;

        switch (args.length) {
            case 0:
                handleWorldZoneTeleport(player, null, null);
                break;
            case 1:
                handleWorldZoneTeleport(player, args[0], null);
                break;
            case 2:
                handleWorldZoneTeleport(player, args[0], args[1]);
                break;
            default:
                break;
        }

        return true;
    }

    private void handleWorldZoneTeleport(Player player, String worldName, String zoneName) {

        ConfigurationSection worldConfig = null;
        ConfigurationSection zoneConfig = null;

        // If no world name is provided, use the player's current world by default
        if (worldName == null) {
            worldName = player.getWorld().getName();
        }

        // See if the requested world exists
        final World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sendPlayerMessage(player, "error-no-world", worldName);
            return;
        }

        // See if there is a valid configuration for the requested world
        worldConfig = plugin.getConfig().getConfigurationSection("ul-rtp.worlds." + worldName);
        if (worldConfig == null) {
            sendPlayerMessage(player, "error-no-world", worldName);
            return;
        }

        // See if the world has any RTP zones defined
        ConfigurationSection zoneSection = worldConfig.getConfigurationSection("zones");
        if (zoneSection == null) {
            sendPlayerMessage(player, "error-no-zones", null);
            return;
        }

        // If no zone name is provided, use a random zone in the requested world.
        if (zoneName == null) {
            List<String> zoneNames = new ArrayList<String>(zoneSection.getKeys(false));
            if (zoneNames.size() > 1) {
                Random rnd = new Random();
                zoneName = zoneNames.get(rnd.nextInt(zoneNames.size()));
            } else if (zoneNames.size() == 1) {
                zoneName = zoneNames.get(0);
            } else {
                sendPlayerMessage(player, "error-no-zones", null);
                return;
            }
        }

        // See if the requested zone has a valid configuration
        zoneConfig = zoneSection.getConfigurationSection(zoneName);
        if (zoneConfig == null) {
            sendPlayerMessage(player, "error-no-zone", zoneName);
            return;
        }

        // Perform additional checks before starting teleportation
        var playerUuid = player.getUniqueId();

        // Teleportation already active?
        if (activeTeleports.contains(playerUuid)) {
            sendPlayerMessage(player, "warning-active-tp", zoneName);
            return;
        }

        // Player on cooldown?
        // NOTE: during times of high server load, cooldown reset might be triggered
        // significantly after the desired cooldown has elapsed. In that case, send a
        // different message to inform the player and prevent unnecessary bug reports.
        Double cooldown = plugin.getConfig().getDouble("ul-rtp.cooldown", 5) * 1000;
        if (cooldownList.containsKey(playerUuid)) {
            var remainingCooldown = cooldown - (System.currentTimeMillis() - cooldownList.get(playerUuid));
            if (remainingCooldown > 0)
                sendPlayerMessage(player, "warning-cooldown", (remainingCooldown / 1000) + " seconds remaining");
            else
                sendPlayerMessage(player, "warning-cooldown-elapsed",
                        (remainingCooldown / 1000) + " seconds over cooldown");
            return;
        }

        // If using economy, does the player have enough money?
        Boolean useEconomy = plugin.getConfig().getBoolean("ul-rtp.use-economy");
        Double rtpCost = worldConfig.getDouble("cost");
        if (useEconomy && rtpCost > 0) {
            Double playerBalance = plugin.getEconomy().getBalance(player);
            if (playerBalance < rtpCost) {
                sendPlayerMessage(player, "warning-nomoney", "costs " + plugin.getEconomy().format(rtpCost));
                return;
            }
        }

        // All checks passed, start random teleporting
        sendPlayerMessage(player, "tp-start", null);
        activeTeleports.add(player.getUniqueId());

        // Try to find a safe location asynchronously on another thread
        findSafeRandomLocation(player, world, zoneConfig, result -> {
            player.teleport(
                    new Location(world, result.getX() + 0.5,
                            result.getY() + 1,
                            result.getZ() + 0.5));

            activeTeleports.remove(playerUuid);
            cooldownList.put(playerUuid, System.currentTimeMillis());

            // Withdraw costs
            if (useEconomy && rtpCost > 0) {
                plugin.getEconomy().withdrawPlayer(player, rtpCost);
                sendPlayerMessage(player, "tp-costs", plugin.getEconomy().format(rtpCost));
            }

            // Schedule cooldown reset
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                cooldownList.remove(playerUuid);
            }, (Long) Math.round(cooldown / 50));

            // Done
            sendPlayerMessage(player, "tp-end", result.getX() + ", " + result.getY() + ", " + result.getZ());
        });

    }

    private void findSafeRandomLocation(Player player, World world, ConfigurationSection zoneConfig,
            Consumer<Block> callback) {

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            int attempt = 0;
            int maxAttempts = plugin.getConfig().getInt("ul-rtp.max-attempts", 1);

            Random random = new Random();

            var minX = zoneConfig.getInt("min-x", -500);
            var minZ = zoneConfig.getInt("min-z", 500);
            var maxX = zoneConfig.getInt("max-x", -500);
            var maxZ = zoneConfig.getInt("max-z", 500);

            Block potentialSafeBlock = null;
            Boolean blockFound = false;

            // Continue searching until you find a safe block, or stop after a certain
            // number of attemps.
            // NOTE: loop iteration length can vary greatly from milliseconds to seconds
            // depending on server load and whether or not the target chunk has previously
            // been generated. DO NOT SET max-attempts too high in the config!
            while (attempt < maxAttempts && !blockFound) {
                attempt++;

                int rndX = random.nextInt((maxX - minX) + 1) + minX;
                int rndZ = random.nextInt((maxZ - minZ) + 1) + minZ;

                potentialSafeBlock = world.getHighestBlockAt(rndX, rndZ);

                // Special nether handling. Start from the nether roof and go down until
                // you find a solid block with 2 blocks of air above.
                if (world.getEnvironment() == Environment.NETHER) {
                    for (int i = potentialSafeBlock.getY(); i >= 2; i--) {
                        var block1 = world.getBlockAt(rndX, i, rndZ);
                        var block2 = world.getBlockAt(rndX, i - 1, rndZ);
                        var block3 = world.getBlockAt(rndX, i - 2, rndZ);
                        if (block1.getType() == Material.AIR && block2.getType() == Material.AIR && block3.isSolid()) {
                            potentialSafeBlock = world.getBlockAt(rndX, i - 2, rndZ);
                            blockFound = true;
                            break;
                        }
                    }
                } else {
                    // Default world behaviours. Don't rtp into water or onto other unstable blocks.
                    if (potentialSafeBlock.isSolid()) {
                        blockFound = true;
                        break;
                    }
                }
            }

            // Sad. Better luck next time.
            if (!blockFound) {
                sendPlayerMessage(player, "error-no-block", null);
                return;
            }

            final Block highestSafeBlock = potentialSafeBlock;
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(highestSafeBlock));
        });
    }

    private void sendPlayerMessage(Player player, String messageId, String info) {
        String prefix = plugin.getConfig().getConfigurationSection("messages.rtp").getString("prefix");
        String message = plugin.getConfig().getConfigurationSection("messages.rtp").getString(messageId);
        player.sendMessage(prefix + message + (info != null ? " (" + info + ")" : ""));
    }

}