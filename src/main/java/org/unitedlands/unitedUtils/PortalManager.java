package org.unitedlands.unitedUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.PortalCreateEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PortalManager implements Listener {

    private final List<String> worldsBlacklist;
    private final String portalDenyMessage;
    private final Map<String, Map<String, String>> worldMappings;

    public PortalManager(FileConfiguration config) {
        // Load configuration values.
        this.worldsBlacklist = config.getStringList("nether-portals.blacklisted-worlds");
        this.portalDenyMessage = config.getString("messages.nether-portal-deny");
        this.worldMappings = loadWorldMappings(Objects.requireNonNull(config.getConfigurationSection("portal-mapping")));
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
}

