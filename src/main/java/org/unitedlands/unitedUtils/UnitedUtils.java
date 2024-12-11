package org.unitedlands.unitedUtils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

public final class UnitedUtils extends JavaPlugin {

    @Override
    public void onEnable() {
        // Save default config if not already present.
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        Bukkit.getPluginManager().registerEvents(new ExplosionManager(config), this);
        getServer().getPluginManager().registerEvents(new ExplosionManager(config), this);
        getLogger().info("UnitedUtils has been enabled!");
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic.
    }
}
