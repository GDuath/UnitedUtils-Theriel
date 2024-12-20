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
        Commands commandHandler = new Commands(this);
        Objects.requireNonNull(getCommand("unitedutils")).setExecutor(commandHandler);
        Objects.requireNonNull(getCommand("unitedutils")).setTabCompleter(commandHandler);
        Objects.requireNonNull(getCommand( "map")).setExecutor(commandHandler);
        Objects.requireNonNull(getCommand("map")).setTabCompleter(commandHandler);
        Objects.requireNonNull(getCommand( "discord")).setExecutor(commandHandler);
        Objects.requireNonNull(getCommand("discord")).setTabCompleter(commandHandler);
        Objects.requireNonNull(getCommand( "wiki")).setExecutor(commandHandler);
        Objects.requireNonNull(getCommand("wiki")).setTabCompleter(commandHandler);
        Objects.requireNonNull(getCommand( "shop")).setExecutor(commandHandler);
        Objects.requireNonNull(getCommand("shop")).setTabCompleter(commandHandler);
        Objects.requireNonNull(getCommand( "greylist")).setExecutor(commandHandler);
        Objects.requireNonNull(getCommand("greylist")).setTabCompleter(commandHandler);
        Objects.requireNonNull(getCommand( "toptime")).setExecutor(commandHandler);
        Objects.requireNonNull(getCommand("toptime")).setTabCompleter(commandHandler);
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
