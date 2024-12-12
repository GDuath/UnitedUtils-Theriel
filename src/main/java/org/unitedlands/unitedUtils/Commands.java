package org.unitedlands.unitedUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Commands implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;

    public Commands(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Reload command.
        FileConfiguration config = plugin.getConfig();
        if (!sender.hasPermission("united.utils.admin")) {
            sender.sendMessage(Objects.requireNonNull(config.getString("messages.no-permission")));
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            ((UnitedUtils) plugin).reloadPluginConfig();
            sender.sendMessage(Objects.requireNonNull(config.getString("messages.reload")));
        } else {
            sender.sendMessage(Objects.requireNonNull(config.getString("messages.invalid-command")));
        }
        return true;
    }
    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("united.utils.admin")) {
            // Return no suggestions if no permission.
            return Collections.emptyList();
        }
        if (args.length == 1) {
            // Provide suggestions for the first argument.
            List<String> suggestions = new ArrayList<>();
            if ("reload".startsWith(args[0].toLowerCase())) {
                suggestions.add("reload");
            }
            return suggestions;
        }
        return Collections.emptyList();
    }
}

