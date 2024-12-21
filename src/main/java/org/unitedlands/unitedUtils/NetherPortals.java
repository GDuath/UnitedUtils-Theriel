package org.unitedlands.unitedUtils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;
import java.util.List;
import java.util.Objects;

public class NetherPortals implements Listener {

    private final List<String> worldsBlacklist;
    private final String portalMessage;

    public NetherPortals(FileConfiguration config) {
        // Load configuration values.
        this.worldsBlacklist = config.getStringList("nether-portals.blacklisted-worlds");
        this.portalMessage = config.getString("messages.portal-message");
    }

    @EventHandler
    // Check if the world is blacklisted and then cancel the event.
    public void onPortalCreate(PortalCreateEvent event) {
        if (worldsBlacklist.contains(event.getWorld().getName())) {
            org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getEntity();
            event.setCancelled(true);
            Objects.requireNonNull(player).sendMessage(portalMessage);
        }
    }
}

