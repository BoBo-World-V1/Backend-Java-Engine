package com.example.world;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AreaLock {
    private final int originX;
    private final int originY;
    private final int ownerId;
    private final int radius;
    private final int blockId;
    private final Set<Integer> admins = new HashSet<>();

    public AreaLock(int originX, int originY, int ownerId, int radius, int blockId) {
        this.originX = originX;
        this.originY = originY;
        this.ownerId = ownerId;
        this.radius = radius;
        this.blockId = blockId;
    }

    public int getOriginX() {
        return originX;
    }

    public int getOriginY() {
        return originY;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getRadius() {
        return radius;
    }

    public int getBlockId() {
        return blockId;
    }

    public boolean isOwner(int playerId) {
        return ownerId == playerId;
    }

    public boolean isAdmin(int playerId) {
        return isOwner(playerId) || admins.contains(playerId);
    }

    public Set<Integer> getAdmins() {
        return Collections.unmodifiableSet(admins);
    }

    public void grantAdmin(int ownerId, int targetPlayerId) {
        if (!isOwner(ownerId)) {
            throw new IllegalStateException("Only the area owner can grant admin access");
        }
        if (targetPlayerId == ownerId) {
            return;
        }
        admins.add(targetPlayerId);
    }

    public void revokeAdmin(int ownerId, int targetPlayerId) {
        if (!isOwner(ownerId)) {
            throw new IllegalStateException("Only the area owner can revoke admin access");
        }
        admins.remove(targetPlayerId);
    }

    public boolean covers(int x, int y) {
        return x >= originX - radius && x <= originX + radius
                && y >= originY - radius && y <= originY + radius;
    }
}
