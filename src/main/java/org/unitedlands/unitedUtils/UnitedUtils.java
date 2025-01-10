package org.unitedlands.unitedUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.HandlerList;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Objects;

public final class UnitedUtils extends JavaPlugin {

    public void unregisterListeners() {
        HandlerList.unregisterAll(this);
    }

    // Helper method to register all commands with an executor and tab completer.
    private void registerCommand(String name, Commands executor, TabCompleter completer) {
        Objects.requireNonNull(getCommand(name), "Command " + name + " is not defined in plugin.yml.")
                .setExecutor(executor);
        Objects.requireNonNull(getCommand(name)).setTabCompleter(completer);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic.
        // Save default config if not already present.
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        // Register the unitedutils command.
        Commands commandHandler = new Commands(this);
        registerCommand("unitedutils", commandHandler, commandHandler);
        registerCommand("remskill", commandHandler, commandHandler);
        registerCommand("whoarewe", commandHandler, commandHandler);
        registerCommand("map", commandHandler, commandHandler);
        registerCommand("discord", commandHandler, commandHandler);
        registerCommand("wiki", commandHandler, commandHandler);
        registerCommand("shop", commandHandler, commandHandler);
        registerCommand("greylist", commandHandler, commandHandler);
        registerCommand("toptime", commandHandler, commandHandler);
        getServer().getPluginManager().registerEvents(new ExplosionManager(config), this);
        getServer().getPluginManager().registerEvents(new VoidProtection(config), this);
        getServer().getPluginManager().registerEvents(new PortalManager(config), this);
        getServer().getPluginManager().registerEvents(new WikiMapLink(), this);
        WikiMapLink wikiMapLink = new WikiMapLink();
        getServer().getPluginManager().registerEvents(wikiMapLink, this);
        wikiMapLink.registerStrippedNationStatus();
        new BorderWrapper(this);
        getLogger().info("UnitedUtils has been enabled!");

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
