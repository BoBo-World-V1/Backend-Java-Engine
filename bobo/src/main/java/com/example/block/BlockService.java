package com.example.block;

import com.example.block.behavior.BehaviorRegistry;
import com.example.block.behavior.BlockBehavior;
import com.example.player.Player;
import com.example.world.AreaLock;
import com.example.world.Tile;
import com.example.world.World;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class BlockService {
    private static final float MAX_INTERACTION_DISTANCE = 2.5f;
    private final BehaviorRegistry behaviorRegistry;
    private final BlockRegistry blockRegistry; 
    public BlockService (BehaviorRegistry behaviorRegistry, BlockRegistry blockRegistry){
        this.behaviorRegistry = behaviorRegistry;
        this.blockRegistry = blockRegistry;
    } 

    public void placeBlock(Player player, World world, int x, int y, int blockID){
        validateWorldOwnership(player, world);
        validateReach(player, x, y);
        validatePlacement(world, x, y, blockID);
        if ((int) Math.floor(player.getX()) == x && (int) Math.floor(player.getY()) == y) {
            throw new IllegalStateException("Cannot place a block inside the player position");
        }
        validateOwnershipAccess(player, world, x, y, blockID, true);
        world.setForeground(x, y, blockID);
        registerOwnershipOnPlacement(player, world, x, y, blockID);

        BlockBehavior behavior = behaviorRegistry.get(blockID);
        if (behavior != null) {
            behavior.onPLaced(world, x, y, player);
        }
        notifyNeighbors(world, x, y, player);
    }

    public boolean breakBlock(Player player, World world, int x, int y, int damage) {
        validateWorldOwnership(player, world);
        validateReach(player, x, y);
        validateBreaking(world, x, y);
        validateOwnershipAccess(player, world, x, y, world.getTile(x, y).getForeground(), false);

        Tile tile = world.getTile(x, y);
        int blockId = tile.getForeground();
        BlockDefinition block = blockRegistry.get(blockId);
        int totalDamage = tile.getDamage() + Math.max(1, damage);

        if (totalDamage < block.getHealth()) {
            tile.setDamage(totalDamage);
            world.markDirty();
            return false;
        }

        tile.setForeground(World.AIR_BLOCK_ID);
        tile.setDamage(0);
        world.markDirty();
        unregisterOwnershipOnBreak(world, x, y, blockId);

        BlockBehavior behavior = behaviorRegistry.get(blockId);
        if (behavior != null) {
            behavior.onBroken(world, x, y, player);
        }

        maybeDropEntity(block, x, y);
        maybeDropSeed(block, x, y);
        notifyNeighbors(world, x, y, player);
        return true;
    }

    private void validatePlacement (World world, int x, int y, int blockID){
        validateWorldOwnership(world, x, y);
        if(!world.isInBounds(x, y)){
            throw new IllegalArgumentException("Placement cannot be out of bound");
        }
        if (world.isSpawnStructure(x, y)) {
            throw new IllegalStateException("Spawn structure cannot be modified");
        }
        BlockDefinition newBlock = blockRegistry.get(blockID);
        if (newBlock == null){
            throw new IllegalArgumentException("Block not found");
        }
        Tile tile = world.getTile(x, y);

        if(tile.getForeground() != World.AIR_BLOCK_ID){
            throw new IllegalStateException("Tile is already occupied");
        }
    }

    private void validateBreaking(World world, int x, int y) {
        validateWorldOwnership(world, x, y);
        if (!world.isInBounds(x, y)) {
            throw new IllegalArgumentException("Breaking cannot be out of bound");
        }
        if (world.isSpawnStructure(x, y)) {
            throw new IllegalStateException("Spawn structure cannot be modified");
        }

        Tile tile = world.getTile(x, y);
        if (tile.getForeground() == World.AIR_BLOCK_ID) {
            throw new IllegalStateException("There is no block to break");
        }

        BlockDefinition block = blockRegistry.get(tile.getForeground());
        if (block == null) {
            throw new IllegalStateException("Block definition not found");
        }
        if (!block.isBreakable()) {
            throw new IllegalStateException("Block is not breakable");
        }
    }

    private void validateWorldOwnership(World world, int x, int y) {
        if (world == null) {
            throw new IllegalArgumentException("World is required");
        }
    }

    private void validateWorldOwnership(Player player, World world) {
        if (player == null) {
            throw new IllegalArgumentException("Player is required");
        }
        if (player.getWorld() != world) {
            throw new IllegalStateException("Player is not connected to the target world");
        }
    }

    private void validateReach(Player player, int x, int y) {
        float targetX = x + 0.5f;
        float targetY = y + 0.5f;
        float deltaX = targetX - player.getX();
        float deltaY = targetY - player.getY();
        float distanceSquared = (deltaX * deltaX) + (deltaY * deltaY);
        if (distanceSquared > MAX_INTERACTION_DISTANCE * MAX_INTERACTION_DISTANCE) {
            throw new IllegalStateException("Target block is out of range");
        }
    }

    private void validateOwnershipAccess(Player player, World world, int x, int y, int blockId, boolean placing) {
        BlockDefinition block = blockRegistry.get(blockId);
        if (block == null) {
            throw new IllegalStateException("Block definition not found");
        }

        if (placing && block.getType() == BlockType.WORLD_LOCK && world.hasWorldOwner()) {
            throw new IllegalStateException("World already has a world lock owner");
        }

        Optional<AreaLock> protectingArea = world.getProtectingAreaLockAt(x, y);
        boolean worldOwner = world.isWorldOwner(player.getId());
        boolean worldAdmin = world.isWorldAdmin(player.getId());
        boolean areaOwner = protectingArea.isPresent() && protectingArea.get().isOwner(player.getId());
        boolean areaAdmin = protectingArea.isPresent() && protectingArea.get().isAdmin(player.getId());

        if (block.getType() == BlockType.WORLD_LOCK) {
            if (world.hasWorldOwner() && !worldOwner) {
                throw new IllegalStateException("Only the world owner can modify this world");
            }
            return;
        }

        if (world.hasWorldOwner() && !worldAdmin && !areaAdmin) {
            throw new IllegalStateException("Only the world owner, world admins, or area owner can modify this tile");
        }

        if (protectingArea.isPresent() && !worldAdmin && !areaAdmin) {
            throw new IllegalStateException("Tile is protected by another player's lock");
        }

        if (placing && block.getType() == BlockType.AREA_LOCK) {
            if (world.getProtectingAreaLockAt(x, y).isPresent()) {
                throw new IllegalStateException("Cannot place a lock inside another protected area");
            }
        }
    }

    private void registerOwnershipOnPlacement(Player player, World world, int x, int y, int blockId) {
        BlockDefinition block = blockRegistry.get(blockId);
        if (block == null) {
            return;
        }

        if (block.getType() == BlockType.WORLD_LOCK) {
            world.setWorldOwner(player.getId(), x, y);
            return;
        }

        if (block.getType() == BlockType.AREA_LOCK) {
            world.addAreaLock(new AreaLock(x, y, player.getId(), block.getProtectionRadius(), blockId));
        }
    }

    private void unregisterOwnershipOnBreak(World world, int x, int y, int blockId) {
        BlockDefinition block = blockRegistry.get(blockId);
        if (block == null) {
            return;
        }

        if (block.getType() == BlockType.WORLD_LOCK && world.isWorldLock(x, y)) {
            world.clearWorldOwner();
            return;
        }

        if (block.getType() == BlockType.AREA_LOCK) {
            world.removeAreaLockAt(x, y);
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
            neighborBehavior.onNeighBorChanged(world, nx, ny, player);
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
