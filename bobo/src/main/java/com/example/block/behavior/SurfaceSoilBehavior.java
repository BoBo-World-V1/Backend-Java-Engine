package com.example.block.behavior;

import com.example.player.Player;
import com.example.world.World;

public class SurfaceSoilBehavior implements BlockBehavior {
    public static final int GRASS_BLOCK_ID = 1;
    public static final int DIRT_BLOCK_ID = 2;

    @Override
    public void onPLaced(World world, int x, int y, Player player) {
        refreshSoilState(world, x, y);
        refreshSoilState(world, x, y + 1);
    }

    @Override
    public void onNeighBorChanged(World world, int x, int y, Player player) {
        refreshSoilState(world, x, y);
    }

    public void refreshSoilState(World world, int x, int y) {
        if (!world.isInBounds(x, y)) {
            return;
        }

        int blockId = world.getTile(x, y).getForeground();
        if (blockId != GRASS_BLOCK_ID && blockId != DIRT_BLOCK_ID) {
            return;
        }

        boolean exposedToAir = y == 0 || world.getTile(x, y - 1).getForeground() == World.AIR_BLOCK_ID;
        int desiredBlockId = exposedToAir ? GRASS_BLOCK_ID : DIRT_BLOCK_ID;
        if (blockId != desiredBlockId) {
            world.setForeground(x, y, desiredBlockId);
        }
    }
}
