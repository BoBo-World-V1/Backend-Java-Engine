package com.example.entity;

public class WorldDrop {
    private final String entityId;
    private final float x;
    private final float y;
    private final int amount;

    public WorldDrop(String entityId, float x, float y, int amount) {
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.amount = amount;
    }

    public String getEntityId() {
        return entityId;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getAmount() {
        return amount;
    }
}
