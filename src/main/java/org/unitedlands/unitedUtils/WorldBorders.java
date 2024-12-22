package org.unitedlands.unitedUtils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Set;

public class WorldBorders {

    private final JavaPlugin plugin;
    private final FileConfiguration config;

    public WorldBorders(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        initialiseBorders();
    }

    // Create world borders based on config settings. Undefined or misconfigured worlds use vanilla behaviour.
    private void initialiseBorders() {
        Set<String> worldsInConfig = Objects.requireNonNull(config.getConfigurationSection("worlds")).getKeys(false);

        for (World world : Bukkit.getWorlds()) {
            if (worldsInConfig.contains(world.getName())) {
                double centreX = config.getDouble("worlds." + world.getName() + ".x", 0.0);
                double centreZ = config.getDouble("worlds." + world.getName() + ".z", 0.0);
                double radiusX = config.getDouble("worlds." + world.getName() + ".radiusX", 0.0);
                double radiusZ = config.getDouble("worlds." + world.getName() + ".radiusZ", 0.0);

                if (radiusX > 0 && radiusZ > 0) {
                    WorldBorder border = world.getWorldBorder();
                    border.setCenter(centreX, centreZ);
                    border.setSize(Math.max(radiusX * 2, radiusZ * 2));

                    plugin.getLogger().info("Set border for world " + world.getName() + ": centreX=" + centreX + ", centreZ=" + centreZ + ", radiusX=" + radiusX + ", radiusZ=" + radiusZ);
                } else {
                    plugin.getLogger().warning("Invalid border configuration for world " + world.getName() + ". Skipping...");
                }
            } else {
                plugin.getLogger().info("No specific border configuration for world " + world.getName() + ". Using vanilla behavior.");
            }
        }
    }
}