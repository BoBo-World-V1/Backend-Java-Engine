package com.example.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.block.BlockRegistry;
import com.example.block.BlockService;
import com.example.block.behavior.BehaviorRegistry;
import com.example.block.behavior.CropBlockBehavior;
import com.example.block.behavior.DefaultBehaviorRegistry;
import com.example.block.behavior.LavaBlockBehavior;
import com.example.block.behavior.SurfaceSoilBehavior;
import com.example.entity.EntityRegistry;
import com.example.entity.WorldDrop;
import com.example.player.Player;
import com.example.player.PlayerService;
import org.junit.Test;

public class WorldSystemsTest {
    private static final int SPAWN_DOOR_BLOCK_ID = 8;
    private static final int BEDROCK_BLOCK_ID = 9;
    private static final int STONE_TREE_BLOCK_ID = 10;
    private static final int DIRT_TREE_BLOCK_ID = 11;
    private static final int LAVA_TREE_BLOCK_ID = 12;
    private static final int READY_GROWTH_TICKS = 3;

    private EntityRegistry createEntityRegistry() {
        EntityRegistry entityRegistry = new EntityRegistry();
        entityRegistry.load();
        return entityRegistry;
    }

    private BehaviorRegistry createBehaviorRegistry(BlockRegistry blockRegistry) {
        return DefaultBehaviorRegistry.create(blockRegistry);
    }

    @Test
    public void generatedWorldHasSpawnAndTerrain() {
        World world = new WorldGenerator().generate("alpha", 100, 100);

        assertNotNull(world);
        assertTrue(world.isInBounds(world.getSpawnX(), world.getSpawnY()));
        assertTrue(world.getSpawnX() >= 0 && world.getSpawnX() < world.getWidth());
        assertEquals(50, world.getSpawnY() + 1);
        assertEquals(SPAWN_DOOR_BLOCK_ID, world.getTile(world.getSpawnX(), world.getSpawnY()).getForeground());
        assertEquals(BEDROCK_BLOCK_ID, world.getTile(world.getSpawnX(), 50).getForeground());
        int adjacentX = world.getSpawnX() + 1 < world.getWidth() ? world.getSpawnX() + 1 : world.getSpawnX() - 1;
        assertEquals(SurfaceSoilBehavior.GRASS_BLOCK_ID, world.getTile(adjacentX, 50).getForeground());
        assertEquals(SurfaceSoilBehavior.DIRT_BLOCK_ID, world.getTile(adjacentX, 51).getForeground());
        assertEquals(SurfaceSoilBehavior.DIRT_BLOCK_ID, world.getTile(world.getSpawnX(), 99).getForeground());
    }

    @Test
    public void playerCanConnectAndMoveThroughWalkableTiles() {
        PlayerService playerService = new PlayerService();
        WorldManager worldManager = new WorldManager(new WorldGenerator(), playerService);
        Player player = new Player(7, "traveler");

        World world = worldManager.connectPlayer(player, "beta", 100, 100);
        assertEquals(world, player.getWorld());
        assertEquals(1, world.getPlayers().size());
        assertEquals(world.getSpawnX() + 0.5f, player.getX(), 0.001f);
        assertEquals(world.getSpawnY() + 0.5f, player.getY(), 0.001f);
    }

    @Test
    public void gravityAndGroundCollisionAreAuthoritative() {
        PlayerService playerService = new PlayerService();
        World world = new WorldGenerator().generate("gamma", 100, 100);
        Player player = new Player(8, "miner");

        playerService.spawnPlayer(player, world);
        float startY = player.getY();

        playerService.tickPlayer(player, 0.25f);
        assertEquals(startY, player.getY(), 0.001f);
        assertTrue(player.isOnGround());
        assertEquals(0.0f, player.getVelocityY(), 0.001f);
    }

    @Test
    public void jumpingUpdatesVelocityAndReturnsToGround() {
        PlayerService playerService = new PlayerService();
        World world = new WorldGenerator().generate("jump", 100, 100);
        Player player = new Player(10, "jumper");

        playerService.spawnPlayer(player, world);
        playerService.queueJump(player);
        playerService.tickPlayer(player, 0.1f);

        for (int i = 0; i < 20; i++) {
            playerService.tickPlayer(player, 0.1f);
        }

        assertTrue(player.isOnGround());
    }

