package org.unitedlands.unitedUtils.Listeners;

import com.palmergames.adventure.text.Component;
import com.palmergames.adventure.text.event.ClickEvent;
import com.palmergames.adventure.text.event.HoverEvent;
import com.palmergames.adventure.text.minimessage.MiniMessage;
import com.palmergames.bukkit.towny.event.statusscreen.NationStatusScreenEvent;
import com.palmergames.bukkit.towny.event.statusscreen.TownStatusScreenEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.unitedUtils.UnitedUtils;

public class StatusScreenListener implements Listener {
    private final UnitedUtils plugin;
    private final @NotNull MiniMessage miniMessage = MiniMessage.miniMessage();

    public StatusScreenListener(UnitedUtils plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onStatusScreen(NationStatusScreenEvent event) {
        this.addNationWikiLink(event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onStatusScreen(TownStatusScreenEvent event) {
        this.addTownWikiLink(event);
    }

    private void addNationWikiLink(NationStatusScreenEvent event) {
        event.getStatusScreen().addComponentOf("wiki", "§7[§aWiki§7]",
                HoverEvent.showText(Component.text("§7https://wiki.unitedlands.net/wiki/" + event.getNation().getName())
                        .appendNewline().append(Component.text("§fClick to view wiki page."))),
                ClickEvent.openUrl("https://wiki.unitedlands.net/wiki/" + event.getNation().getName()));
    }

    private void addTownWikiLink(TownStatusScreenEvent event) {
        event.getStatusScreen().addComponentOf("wiki", "§7[§aWiki§7]",
                HoverEvent.showText(Component.text("§7https://wiki.unitedlands.net/wiki/" + event.getTown().getName())
                        .appendNewline().append(Component.text("§fClick to view wiki page."))),
                ClickEvent.openUrl("https://wiki.unitedlands.net/wiki/" + event.getTown().getName()));
    }
}
