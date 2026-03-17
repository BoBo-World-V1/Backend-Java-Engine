package com.example.world;

import com.example.block.behavior.SurfaceSoilBehavior;
import java.util.concurrent.ThreadLocalRandom;

public class WorldGenerator {
    private static final int SPAWN_DOOR_BLOCK_ID = 8;
    private static final int BEDROCK_BLOCK_ID = 9;

    public World generate(String name, int width, int height) {
        if (width < 8 || height < 8) {
            throw new IllegalArgumentException("World dimensions must be at least 8x8");
        }

        World world = new World(name, width, height);
        int surfaceY = height / 2;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int blockId;
                if (y < surfaceY) {
                    blockId = World.AIR_BLOCK_ID;
                } else if (y == surfaceY) {
                    blockId = SurfaceSoilBehavior.GRASS_BLOCK_ID;
                } else {
                    blockId = SurfaceSoilBehavior.DIRT_BLOCK_ID;
                }
                world.setForeground(x, y, blockId);
            }
        }

        int spawnX = chooseSpawnX(width);
        int spawnY = Math.max(0, surfaceY - 1);
        world.setForeground(spawnX, spawnY, SPAWN_DOOR_BLOCK_ID);
        world.setForeground(spawnX, spawnY + 1, BEDROCK_BLOCK_ID);
        world.setSpawnPoint(spawnX, spawnY);
        world.setSpawnStructure(spawnX, spawnY, spawnX, spawnY + 1);
        world.clearDirty();
        return world;
    }

    private int chooseSpawnX(int width) {
        return ThreadLocalRandom.current().nextInt(width);
    }
}
