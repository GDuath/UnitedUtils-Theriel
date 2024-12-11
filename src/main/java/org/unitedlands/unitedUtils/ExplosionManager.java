package org.unitedlands.unitedUtils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ExplosionManager implements Listener {
    private final FileConfiguration config;
    public ExplosionManager(FileConfiguration config) {
        this.config = config;
    }

    // End Crystals
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Check if the damage is caused by an end crystal
        if (event.getDamager().getType() == org.bukkit.entity.EntityType.END_CRYSTAL) {
            // Check if the entity being damaged is a player
            if (event.getEntity() instanceof org.bukkit.entity.Player) {
                // Check if cancelling is enabled in the config
                if (config.getBoolean("nerf-crystal-damage", true)) {
                    // Cancel the damage event
                    event.setCancelled(true);
                }
            }
        }
    }
}
