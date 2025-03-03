package org.unitedlands.unitedUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.PortalCreateEvent;

import java.util.*;

public class PortalManager implements Listener {

    private final List<String> worldsBlacklist;
    private final String portalDenyMessage;
    private final Map<String, Map<String, String>> worldMappings;
    private final Location corner1;
    private final Location corner2;
    private final String warpCommand;
    private final HashMap<UUID, Boolean> playerInZone = new HashMap<>();

    public PortalManager(FileConfiguration config) {
        // Load configuration values.
        this.worldsBlacklist = config.getStringList("nether-portals.blacklisted-worlds");
        this.portalDenyMessage = config.getString("messages.nether-portal-deny");
        this.worldMappings = loadWorldMappings(Objects.requireNonNull(config.getConfigurationSection("portal-mapping")));
        this.warpCommand = config.getString("spawn-portal.command");
        corner1 = new Location(
                Bukkit.getWorld(Objects.requireNonNull(config.getString("spawn-portal.world"))),
                config.getDouble("spawn-portal.corner1.x"),
                config.getDouble("spawn-portal.corner1.y"),
                config.getDouble("spawn-portal.corner1.z")
        );
        corner2 = new Location(
                Bukkit.getWorld(Objects.requireNonNull(config.getString("spawn-portal.world"))),
                config.getDouble("spawn-portal.corner2.x"),
                config.getDouble("spawn-portal.corner2.y"),
                config.getDouble("spawn-portal.corner2.z")
        );
    }

    // Process the portal mapping configuration.
    private Map<String, Map<String, String>> loadWorldMappings(ConfigurationSection section) {
        Map<String, Map<String, String>> mappings = new HashMap<>();

        for (String fromWorld : section.getKeys(false)) {
            ConfigurationSection innerSection = section.getConfigurationSection(fromWorld);
            if (innerSection != null) {
                Map<String, String> destinations = new HashMap<>();
                for (String key : innerSection.getKeys(false)) {
                    destinations.put(key, innerSection.getString(key));
                }
                mappings.put(fromWorld, destinations);
            }
        }
        return mappings;
    }

    // Check the player is within the spawn portal defined region.
    private boolean isWithinZone(Location location) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        int x1 = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int y1 = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int z1 = Math.min(corner1.getBlockZ(), corner2.getBlockZ());

        int x2 = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int y2 = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int z2 = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

        return x >= x1 && x <= x2 &&
                y >= y1 && y <= y2 &&
                z >= z1 && z <= z2;
    }

    @EventHandler
    // Check if the world is blacklisted or unmapped and then cancel the event.
    public void onPortalCreate(PortalCreateEvent event) {
        World fromWorld = event.getWorld();
        if (worldsBlacklist.contains(fromWorld.getName()) || !worldMappings.containsKey(fromWorld.getName())) {
            org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getEntity();
            event.setCancelled(true);
            Objects.requireNonNull(player).sendMessage(portalDenyMessage);
        }
    }

    @EventHandler
    // Handles portal redirects.
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        World fromWorld = player.getWorld();

        // Check if the source world has a valid mapping.
        if (worldMappings.containsKey(fromWorld.getName())) {
            Map<String, String> destinations = worldMappings.get(fromWorld.getName());
            String targetWorldName = switch (event.getCause()) {
                // Determine if the portal is Nether or End based.
                case NETHER_PORTAL -> destinations.get("nether");
                case END_PORTAL -> destinations.get("end");
                default -> null;

            };

            if (targetWorldName != null) {
                World targetWorld = Bukkit.getWorld(targetWorldName);
                if (targetWorld != null) {
                    Location targetLocation = targetWorld.getSpawnLocation();

                    // Redirect the player
                    event.setCancelled(true);
                    player.teleport(targetLocation);
                }
            }
        }
    }

    @EventHandler
    // Check when a player crosses block boundaries.
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();
        if (from.getBlockX() == to.getBlockX() &&
                from.getBlockY() == to.getBlockY() &&
                from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        // Check if the player is within the portal.
        boolean isInZone = isWithinZone(to);
        UUID playerId = player.getUniqueId();

        // Only run the command when the player enters the portal.
        if (isInZone && !playerInZone.getOrDefault(playerId, false)) {
            String finalCommand = warpCommand.replace("{player}", player.getName());
            player.performCommand(finalCommand);
            playerInZone.put(playerId, true);
        } else if (!isInZone) {
            // Player has left the portal, reset their status.
            playerInZone.put(playerId, false);
        }
    }

    // This is a fix for TownyFlight sometimes persisting incorrectly over world change.
    // Code adapted from TheNylox.
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("group.admin")) {
            player.performCommand("townyflight:tfly");
        }
    }
}