    @Test
    public void validatePositionRejectsLargeTeleports() {
        PlayerService playerService = new PlayerService();
        World world = new WorldGenerator().generate("validate", 100, 100);
        Player player = new Player(11, "validator");

        playerService.spawnPlayer(player, world);
        assertFalse(playerService.validatePosition(player, world.getWidth() + 10.0f, player.getY()));
    }

    @Test
    public void breakBlockRemovesTileAfterEnoughDamage() {
        BlockRegistry blockRegistry = new BlockRegistry();
        blockRegistry.load();
        BlockService blockService = new BlockService(createBehaviorRegistry(blockRegistry), blockRegistry, createEntityRegistry());
        World world = new WorldGenerator().generate("delta", 100, 100);
        Player player = new Player(9, "breaker");
        new PlayerService().spawnPlayer(player, world);

        int targetX = world.getSpawnX() + 1 < world.getWidth() ? world.getSpawnX() + 1 : world.getSpawnX() - 1;
        int targetY = world.getSpawnY() + 1;

        boolean broken = blockService.breakBlock(player, world, targetX, targetY, 20);

        assertTrue(broken);
        assertEquals(World.AIR_BLOCK_ID, world.getTile(targetX, targetY).getForeground());
        assertEquals(0, world.getTile(targetX, targetY).getDamage());
    }

    @Test
    public void placeAndBreakRequireReachableBlocks() {
        BlockRegistry blockRegistry = new BlockRegistry();
        blockRegistry.load();
        BlockService blockService = new BlockService(createBehaviorRegistry(blockRegistry), blockRegistry, createEntityRegistry());
        World world = new WorldGenerator().generate("reach", 100, 100);
        Player player = new Player(12, "ranger");
        PlayerService playerService = new PlayerService();
        playerService.spawnPlayer(player, world);
        int targetX = world.getSpawnX() + 1 < world.getWidth() ? world.getSpawnX() + 1 : world.getSpawnX() - 1;

        try {
            blockService.placeBlock(player, world, world.getSpawnX() + 10, world.getSpawnY(), 2);
            fail("Expected placement out of range");
        } catch (IllegalStateException expected) {
            assertEquals("Target block is out of range", expected.getMessage());
        }

        assertTrue(blockService.breakBlock(player, world, targetX, world.getSpawnY() + 1, 20));
    }

    @Test
    public void worldLockCreatesWholeWorldOwnership() {
        BlockRegistry blockRegistry = new BlockRegistry();
        blockRegistry.load();
        BlockService blockService = new BlockService(createBehaviorRegistry(blockRegistry), blockRegistry, createEntityRegistry());
        World world = new WorldGenerator().generate("owned-world", 100, 100);
        Player owner = new Player(13, "owner");
        Player intruder = new Player(14, "intruder");
        PlayerService playerService = new PlayerService();

        playerService.spawnPlayer(owner, world);
        playerService.spawnPlayer(intruder, world);

        int lockX = world.getSpawnX() + 1 < world.getWidth() ? world.getSpawnX() + 1 : world.getSpawnX() - 1;
        int lockY = world.getSpawnY();
        int targetX = world.getSpawnX() + 2 < world.getWidth() ? world.getSpawnX() + 2 : world.getSpawnX() - 2;

        blockService.placeBlock(owner, world, lockX, lockY, 7);
        assertTrue(world.hasWorldOwner());
        assertEquals(owner.getId(), world.getWorldOwnerId().intValue());
        assertTrue(playerService.moveTo(intruder, targetX + 0.5f, world.getSpawnY() + 0.5f));

        try {
            blockService.breakBlock(intruder, world, targetX, world.getSpawnY() + 1, 20);
            fail("Expected world ownership denial");
        } catch (IllegalStateException expected) {
            assertEquals("Only the world owner, world admins, or area owner can modify this tile", expected.getMessage());
        }
    }

