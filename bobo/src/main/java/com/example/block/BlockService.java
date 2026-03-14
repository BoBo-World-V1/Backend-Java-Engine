package com.example.block;

import java.util.Random;

import com.example.block.behavior.BehaviorRegistry;
import com.example.block.behavior.BlockBehavior;
import com.example.player.Player;
import com.example.world.Tile;
import com.example.world.World;
import java.util.concurrent.ThreadLocalRandom;

public class BlockService {
    private final BehaviorRegistry behaviorRegistry;
    private final BlockRegistry blockRegistry; 
    private static final int AIR_BLOCK_ID = 0;
    public BlockService (BehaviorRegistry behaviorRegistry, BlockRegistry blockRegistry){
        this.behaviorRegistry = behaviorRegistry;
        this.blockRegistry = blockRegistry;
    } 

    public void placeBlock(Player player, World world, int x, int y, int blockID){
    }

    private void validatePlacement (World world, int x, int y, int blockID){
        if(!world.isInBounds(x, y)){
            throw new IllegalArgumentException("Placement cannot be out of bound");
        }
        BlockDefinition newBlock = blockRegistry.get(blockID);
        if (newBlock == null){
            throw new IllegalArgumentException("Block not found");
        }
        Tile tile = world.getTile(x, y);

        if(tile.getForeground() != AIR_BLOCK_ID){
            throw new IllegalStateException("Tile is already occupied");
        }
    }

    private void notifyNeighbors(World world, int x, int y, Player player){
        int[][] directions = {
                { 1, 0 },
                { -1, 0 },
                { 0, 1 },
                { 0, -1 }
        };

    for(int [] dir : directions){
        int nx = x + dir[0];
        int ny = y + dir[1];

        if (!world.isInBounds(nx, ny)){
            continue;
        }

        int neighBlockId = world.getTile(nx,ny).getForeground();
        BlockBehavior neighborBehavior = behaviorRegistry.get(neighBlockId);

        if(neighborBehavior != null){
            neighborBehavior.onNeighBorChanged(world, x, y, player);
            }
        }
    }

    private void maybeDropEntity (BlockDefinition block, int x, int y){
        String dropEntity = block.getDropEntity();
        if(percentChance(block.getChanceDropSeed())){
            System.out.println("Drop Item");
            //TODO Implement the entity method later
        }
    }
    private void maybeDropSeed (BlockDefinition block, int x, int y){
        // String dropEntity = block.getDrop();
        if(percentChance(block.getChanceDropSeed())){
            System.out.println("Drop Item");
            //TODO Implement the entity method later
        }
    }
    private boolean percentChance(float chance) {
    return ThreadLocalRandom.current().nextFloat() <= chance;
}



    
}
