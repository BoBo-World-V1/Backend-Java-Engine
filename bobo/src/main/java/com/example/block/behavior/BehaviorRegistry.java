package com.example.block.behavior;

import java.util.HashMap;
import java.util.Map;

public class BehaviorRegistry {
    private final Map<Integer, BlockBehavior> behaviors = new HashMap<>();

    public void register(int blockId, BlockBehavior behavior){
        behaviors.put(blockId, behavior);
    }
    public BlockBehavior get(int blockId){
        return behaviors.get(blockId);
    }

    public boolean hasBehavior(int blockID){
        return behaviors.containsKey(blockID);
    }
}
