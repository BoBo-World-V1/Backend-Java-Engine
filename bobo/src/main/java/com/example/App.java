package com.example;

import com.example.block.BlockService;
import com.example.block.BlockRegistry;
import com.example.block.behavior.BehaviorRegistry;
import com.example.block.behavior.DefaultBehaviorRegistry;
import com.example.entity.EntityRegistry;
import com.example.player.Player;
import com.example.player.PlayerService;
import com.example.world.World;
import com.example.world.WorldGenerator;
import com.example.world.WorldManager;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final int DEFAULT_WORLD_WIDTH = 100;
    private static final int DEFAULT_WORLD_HEIGHT = 100;

    public static BlockRegistry BLOCKS;
    public static void main( String[] args )
    {
        BLOCKS = new BlockRegistry();
        BLOCKS.load();
        EntityRegistry entities = new EntityRegistry();
        entities.load();

        BehaviorRegistry behaviorRegistry = DefaultBehaviorRegistry.create(BLOCKS);
        PlayerService playerService = new PlayerService(behaviorRegistry);
        WorldManager worldManager = new WorldManager(new WorldGenerator(), playerService, behaviorRegistry);
        BlockService blockService = new BlockService(behaviorRegistry, BLOCKS, entities);

        World world = worldManager.createWorld("starter-world", DEFAULT_WORLD_WIDTH, DEFAULT_WORLD_HEIGHT);
        Player player = new Player(1, "builder");
         Player player1 = new Player(2, "builders");
        worldManager.connectPlayer(player, world.getName(), world.getWidth(), world.getHeight());
        worldManager.connectPlayer(player1, world.getName(), world.getWidth(), world.getHeight());
        playerService.setMovementInput(player, 1.0f);
        worldManager.tick(0.1f);
        blockService.breakBlock(player, world, world.getSpawnX(), world.getSpawnY() + 1, 20);

        System.out.println("World '" + world.getName() + "' ready with " + world.getPlayers().size() + " connected player(s).");
    }
}
