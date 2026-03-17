package com.example.world;

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

    public WorldManager(WorldGenerator worldGenerator, PlayerService playerService) {
        this.worldGenerator = worldGenerator;
        this.playerService = playerService;
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
            world.advanceTick();
        }
    }
}
