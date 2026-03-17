package com.example.world;

import com.example.block.behavior.BehaviorRegistry;
import com.example.block.behavior.BlockBehavior;
import com.example.player.Player;
import com.example.player.PlayerService;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldManager {
    private final Map<String, World> worlds = new ConcurrentHashMap<>();
    private final WorldGenerator worldGenerator;
    private final PlayerService playerService;
    private final BehaviorRegistry behaviorRegistry;

    public WorldManager(WorldGenerator worldGenerator, PlayerService playerService) {
        this(worldGenerator, playerService, null);
    }

    public WorldManager(WorldGenerator worldGenerator, PlayerService playerService, BehaviorRegistry behaviorRegistry) {
        this.worldGenerator = worldGenerator;
        this.playerService = playerService;
        this.behaviorRegistry = behaviorRegistry;
    }

    public World createWorld(String name, int width, int height) {
        World world = worldGenerator.generate(name, width, height);
        worlds.put(name, world);
        return world;
    }

    public World getOrCreateWorld(String name, int width, int height) {
        return worlds.computeIfAbsent(name, ignored -> worldGenerator.generate(name, width, height));
    }

    public World getWorld(String name) {
        return worlds.get(name);
    }

    public Collection<World> getWorlds() {
        return Collections.unmodifiableCollection(worlds.values());
    }

    public World connectPlayer(Player player, String worldName, int width, int height) {
        World world = getOrCreateWorld(worldName, width, height);
        if (player.getWorld() != null) {
            disconnectPlayer(player);
        }
        playerService.spawnPlayer(player, world);
        return world;
    }

    public void disconnectPlayer(Player player) {
        World world = player.getWorld();
        if (world == null) {
            return;
        }

        world.removePlayer(player);
        player.setWorld(null);
    }

    public void tick(float deltaSeconds) {
        for (World world : worlds.values()) {
            for (Player player : world.getPlayers()) {
                playerService.tickPlayer(player, deltaSeconds);
            }
            tickWorldBlocks(world);
            world.advanceTick();
        }
    }

    private void tickWorldBlocks(World world) {
        if (behaviorRegistry == null) {
            return;
        }

        for (int y = 0; y < world.getHeight(); y++) {
            for (int x = 0; x < world.getWidth(); x++) {
                int blockId = world.getTile(x, y).getForeground();
                BlockBehavior behavior = behaviorRegistry.get(blockId);
                if (behavior != null) {
                    behavior.onTick(world, x, y);
                }
            }
        }
    }
}