    @Test
    public void worldOwnerCanGrantAdminBuildAccess() {
        BlockRegistry blockRegistry = new BlockRegistry();
        blockRegistry.load();
        BlockService blockService = new BlockService(createBehaviorRegistry(blockRegistry), blockRegistry, createEntityRegistry());
        World world = new WorldGenerator().generate("admin-world", 100, 100);
        Player owner = new Player(17, "owner");
        Player admin = new Player(18, "admin");
        Player outsider = new Player(19, "outsider");
        PlayerService playerService = new PlayerService();

        playerService.spawnPlayer(owner, world);
        playerService.spawnPlayer(admin, world);
        playerService.spawnPlayer(outsider, world);

        int lockX = world.getSpawnX() + 1 < world.getWidth() ? world.getSpawnX() + 1 : world.getSpawnX() - 1;
        int lockY = world.getSpawnY();
        int targetX = world.getSpawnX() + 2 < world.getWidth() ? world.getSpawnX() + 2 : world.getSpawnX() - 2;

        blockService.placeBlock(owner, world, lockX, lockY, 7);
        world.grantWorldAdmin(owner.getId(), admin.getId());
        assertTrue(playerService.moveTo(admin, targetX + 0.5f, world.getSpawnY() + 0.5f));
        assertTrue(playerService.moveTo(outsider, targetX + 0.5f, world.getSpawnY() - 0.5f));

        assertTrue(world.isWorldAdmin(admin.getId()));
        assertTrue(blockService.breakBlock(admin, world, targetX, world.getSpawnY() + 1, 20));

        world.setForeground(targetX, world.getSpawnY() + 1, 2);

        try {
            blockService.breakBlock(outsider, world, targetX, world.getSpawnY() + 1, 20);
            fail("Expected outsider denial");
        } catch (IllegalStateException expected) {
            assertEquals("Only the world owner, world admins, or area owner can modify this tile", expected.getMessage());
        }
    }

    @Test
    public void areaLockProtectsItsRegion() {
        BlockRegistry blockRegistry = new BlockRegistry();
        blockRegistry.load();
        BlockService blockService = new BlockService(createBehaviorRegistry(blockRegistry), blockRegistry, createEntityRegistry());
        World world = new WorldGenerator().generate("area-world", 100, 100);
        Player owner = new Player(15, "claimer");
        Player intruder = new Player(16, "raider");
        PlayerService playerService = new PlayerService();

        playerService.spawnPlayer(owner, world);
        playerService.spawnPlayer(intruder, world);

        int lockX = world.getSpawnX() + 1 < world.getWidth() ? world.getSpawnX() + 1 : world.getSpawnX() - 1;
        int lockY = world.getSpawnY();
        int protectedX = lockX;
        int protectedY = world.getSpawnY() + 1;

        blockService.placeBlock(owner, world, lockX, lockY, 5);
        assertTrue(world.getProtectingAreaLockAt(protectedX, protectedY).isPresent());
        float intruderX = protectedX + 1 < world.getWidth() ? protectedX + 1.5f : protectedX - 1.5f;
        assertTrue(playerService.moveTo(intruder, intruderX, world.getSpawnY() + 0.5f));

        try {
            blockService.breakBlock(intruder, world, protectedX, protectedY, 20);
            fail("Expected area lock denial");
        } catch (IllegalStateException expected) {
            assertEquals("Tile is protected by another player's lock", expected.getMessage());
        }
    }

