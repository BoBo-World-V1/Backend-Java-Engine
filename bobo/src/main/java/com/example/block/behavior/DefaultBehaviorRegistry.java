package com.example.block.behavior;

public final class DefaultBehaviorRegistry {
    private DefaultBehaviorRegistry() {
    }

    public static BehaviorRegistry create() {
        BehaviorRegistry registry = new BehaviorRegistry();
        SurfaceSoilBehavior surfaceSoilBehavior = new SurfaceSoilBehavior();
        registry.register(SurfaceSoilBehavior.GRASS_BLOCK_ID, surfaceSoilBehavior);
        registry.register(SurfaceSoilBehavior.DIRT_BLOCK_ID, surfaceSoilBehavior);
        registry.register(LavaBlockBehavior.LAVA_BLOCK_ID, new LavaBlockBehavior());
        return registry;
    }
}
