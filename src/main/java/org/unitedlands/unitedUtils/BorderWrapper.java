package org.unitedlands.unitedUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class BorderWrapper implements Listener {

    private final JavaPlugin plugin;
    private final World worldEarth;
    private final int borderMaxX = 36800;
    private final int borderMinX = -36800;
    private final HashMap<UUID, Long> lastTeleportTime = new HashMap<>();
    private final HashMap<UUID, Boolean> isTeleporting = new HashMap<>();
    private final long teleportCooldown = 1000; // Cooldown in milliseconds

    public BorderWrapper(JavaPlugin plugin) {
        this.plugin = plugin;
        this.worldEarth = Bukkit.getWorld("world_earth");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    // Handle East-West border wrapping.
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();

        if (!to.getWorld().equals(worldEarth)) {
            return;
        }

        UUID playerId = player.getUniqueId();

        // Skip processing if the player is currently teleporting
        if (isTeleporting.getOrDefault(playerId, false)) {
            return;
        }

        // Only process if the player has moved to a different chunk
        if ((from.getBlockX() >> 4) == (to.getBlockX() >> 4) && (from.getBlockZ() >> 4) == (to.getBlockZ() >> 4)) {
            return;
        }

        int x = to.getBlockX();
        long currentTime = System.currentTimeMillis();

        // Prevent teleport loops with a cooldown.
        if (lastTeleportTime.containsKey(playerId) && (currentTime - lastTeleportTime.get(playerId)) < teleportCooldown) {
            return;
        }

        if (x <= borderMinX || x >= borderMaxX) {
            // Update the cooldown before teleporting to prevent loops.
            lastTeleportTime.put(playerId, currentTime);
            isTeleporting.put(playerId, true);

            // Schedule the teleport for better performance.
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        Location originalLocation;
                        if (x <= borderMinX) {
                            originalLocation = new Location(worldEarth, borderMaxX - 1, to.getY(), to.getZ(), to.getYaw(), to.getPitch());
                        } else {
                            originalLocation = new Location(worldEarth, borderMinX + 1, to.getY(), to.getZ(), to.getYaw(), to.getPitch());
                        }

                        // Find a safe location before teleporting.
                        Location safeLocation = findSafeLocation(originalLocation);
                        if (safeLocation != null) {
                            player.teleport(safeLocation);
                        }
                    } finally {
                        // Allow processing after teleport.
                        isTeleporting.put(playerId, false);
                    }
                }
            }.runTask(plugin);
        }
    }

    private Location findSafeLocation(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return null;
        }

        int x = location.getBlockX();
        int z = location.getBlockZ();
        // Get the highest safe Y level.
        int y = world.getHighestBlockYAt(x, z);

        // Ensure the location is on solid ground.
        Location safeLocation = new Location(world, x, y, z, location.getYaw(), location.getPitch());
        Material blockType = safeLocation.getBlock().getType();
        if (blockType.isSolid() && blockType != Material.LAVA) {
            return safeLocation;
        }
        return null;
    }
}