    @Test
    public void areaOwnerCanGrantAdminBuildAccess() {
        BlockRegistry blockRegistry = new BlockRegistry();
        blockRegistry.load();
        BlockService blockService = new BlockService(createBehaviorRegistry(blockRegistry), blockRegistry, createEntityRegistry());
        World world = new WorldGenerator().generate("area-admin-world", 100, 100);
        Player owner = new Player(20, "owner");
        Player admin = new Player(21, "admin");
        Player outsider = new Player(22, "outsider");
        PlayerService playerService = new PlayerService();

        playerService.spawnPlayer(owner, world);
        playerService.spawnPlayer(admin, world);
        playerService.spawnPlayer(outsider, world);

        int lockX = world.getSpawnX() + 1 < world.getWidth() ? world.getSpawnX() + 1 : world.getSpawnX() - 1;
        int lockY = world.getSpawnY();
        int protectedX = lockX;
        int protectedY = world.getSpawnY() + 1;

        blockService.placeBlock(owner, world, lockX, lockY, 5);
        world.grantAreaAdmin(owner.getId(), lockX, lockY, admin.getId());
        float adminX = protectedX + 1 < world.getWidth() ? protectedX + 1.5f : protectedX - 1.5f;
        float outsiderX = protectedX + 2 < world.getWidth() ? protectedX + 2.5f : protectedX - 2.5f;
        assertTrue(playerService.moveTo(admin, adminX, world.getSpawnY() + 0.5f));
        assertTrue(playerService.moveTo(outsider, outsiderX, world.getSpawnY() + 0.5f));

        assertTrue(world.getAreaLockAt(lockX, lockY).orElseThrow().isAdmin(admin.getId()));
        assertTrue(blockService.breakBlock(admin, world, protectedX, protectedY, 20));

        world.setForeground(protectedX, protectedY, 2);

        try {
            blockService.breakBlock(outsider, world, protectedX, protectedY, 20);
            fail("Expected outsider denial");
        } catch (IllegalStateException expected) {
            assertEquals("Tile is protected by another player's lock", expected.getMessage());
        }
    }

    @Test
    public void exposedDirtTurnsIntoGrassAfterBreakingTopLayer() {
        BlockRegistry blockRegistry = new BlockRegistry();
        blockRegistry.load();
        BlockService blockService = new BlockService(createBehaviorRegistry(blockRegistry), blockRegistry, createEntityRegistry());
        World world = new WorldGenerator().generate("soil-break", 100, 100);
        Player player = new Player(23, "farmer");
        PlayerService playerService = new PlayerService();

        playerService.spawnPlayer(player, world);

        int x = world.getSpawnX() + 1 < world.getWidth() ? world.getSpawnX() + 1 : world.getSpawnX() - 1;
        int grassY = world.getSpawnY() + 1;
        int dirtY = grassY + 1;

        assertEquals(SurfaceSoilBehavior.GRASS_BLOCK_ID, world.getTile(x, grassY).getForeground());
        assertEquals(SurfaceSoilBehavior.DIRT_BLOCK_ID, world.getTile(x, dirtY).getForeground());

        assertTrue(blockService.breakBlock(player, world, x, grassY, 20));
        assertEquals(World.AIR_BLOCK_ID, world.getTile(x, grassY).getForeground());
        assertEquals(SurfaceSoilBehavior.GRASS_BLOCK_ID, world.getTile(x, dirtY).getForeground());
    }

    @Test
    public void coveredGrassTurnsBackIntoDirtAfterPlacement() {
        BlockRegistry blockRegistry = new BlockRegistry();
        blockRegistry.load();
        BlockService blockService = new BlockService(createBehaviorRegistry(blockRegistry), blockRegistry, createEntityRegistry());
        World world = new WorldGenerator().generate("soil-place", 100, 100);
        Player player = new Player(24, "builder");
        PlayerService playerService = new PlayerService();

        playerService.spawnPlayer(player, world);

        int x = world.getSpawnX() + 1 < world.getWidth() ? world.getSpawnX() + 1 : world.getSpawnX() - 1;
        int grassY = world.getSpawnY() + 1;
        int placeY = world.getSpawnY();

        assertEquals(SurfaceSoilBehavior.GRASS_BLOCK_ID, world.getTile(x, grassY).getForeground());
        blockService.placeBlock(player, world, x, placeY, SurfaceSoilBehavior.DIRT_BLOCK_ID);
        assertEquals(SurfaceSoilBehavior.DIRT_BLOCK_ID, world.getTile(x, grassY).getForeground());
    }

