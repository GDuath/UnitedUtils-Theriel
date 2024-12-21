package org.unitedlands.unitedUtils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import java.util.List;

public class VoidProtection implements Listener {

    private final List<String> worldsWhitelist;
    private final double voidThreshold;
    private final String voidMessage;

    public VoidProtection(FileConfiguration config) {
        // Load configuration values.
        this.worldsWhitelist = config.getStringList("void-protections.whitelisted-worlds");
        this.voidThreshold = config.getDouble("void-protections.void-threshold");
        this.voidMessage = config.getString("messages.void-protect");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        World playerWorld = player.getWorld();

        // Check if the world is in the whitelist
        if (!worldsWhitelist.contains(playerWorld.getName())) {
            return;
        }
        // Check if y coordinate changed.
        if (event.getFrom().getY() == event.getTo().getY()) {
            return;
        }
        // Check if the player is below the void threshold.
        if (player.getLocation().getY() < voidThreshold) {
            Location spawnLocation = playerWorld.getSpawnLocation();
            player.teleport(spawnLocation);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            playerWorld.spawnParticle(Particle.PORTAL, player.getLocation(), 50, 0.5, 0.5, 0.5, 0.1);
            player.sendMessage(voidMessage);
        }
    }
}
