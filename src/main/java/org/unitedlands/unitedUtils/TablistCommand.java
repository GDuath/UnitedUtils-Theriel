package org.unitedlands.unitedUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.MetaNode;

public class TablistCommand implements CommandExecutor, TabCompleter {

    private final UnitedUtils plugin;

    public TablistCommand(UnitedUtils plugin) {
        this.plugin = plugin;
    }

    private final List<String> commandList = Arrays.asList("towntag", "nationtag");
    private final List<String> toggleList = Arrays.asList("on", "off");

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {

        List<String> options = null;
        String input = args[args.length - 1];

        switch (args.length) {
            case 1:
                options = commandList;
                break;
            case 2:
                options = toggleList;
                break;
        }

        List<String> completions = null;
        if (options != null) {
            completions = options.stream().filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                    .collect(Collectors.toList());
            Collections.sort(completions);
        }
        return completions;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            String[] args) {

        var player = (Player) sender;

        if (args.length != 2) {
            plugin.getLogger().info("Not enough arguments");
            return false;
        }

        togglePermission(player, args[0], args[1].equalsIgnoreCase("on"));

        return true;
    }

    private void togglePermission(Player player, String permission, Boolean toggle) {
        
        plugin.getLogger().info("Setting " + permission + " to " + toggle + "...");

        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());

        user.data().remove(MetaNode.builder("show_" + permission, Boolean.toString(!toggle)).build());
        user.data().remove(MetaNode.builder("show_" + permission, Boolean.toString(toggle)).build());
        user.data().add(MetaNode.builder("show_" + permission, Boolean.toString(toggle)).build());



        luckPerms.getUserManager().saveUser(user);

        plugin.getLogger().info("Done.");

    }

}