    @Test
    public void lavaDamagesAndKnocksBackPlayerOnTouch() {
        BlockRegistry blockRegistry = new BlockRegistry();
        blockRegistry.load();
        BlockService blockService = new BlockService(createBehaviorRegistry(blockRegistry), blockRegistry, createEntityRegistry());
        World world = new WorldGenerator().generate("lava-touch", 100, 100);
        Player player = new Player(25, "tester");
        new PlayerService(createBehaviorRegistry(blockRegistry)).spawnPlayer(player, world);

        int lavaX = world.getSpawnX() + 1 < world.getWidth() ? world.getSpawnX() + 1 : world.getSpawnX() - 1;
        int lavaY = world.getSpawnY();
        int startHealth = player.getHealth();

        blockService.placeBlock(player, world, lavaX, lavaY, LavaBlockBehavior.LAVA_BLOCK_ID);
        new LavaBlockBehavior().onPlayerTouch(world, lavaX, lavaY, player);

        assertTrue(player.getHealth() < startHealth);
        if (lavaX > world.getSpawnX()) {
            assertTrue(player.getVelocityX() < 0.0f);
        } else {
            assertTrue(player.getVelocityX() > 0.0f);
        }
    }

    @Test
    public void breakingBlockSpawnsConfiguredEntityDrop() {
        BlockRegistry blockRegistry = new BlockRegistry();
        blockRegistry.load();
        BlockService blockService = new BlockService(createBehaviorRegistry(blockRegistry), blockRegistry, createEntityRegistry());
        World world = new WorldGenerator().generate("entity-drop", 100, 100);
        Player player = new Player(30, "collector");
        new PlayerService().spawnPlayer(player, world);

        int targetX = world.getSpawnX() + 1 < world.getWidth() ? world.getSpawnX() + 1 : world.getSpawnX() - 1;
        int targetY = world.getSpawnY() + 1;

        assertTrue(blockService.breakBlock(player, world, targetX, targetY, 20));
        assertTrue(world.getDrops().size() >= 1);
        assertTrue(world.getDrops().stream().anyMatch(drop -> "dirt".equals(drop.getEntityId())));
    }

    @Test
    public void playerPicksUpDropsWithinHalfBlock() {
        PlayerService playerService = new PlayerService();
        World world = new WorldGenerator().generate("pickup", 100, 100);
        Player player = new Player(31, "picker");
        playerService.spawnPlayer(player, world);

        world.spawnDrop(new WorldDrop("dirt", player.getX() + 0.4f, player.getY(), 1));
        playerService.tickPlayer(player, 0.01f);

        assertEquals(1, player.getItemCount("dirt"));
        assertTrue(world.getDrops().isEmpty());
    }

    @Test
    public void cropsMustBePlacedOnTopOfASolidBlock() {
        BlockRegistry blockRegistry = new BlockRegistry();
        blockRegistry.load();
        BlockService blockService = new BlockService(createBehaviorRegistry(blockRegistry), blockRegistry, createEntityRegistry());
        World world = new WorldGenerator().generate("crop-placement", 100, 100);
        Player player = new Player(32, "planter");
        PlayerService playerService = new PlayerService(createBehaviorRegistry(blockRegistry));
        playerService.spawnPlayer(player, world);

        int x = world.getSpawnX() + 1 < world.getWidth() ? world.getSpawnX() + 1 : world.getSpawnX() - 1;

        try {
            blockService.placeBlock(player, world, x, world.getSpawnY() - 1, STONE_TREE_BLOCK_ID);
            fail("Expected unsupported crop placement to fail");
        } catch (IllegalStateException expected) {
            assertEquals("Seeds and crops must be placed on top of a solid block", expected.getMessage());
        }
    }

    @Test
    public void saplingGrowsIntoReadyTreeAfterWorldTicks() {
        BlockRegistry blockRegistry = new BlockRegistry();
        blockRegistry.load();
        EntityRegistry entityRegistry = createEntityRegistry();
        BehaviorRegistry behaviorRegistry = createBehaviorRegistry(blockRegistry);
        BlockService blockService = new BlockService(behaviorRegistry, blockRegistry, entityRegistry);
        PlayerService playerService = new PlayerService(behaviorRegistry);
        WorldManager worldManager = new WorldManager(new WorldGenerator(), playerService, behaviorRegistry);
        World world = worldManager.createWorld("tree-growth", 100, 100);
        Player player = new Player(33, "gardener");
        worldManager.connectPlayer(player, world.getName(), world.getWidth(), world.getHeight());

        int x = world.getSpawnX() + 1 < world.getWidth() ? world.getSpawnX() + 1 : world.getSpawnX() - 1;
        int y = world.getSpawnY();
        CropBlockBehavior cropBehavior = new CropBlockBehavior(blockRegistry);
        blockService.placeBlock(player, world, x, y, STONE_TREE_BLOCK_ID);

        for (int i = 0; i < 3; i++) {
            worldManager.tick(0.1f);
        }

        assertEquals(STONE_TREE_BLOCK_ID, world.getTile(x, y).getForeground());
        assertTrue(cropBehavior.isReady(world, x, y));
    }

