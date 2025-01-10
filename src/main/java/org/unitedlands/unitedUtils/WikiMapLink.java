package org.unitedlands.unitedUtils;

import me.silverwolfg11.maptowny.MapTownyPlugin;
import me.silverwolfg11.maptowny.events.MapReloadEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WikiMapLink implements Listener {

    @EventHandler
    public void onPluginReload(MapReloadEvent event) {
        registerStrippedNationStatus();
    }

    private MapTownyPlugin getMapPlugin() {
        return (MapTownyPlugin) Bukkit.getPluginManager().getPlugin("MapTowny");
    }

    public void registerStrippedNationStatus() {
        getMapPlugin().getLayerManager().registerReplacement("strippednationstatus", town -> {
            if (!town.hasNation())
                return "";
            return town.isCapital() ? "Capital of the " : "Member of the ";
        });
    }
}