package org.unitedlands.unitedUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Commands implements CommandExecutor {

    private final JavaPlugin plugin;

    public Commands(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        FileConfiguration config = plugin.getConfig();
        if (!sender.hasPermission("united.utils.admin")) {
            sender.sendMessage(Objects.requireNonNull(config.getString("messages.no-permission")));
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            ((UnitedUtils) plugin).reloadPluginConfig();
            // Update listeners or handlers that rely on config.
            plugin.getServer().getPluginManager().registerEvents(new ExplosionManager(config), plugin);
            sender.sendMessage(Objects.requireNonNull(config.getString("messages.reload")));
        } else {
            sender.sendMessage(Objects.requireNonNull(config.getString("messages.invalid-command")));
        }
        return true;
    }
}
