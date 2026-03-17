package com.example.world;
import com.example.player.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class World {
    public static final int AIR_BLOCK_ID = 0;

    private final String name; 
    private final int width; 
    private final int height;

    private final Tile[][] tiles;

    private final List<Player> players = new ArrayList<>();
    private final List<AreaLock> areaLocks = new ArrayList<>();
    private final Set<Integer> worldAdmins = new HashSet<>();
    private int spawnX;
    private int spawnY;
    private Integer spawnDoorX;
    private Integer spawnDoorY;
    private Integer spawnBedrockX;
    private Integer spawnBedrockY;
    private long tick;
    private Integer worldOwnerId;
    private Integer worldLockX;
    private Integer worldLockY;
    
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
        Tile tile = getTile(x, y);
        tile.setForeground(blockId);
        tile.setDamage(0);
        dirty = true;
    }

    public void setBackground(int x, int y, int blockId) {
        getTile(x, y).setBackground(blockId);
        dirty = true;
    }

    public boolean isSolid(int x, int y) {
        return isInBounds(x, y) && getTile(x, y).getForeground() != AIR_BLOCK_ID;
    }

    public boolean isWalkable(int x, int y) {
        return isInBounds(x, y) && !isSolid(x, y) && getPlayerAt(x, y) == null;
    }

    public boolean isSolidAt(float x, float y) {
        int tileX = (int) Math.floor(x);
        int tileY = (int) Math.floor(y);
        return !isInBounds(tileX, tileY) || isSolid(tileX, tileY);
    }

    public Player getPlayerAt(int x, int y) {
        for (Player player : players) {
            if ((int) player.getX() == x && (int) player.getY() == y) {
                return player;
            }
        }
        return null;
    }

    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < width &&
               y >= 0 && y < height;
    }

    public void addPlayer(Player player) {
        if (!players.contains(player)) {
            players.add(player);
            dirty = true;
        }
    }

    public void removePlayer(Player player) {
        if (players.remove(player)) {
            dirty = true;
        }
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public List<AreaLock> getAreaLocks() {
        return Collections.unmodifiableList(areaLocks);
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

    public int getSpawnX() {
        return spawnX;
    }

    public int getSpawnY() {
        return spawnY;
    }

    public void setSpawnPoint(int spawnX, int spawnY) {
        if (!isInBounds(spawnX, spawnY)) {
            throw new IllegalArgumentException("Spawn point is out of bounds");
        }
        this.spawnX = spawnX;
        this.spawnY = spawnY;
    }

    public void setSpawnStructure(int doorX, int doorY, int bedrockX, int bedrockY) {
        if (!isInBounds(doorX, doorY) || !isInBounds(bedrockX, bedrockY)) {
            throw new IllegalArgumentException("Spawn structure is out of bounds");
        }
        this.spawnDoorX = doorX;
        this.spawnDoorY = doorY;
        this.spawnBedrockX = bedrockX;
        this.spawnBedrockY = bedrockY;
    }

    public boolean isSpawnDoor(int x, int y) {
        return spawnDoorX != null && spawnDoorY != null && spawnDoorX == x && spawnDoorY == y;
    }

    public boolean isSpawnBedrock(int x, int y) {
        return spawnBedrockX != null && spawnBedrockY != null && spawnBedrockX == x && spawnBedrockY == y;
    }

    public boolean isSpawnStructure(int x, int y) {
        return isSpawnDoor(x, y) || isSpawnBedrock(x, y);
    }

    public boolean hasWorldOwner() {
        return worldOwnerId != null;
    }

    public Integer getWorldOwnerId() {
        return worldOwnerId;
    }

    public boolean isWorldOwner(int playerId) {
        return worldOwnerId != null && worldOwnerId == playerId;
    }

    public boolean isWorldAdmin(int playerId) {
        return isWorldOwner(playerId) || worldAdmins.contains(playerId);
    }

    public Set<Integer> getWorldAdmins() {
        return Collections.unmodifiableSet(worldAdmins);
    }

    public void setWorldOwner(int playerId, int lockX, int lockY) {
        this.worldOwnerId = playerId;
        this.worldLockX = lockX;
        this.worldLockY = lockY;
        this.worldAdmins.clear();
        dirty = true;
    }

    public void clearWorldOwner() {
        this.worldOwnerId = null;
        this.worldLockX = null;
        this.worldLockY = null;
        this.worldAdmins.clear();
        dirty = true;
    }

    public void grantWorldAdmin(int ownerId, int targetPlayerId) {
        if (!isWorldOwner(ownerId)) {
            throw new IllegalStateException("Only the world owner can grant admin access");
        }
        if (targetPlayerId == ownerId) {
            return;
        }
        worldAdmins.add(targetPlayerId);
        dirty = true;
    }

    public void revokeWorldAdmin(int ownerId, int targetPlayerId) {
        if (!isWorldOwner(ownerId)) {
            throw new IllegalStateException("Only the world owner can revoke admin access");
        }
        if (worldAdmins.remove(targetPlayerId)) {
            dirty = true;
        }
    }

    public boolean isWorldLock(int x, int y) {
        return worldLockX != null && worldLockY != null && worldLockX == x && worldLockY == y;
    }

    public void addAreaLock(AreaLock areaLock) {
        areaLocks.add(areaLock);
        dirty = true;
    }

    public Optional<AreaLock> getAreaLockAt(int x, int y) {
        for (AreaLock areaLock : areaLocks) {
            if (areaLock.getOriginX() == x && areaLock.getOriginY() == y) {
                return Optional.of(areaLock);
            }
        }
        return Optional.empty();
    }

    public void grantAreaAdmin(int ownerId, int lockX, int lockY, int targetPlayerId) {
        AreaLock areaLock = getAreaLockAt(lockX, lockY)
                .orElseThrow(() -> new IllegalStateException("Area lock not found"));
        areaLock.grantAdmin(ownerId, targetPlayerId);
        dirty = true;
    }

    public void revokeAreaAdmin(int ownerId, int lockX, int lockY, int targetPlayerId) {
        AreaLock areaLock = getAreaLockAt(lockX, lockY)
                .orElseThrow(() -> new IllegalStateException("Area lock not found"));
        areaLock.revokeAdmin(ownerId, targetPlayerId);
        dirty = true;
    }

    public void removeAreaLockAt(int x, int y) {
        areaLocks.removeIf(areaLock -> areaLock.getOriginX() == x && areaLock.getOriginY() == y);
        dirty = true;
    }

    public Optional<AreaLock> getProtectingAreaLockAt(int x, int y) {
        for (AreaLock areaLock : areaLocks) {
            if (areaLock.covers(x, y)) {
                return Optional.of(areaLock);
            }
        }
        return Optional.empty();
    }

    public boolean isDirty() {
        return dirty;
    }

    public long getTick() {
        return tick;
    }

    public void advanceTick() {
        tick++;
        dirty = true;
    }

    public void markDirty() {
        dirty = true;
    }

    public void clearDirty() {
        dirty = false;
    }
}
