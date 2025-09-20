package org.unitedlands.unitedUtils.Commands;

import java.util.ArrayList;
import java.util.List;


import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.unitedUtils.UnitedUtils;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI.CommandType;
import com.palmergames.bukkit.towny.object.AddonCommand;

public class TownyNationCommandExtensions implements CommandExecutor, TabCompleter {

    @SuppressWarnings("unused")
    private final UnitedUtils plugin;

    public TownyNationCommandExtensions(UnitedUtils plugin) {
        this.plugin = plugin;
        TownyCommandAddonAPI.addSubCommand(new AddonCommand(CommandType.NATION_SET, "mapcolorhex", this));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
            @NotNull String lbl, @NotNull String @NotNull [] args) {

        return new ArrayList<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String lbl,
            @NotNull String @NotNull [] args) {

        if (args.length != 1)
        {
            sender.sendMessage("Usage: /n set mapcolorhex <#RRGGBB>");
            return false;
        }

        var colorString = args[0];
        if (!colorString.matches("^#(?:[0-9a-fA-F]{3}){1,2}$"))
        {
            sender.sendMessage("§cThe color was not in a valid hex format.");
            return false;
        }

        var player = (Player) sender;
        var resident = TownyAPI.getInstance().getResident(player);
        if  (resident == null)
            return false;

        var nation = resident.getNationOrNull();
        if (nation == null)
            return false;

        if (!player.hasPermission("towny.command.nation.set.mapcolor"))
        {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return false;
        }

        nation.setMapColorHexCode(colorString.replaceAll("#", ""));
        nation.save();

        sender.sendMessage("§aMap color saved. This will take effect on the map in the next 5 minutes.");

        return true;
    }

}