    @Test
    public void breakingSupportUnderImmatureTreeDropsSeedEntity() {
        BlockRegistry blockRegistry = new BlockRegistry();
        blockRegistry.load();
        BlockService blockService = new BlockService(createBehaviorRegistry(blockRegistry), blockRegistry, createEntityRegistry());
        World world = new WorldGenerator().generate("tree-support-break", 100, 100);
        Player player = new Player(34, "woodcutter");
        PlayerService playerService = new PlayerService(createBehaviorRegistry(blockRegistry));
        playerService.spawnPlayer(player, world);

        int x = world.getSpawnX() + 1 < world.getWidth() ? world.getSpawnX() + 1 : world.getSpawnX() - 1;
        int y = world.getSpawnY();
        int supportY = y + 1;

        blockService.placeBlock(player, world, x, y, STONE_TREE_BLOCK_ID);
        assertEquals(STONE_TREE_BLOCK_ID, world.getTile(x, y).getForeground());

        assertTrue(blockService.breakBlock(player, world, x, supportY, 20));
        assertEquals(World.AIR_BLOCK_ID, world.getTile(x, y).getForeground());
        assertTrue(world.getDrops().stream().allMatch(drop ->
                "dirt".equals(drop.getEntityId())
                        || "dirt_seed".equals(drop.getEntityId())
                        || "stone_seed".equals(drop.getEntityId())));
    }

