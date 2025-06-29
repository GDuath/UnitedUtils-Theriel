package org.unitedlands.unitedUtils.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.unitedUtils.UnitedUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Commands implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;

    public Commands(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // Generic handler for simple message commands.
    private void handleMessageCommand(CommandSender sender, String messageKey) {
        if (!sender.hasPermission("united.utils.player")) {
            sender.sendMessage(Objects.requireNonNull(plugin.getConfig().getString("messages.no-permission")));
            return;
        }
        List<String> messages = plugin.getConfig().getStringList(messageKey);
        for (String line : messages) {
            sender.sendMessage(line);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        FileConfiguration config = plugin.getConfig();

        // Reload command.
        if (label.equalsIgnoreCase("unitedutils") && args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("united.utils.admin")) {
                sender.sendMessage(Objects.requireNonNull(config.getString("messages.no-permission")));
                return true;
            }
            plugin.reloadConfig();
            ((UnitedUtils) plugin).reloadPluginConfig();
            sender.sendMessage(Objects.requireNonNull(config.getString("messages.reload")));
            return true;
        }

        // Map command.
        if (command.getName().equalsIgnoreCase("map")) {
            handleMessageCommand(sender,"messages.map");
            return true;
        }

        // Discord command.
        if (command.getName().equalsIgnoreCase("discord")) {
            handleMessageCommand(sender,"messages.discord");
            return true;
        }

        // Wiki command.
        if (command.getName().equalsIgnoreCase("wiki")) {
            handleMessageCommand(sender,"messages.wiki");
            return true;
        }

        // Shop command.
        if (command.getName().equalsIgnoreCase("shop")) {
            handleMessageCommand(sender,"messages.shop");
            return true;
        }

        // Greylist command.
        if (command.getName().equalsIgnoreCase("greylist")) {
            handleMessageCommand(sender,"messages.greylist");
            return true;
        }

        // Sidebar command.
        if (command.getName().equalsIgnoreCase("sb")) {
            return true;
        }

        // Who are we?
        if (label.equalsIgnoreCase("whoarewe")) {
            if (!sender.hasPermission("united.utils.admin")) {
                sender.sendMessage(Objects.requireNonNull(config.getString("messages.no-permission")));
                return true;
            }
            List<String> mapMessage = config.getStringList("messages.whoarewe");
            for (String line : mapMessage) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(line);
                }
            }
            return true;
        }

        // Remove skill command (console script for job skill menu).
        if (label.equalsIgnoreCase("remskill")) {
            if (!sender.hasPermission("united.utils.admin")) {
                sender.sendMessage(Objects.requireNonNull(config.getString("messages.no-permission")));
                return true;
            }
            if (args.length < 2)
                return true;

            String targetPlayer = args[0];
            String skill = args[1];

            for (int i = 1; i <= 3; i++) {
                String consoleCommand = String.format(
                        "lp user %s permission set united.skills.%s.%d false",
                        targetPlayer, skill, i
                );
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCommand);
            }
            return true;
        }

            // Default invalid command message.
        sender.sendMessage(Objects.requireNonNull(config.getString("messages.invalid-command")));
        return true;
    }

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

