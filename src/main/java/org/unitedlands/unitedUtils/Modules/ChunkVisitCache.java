package org.unitedlands.unitedUtils.Modules;

import java.util.BitSet;

public class ChunkVisitCache {
    private final int width, height;
    private final int xOffset, zOffset;
    private final BitSet visited;

    public ChunkVisitCache(int minX, int maxX, int minZ, int maxZ) {
        this.width = maxX - minX + 1;
        this.height = maxZ - minZ + 1;
        this.xOffset = -minX;
        this.zOffset = -minZ;
        this.visited = new BitSet(width * height);
    }

    private int index(int x, int z) {
        return (z + zOffset) * width + (x + xOffset);
    }

    /** Returns true if this is the first time this chunk was marked. */
    public boolean markIfNew(int x, int z) {
        int i = index(x, z);
        if (visited.get(i)) return false;
        visited.set(i);
        return true;
    }

    public boolean isVisited(int x, int z) {
        return visited.get(index(x, z));
    }
}