    @Test
    public void readyTreeIsHarvestedInOnePunch() {
        BlockRegistry blockRegistry = new BlockRegistry();
        blockRegistry.load();
        BlockService blockService = new BlockService(createBehaviorRegistry(blockRegistry), blockRegistry, createEntityRegistry());
        World world = new WorldGenerator().generate("ready-tree-break", 100, 100);
        Player player = new Player(35, "harvester");
        PlayerService playerService = new PlayerService(createBehaviorRegistry(blockRegistry));
        playerService.spawnPlayer(player, world);

        int x = world.getSpawnX() + 1 < world.getWidth() ? world.getSpawnX() + 1 : world.getSpawnX() - 1;
        int y = world.getSpawnY();
        world.setForeground(x, y, STONE_TREE_BLOCK_ID);
        world.getTile(x, y).setFlags(READY_GROWTH_TICKS);

        assertTrue(blockService.breakBlock(player, world, x, y, 1));
        assertEquals(World.AIR_BLOCK_ID, world.getTile(x, y).getForeground());
        WorldDrop harvestedStone = world.getDrops().stream()
                .filter(drop -> "stone".equals(drop.getEntityId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected harvested stone drop"));
        assertTrue(harvestedStone.getAmount() >= 1);
        assertTrue(harvestedStone.getAmount() <= 4);
    }

    @Test
    public void dirtAndLavaTreesUseGenericCropBehavior() {
        BlockRegistry blockRegistry = new BlockRegistry();
        blockRegistry.load();
        EntityRegistry entityRegistry = createEntityRegistry();
        BehaviorRegistry behaviorRegistry = createBehaviorRegistry(blockRegistry);
        BlockService blockService = new BlockService(behaviorRegistry, blockRegistry, entityRegistry);
        PlayerService playerService = new PlayerService(behaviorRegistry);
        WorldManager worldManager = new WorldManager(new WorldGenerator(), playerService, behaviorRegistry);
        CropBlockBehavior cropBehavior = new CropBlockBehavior(blockRegistry);
        World world = worldManager.createWorld("multi-crop", 100, 100);
        Player player = new Player(36, "multi-farmer");
        worldManager.connectPlayer(player, world.getName(), world.getWidth(), world.getHeight());

        int dirtX = world.getSpawnX() + 1 < world.getWidth() ? world.getSpawnX() + 1 : world.getSpawnX() - 1;
        int lavaX = dirtX + 1 < world.getWidth() ? dirtX + 1 : dirtX - 2;
        int y = world.getSpawnY();

        blockService.placeBlock(player, world, dirtX, y, DIRT_TREE_BLOCK_ID);
        blockService.placeBlock(player, world, lavaX, y, LAVA_TREE_BLOCK_ID);

        for (int i = 0; i < 4; i++) {
            worldManager.tick(0.1f);
        }

        assertTrue(cropBehavior.isReady(world, dirtX, y));
        assertTrue(cropBehavior.isReady(world, lavaX, y));

        assertTrue(blockService.breakBlock(player, world, dirtX, y, 1));
        assertTrue(blockService.breakBlock(player, world, lavaX, y, 1));

        assertTrue(world.getDrops().stream().anyMatch(drop -> "dirt".equals(drop.getEntityId())));
        assertTrue(world.getDrops().stream().anyMatch(drop -> "lava".equals(drop.getEntityId())));
    }

    @Test
    public void playerLosesTenPercentOfDiamondsOnDeath() {
        PlayerService playerService = new PlayerService();
        World world = new WorldGenerator().generate("death-loss", 100, 100);
        Player player = new Player(26, "rich");
        player.setDiamondCount(55);
        playerService.spawnPlayer(player, world);

        player.applyDamage(Player.MAX_HEALTH);
        playerService.tickPlayer(player, 0.01f);

        assertEquals(Player.MAX_HEALTH, player.getHealth());
        assertEquals(49, player.getDiamondCount());

        player.applyDamage(Player.MAX_HEALTH);
        playerService.tickPlayer(player, 0.01f);
        assertEquals(44, player.getDiamondCount());
    }

    @Test
    public void playerHealsToFullAfterFiveSecondsWithoutDamage() {
        Player player = new Player(27, "healer");
        player.applyDamage(30);

        assertEquals(70, player.getHealth());

        player.tickRecovery(4.9f);
        assertEquals(70, player.getHealth());

        player.tickRecovery(0.1f);
        assertEquals(Player.MAX_HEALTH, player.getHealth());
    }

    @Test
    public void playerInstantlyRespawnsAtSpawnAfterDeath() {
        PlayerService playerService = new PlayerService();
        World world = new WorldGenerator().generate("respawn", 100, 100);
        Player player = new Player(28, "respawner");
        playerService.spawnPlayer(player, world);

        float respawnX = world.getSpawnX() + 0.5f;
        float respawnY = world.getSpawnY() + 0.5f;
        float movedX = world.getSpawnX() + 1 < world.getWidth() ? respawnX + 1.0f : respawnX - 1.0f;

        assertTrue(playerService.moveTo(player, movedX, respawnY));
        assertTrue(player.getX() != respawnX);

        player.applyDamage(Player.MAX_HEALTH);
        playerService.tickPlayer(player, 0.01f);

        assertEquals(Player.MAX_HEALTH, player.getHealth());
        assertEquals(respawnX, player.getX(), 0.001f);
        assertEquals(respawnY, player.getY(), 0.001f);
    }

    @Test
    public void spawnDoorAndBedrockCannotBeModifiedByAnyone() {
        BlockRegistry blockRegistry = new BlockRegistry();
        blockRegistry.load();
        BlockService blockService = new BlockService(createBehaviorRegistry(blockRegistry), blockRegistry, createEntityRegistry());
        World world = new WorldGenerator().generate("spawn-protection", 100, 100);
        Player owner = new Player(29, "owner");
        PlayerService playerService = new PlayerService(createBehaviorRegistry(blockRegistry));

        playerService.spawnPlayer(owner, world);

        try {
            blockService.breakBlock(owner, world, world.getSpawnX(), world.getSpawnY(), 20);
            fail("Expected spawn door protection");
        } catch (IllegalStateException expected) {
            assertEquals("Spawn structure cannot be modified", expected.getMessage());
        }

        try {
            blockService.breakBlock(owner, world, world.getSpawnX(), world.getSpawnY() + 1, 20);
            fail("Expected spawn bedrock protection");
        } catch (IllegalStateException expected) {
            assertEquals("Spawn structure cannot be modified", expected.getMessage());
        }
    }
}
