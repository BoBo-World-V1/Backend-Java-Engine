package com.example.block.behavior;

import com.example.block.BlockDefinition;
import com.example.block.BlockRegistry;
import com.example.block.BlockType;
import com.example.entity.WorldDrop;
import com.example.player.Player;
import com.example.world.Tile;
import com.example.world.World;
import java.util.concurrent.ThreadLocalRandom;

public class CropBlockBehavior implements BlockBehavior {
    private final BlockRegistry blockRegistry;

    public CropBlockBehavior(BlockRegistry blockRegistry) {
        this.blockRegistry = blockRegistry;
    }

    @Override
    public void onPLaced(World world, int x, int y, Player player) {
        if (!hasSupport(world, x, y)) {
            throw new IllegalStateException("Seeds and crops must be placed on top of a solid block");
        }
        world.getTile(x, y).setFlags(0);
    }

    @Override
    public void onNeighBorChanged(World world, int x, int y, Player player) {
        BlockDefinition block = getCropBlock(world, x, y);
        if (block == null || hasSupport(world, x, y)) {
            return;
        }

        dropForCurrentState(world, x, y, block);
        clearCrop(world, x, y);
    }

    @Override
    public void onTick(World world, int x, int y) {
        BlockDefinition block = getCropBlock(world, x, y);
        if (block == null) {
            return;
        }
        if (!hasSupport(world, x, y)) {
            dropForCurrentState(world, x, y, block);
            clearCrop(world, x, y);
            return;
        }

        Tile tile = world.getTile(x, y);
        int readyTicks = block.getGrowthTicksRequired();
        int growthTicks = Math.min(readyTicks, tile.getFlags() + 1);
        if (growthTicks != tile.getFlags()) {
            tile.setFlags(growthTicks);
            world.markDirty();
        }
    }

    @Override
    public int getBreakHealth(World world, int x, int y, BlockDefinition block) {
        return isReady(world, x, y, block) ? 1 : block.getHealth();
    }

    @Override
    public boolean usesCustomDrops() {
        return true;
    }

    @Override
    public void onBroken(World world, int x, int y, Player player, BlockDefinition block) {
        dropForCurrentState(world, x, y, block);
    }

    public boolean isReady(World world, int x, int y) {
        BlockDefinition block = getCropBlock(world, x, y);
        return block != null && isReady(world, x, y, block);
    }

    private boolean isReady(World world, int x, int y, BlockDefinition block) {
        return world.isInBounds(x, y) && world.getTile(x, y).getFlags() >= block.getGrowthTicksRequired();
    }

    private BlockDefinition getCropBlock(World world, int x, int y) {
        if (!world.isInBounds(x, y)) {
            return null;
        }
        BlockDefinition block = blockRegistry.get(world.getTile(x, y).getForeground());
        if (block == null || block.getType() != BlockType.CROP) {
            return null;
        }
        return block;
    }

    private void dropForCurrentState(World world, int x, int y, BlockDefinition block) {
        if (isReady(world, x, y, block)) {
            String dropEntity = block.getDropEntity();
            if (dropEntity != null) {
                int dropAmount = ThreadLocalRandom.current().nextInt(1, block.getMaximumHarvestDropAmount() + 1);
                world.spawnDrop(new WorldDrop(dropEntity, x + 0.5f, y + 0.5f, dropAmount));
            }
            maybeDropSeed(world, x, y, block);
            return;
        }

        maybeDropSeed(world, x, y, block);
    }

    private void maybeDropSeed(World world, int x, int y, BlockDefinition block) {
        String seedEntity = block.getDropSeedEntity();
        if (seedEntity == null) {
            return;
        }
        if (ThreadLocalRandom.current().nextFloat() <= block.getChanceDropSeed()) {
            world.spawnDrop(new WorldDrop(seedEntity, x + 0.5f, y + 0.5f, 1));
        }
    }

    private boolean hasSupport(World world, int x, int y) {
        return world.isInBounds(x, y + 1) && world.isSolid(x, y + 1);
    }

    private void clearCrop(World world, int x, int y) {
        Tile tile = world.getTile(x, y);
        tile.setForeground(World.AIR_BLOCK_ID);
        tile.setDamage(0);
        tile.setFlags(0);
        world.markDirty();
    }
}
