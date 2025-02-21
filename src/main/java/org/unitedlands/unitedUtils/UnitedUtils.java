package org.unitedlands.unitedUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.HandlerList;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.unitedUtils.Listeners.StatusScreenListener;

import net.milkbowl.vault.economy.Economy;

import java.util.Objects;

public final class UnitedUtils extends JavaPlugin {

    private Economy economy;

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
        getCommand("rtp").setExecutor(new RandomTeleportCommand(this));
        getServer().getPluginManager().registerEvents(new ExplosionManager(config), this);
        getServer().getPluginManager().registerEvents(new VoidProtection(config), this);
        getServer().getPluginManager().registerEvents(new PortalManager(config), this);
        getServer().getPluginManager().registerEvents(new WikiMapLink(), this);
        WikiMapLink wikiMapLink = new WikiMapLink();
        getServer().getPluginManager().registerEvents(wikiMapLink, this);
        wikiMapLink.registerStrippedNationStatus();
        new BorderWrapper(this);

        this.getServer().getPluginManager().registerEvents(new StatusScreenListener(this), this);

        loadEconomy();

        getLogger().info("UnitedUtils has been enabled!");
    }

    public void reloadPluginConfig() {
        // Reapply config on reload.
        reloadConfig();
        FileConfiguration config = getConfig();
        unregisterListeners();
        Bukkit.getPluginManager().registerEvents(new ExplosionManager(config), this);
        loadEconomy();
        getLogger().info("Plugin configuration reloaded.");
    }

    private void loadEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Vault could not be found.");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("Economy service provider could not be found.");
            return;
        }
        economy = rsp.getProvider();
    }

    public Economy getEconomy()
    {
        return economy;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic.
    }
}
