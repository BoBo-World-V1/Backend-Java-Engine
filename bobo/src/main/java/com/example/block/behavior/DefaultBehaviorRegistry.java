package com.example.block.behavior;

import com.example.block.BlockDefinition;
import com.example.block.BlockRegistry;
import com.example.block.BlockType;

public final class DefaultBehaviorRegistry {
    private DefaultBehaviorRegistry() {
    }

    public static BehaviorRegistry create(BlockRegistry blockRegistry) {
        BehaviorRegistry registry = new BehaviorRegistry();
        SurfaceSoilBehavior surfaceSoilBehavior = new SurfaceSoilBehavior();
        CropBlockBehavior cropBlockBehavior = new CropBlockBehavior(blockRegistry);
        registry.register(SurfaceSoilBehavior.GRASS_BLOCK_ID, surfaceSoilBehavior);
        registry.register(SurfaceSoilBehavior.DIRT_BLOCK_ID, surfaceSoilBehavior);
        registry.register(LavaBlockBehavior.LAVA_BLOCK_ID, new LavaBlockBehavior());
        for (BlockDefinition block : blockRegistry.getAll()) {
            if (block.getType() == BlockType.CROP) {
                registry.register(block.getId(), cropBlockBehavior);
            }
        }
        return registry;
    }
}
