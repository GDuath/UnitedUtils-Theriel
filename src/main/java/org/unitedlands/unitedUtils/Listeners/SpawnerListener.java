package org.unitedlands.unitedUtils.Listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.unitedlands.unitedUtils.UnitedUtils;
import org.unitedlands.unitedUtils.Modules.ChunkVisitCache;

public class SpawnerListener implements Listener {

    private final UnitedUtils plugin;
    private final ChunkVisitCache cache;

    private List<String> worlds = new ArrayList<>();
    private List<String> whitelist = new ArrayList<>();

    public SpawnerListener(UnitedUtils plugin, ChunkVisitCache cache) {
        this.plugin = plugin;
        this.cache = cache;
        loadConfig();
    }

    public void loadConfig() {
        worlds = plugin.getConfig().getStringList("spawner-worlds");
        whitelist = plugin.getConfig().getStringList("spawner-whitelist");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {

        if (worlds == null || worlds.isEmpty() || whitelist == null || whitelist.isEmpty())
            return;
        if (!worlds.contains(event.getWorld().getName()))
            return;

        Chunk chunk = event.getChunk();
        int x = chunk.getX();
        int z = chunk.getZ();

        if (!cache.markIfNew(x, z)) {
            return;
        }

        for (BlockState state : chunk.getTileEntities()) {
            if (state.getType() == Material.SPAWNER) {
                CreatureSpawner spawner = (CreatureSpawner) state;
                var type = spawner.getSpawnedType().toString();

                if (!whitelist.contains(type)) {
                    var block = spawner.getBlock();
                    plugin.getLogger().info("Cleaning blacklisted " + type + " spawner at " + block.getLocation().toString());
                    block.setType(Material.AIR);
                }

            }
        }
    }

}
