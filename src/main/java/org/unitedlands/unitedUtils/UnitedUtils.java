package org.unitedlands.unitedUtils;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Objects;

public final class UnitedUtils extends JavaPlugin {

    public void unregisterListeners() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public void onEnable() {
        // Save default config if not already present.
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        // Register the unitedutils command.
        Objects.requireNonNull(getCommand("unitedutils")).setExecutor(new Commands(this));
        getServer().getPluginManager().registerEvents(new ExplosionManager(config), this);
        getLogger().info("UnitedUtils has been enabled!");
        // Plugin startup logic.

    }

    public void reloadPluginConfig() {
        // Reapply config on reload.
        reloadConfig();
        FileConfiguration config = getConfig();
        unregisterListeners();
        Bukkit.getPluginManager().registerEvents(new ExplosionManager(config), this);
        getLogger().info("Plugin configuration reloaded.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic.
    }
}
