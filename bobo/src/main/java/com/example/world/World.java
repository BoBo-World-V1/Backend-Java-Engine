package com.example.world;
import com.example.player.Player;
import java.util.ArrayList;
import java.util.List;

public class World {
    private final String name; 
    private final int width; 
    private final int height;

    private final Tile[][] tiles;

    private final List<Player> players = new ArrayList<>();
    
    public World (String name, int width, int height){
        this.name = name;
        this.width = width;
        this.height = height;

        this.tiles = new Tile[height][width];
        initializeTiles();

    }
    private boolean dirty;
    
    private void initializeTiles() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = new Tile();
            }
        }
    }

   public Tile getTile(int x, int y) {
        if (!isInBounds(x, y)) {
            throw new IllegalArgumentException("Out of bounds: " + x + "," + y);
        }

        return tiles[y][x];
    }

    public void setForeground(int x, int y, int blockId) {
        getTile(x, y).setForeground(blockId);
        dirty = true;
    }

    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < width &&
               y >= 0 && y < height;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public String getName() {
        return name;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markDirty() {
        dirty = true;
    }

    public void clearDirty() {
        dirty = false;
    }
}
