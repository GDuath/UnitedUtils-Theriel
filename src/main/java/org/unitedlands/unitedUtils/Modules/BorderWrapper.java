package org.unitedlands.unitedUtils.Modules;

import org.bukkit.Location;
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
    private final int borderMaxX = 36800; // Eastern boundary.
    private final int borderMinX = -36800; // Western boundary.
    private final int borderMaxZ = -18400; // Northern boundary.
    private final int borderMinZ = 18400;  // Southern boundary.
    private final int buffer = 160; // Safe zone buffer (10 chunks).
    private final HashMap<UUID, Long> lastTeleportTime = new HashMap<>();
    private final HashMap<UUID, Location> lastTeleportLocation = new HashMap<>();

    public BorderWrapper(JavaPlugin plugin) {
        this.plugin = plugin;
        this.worldEarth = plugin.getServer().getWorld("world_earth");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();

        if (!to.getWorld().equals(worldEarth)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Suppress movement immediately after teleport.
        if (lastTeleportTime.containsKey(playerId) && currentTime - lastTeleportTime.get(playerId) < 500) {
            return; // Suppress movement events for 500ms after teleport.
        }

        // Suppress redundant movement near the last teleport location.
        Location lastLocation = lastTeleportLocation.get(playerId);
        if (lastLocation != null && lastLocation.distanceSquared(to) < buffer * buffer) {
            return;
        }

        // Check if player crosses a boundary.
        int x = to.getBlockX();
        int z = to.getBlockZ();

        if (x <= borderMinX || x >= borderMaxX || z >= borderMinZ || z <= borderMaxZ) {
            // Check if cooldown is up.
            if (lastTeleportTime.getOrDefault(playerId, 0L) + 5000 > currentTime) {
                return;
            }

            // Schedule teleport.
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location teleportLocation = calculateTeleportLocation(x, z, to);
                    player.teleport(teleportLocation);
                    lastTeleportLocation.put(playerId, teleportLocation);
                    lastTeleportTime.put(playerId, System.currentTimeMillis());
                }
            }.runTask(plugin);
        }
    }

    private Location calculateTeleportLocation(int x, int z, Location currentLocation) {
        float yaw = currentLocation.getYaw();
        Location teleportLocation = new Location(worldEarth, currentLocation.getX(), currentLocation.getY(), currentLocation.getZ(), yaw, currentLocation.getPitch());

        // Use a helper method to calculate the wrapped X-coordinate.
        int wrappedX = getWrappedX(x);

        if (x <= borderMinX) {
            // Wrap East to West.
            teleportLocation.setX(borderMaxX - buffer);
        } else if (x >= borderMaxX) {
            // Wrap West to East.
            teleportLocation.setX(borderMinX + buffer);
        } else if (z >= borderMinZ) {
            // Wrap South to South.
            teleportLocation.setX(wrappedX);
            teleportLocation.setZ(borderMinZ - buffer);
            teleportLocation.setYaw(180); // Face player North
        } else if (z <= borderMaxZ) {
            // Wrap North to North.
            teleportLocation.setX(wrappedX);
            teleportLocation.setZ(borderMaxZ + buffer);
            teleportLocation.setYaw(0); // Face player South
        }

        return teleportLocation;
    }

    private int getWrappedX(int x) {
        // Calculate total width of the world.
        int worldWidth = borderMaxX - borderMinX;
        return ((x - borderMinX + worldWidth / 2) % worldWidth) + borderMinX;
    }
}