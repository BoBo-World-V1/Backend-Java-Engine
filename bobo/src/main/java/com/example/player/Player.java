package com.example.player;
import com.example.world.World;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class Player {
    public static final int MAX_HEALTH = 100;
    private static final float FULL_HEAL_DELAY_SECONDS = 5.0f;


    private final int id;
    private final String username;

    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private float horizontalInput;
    private float hazardCooldownSeconds;
    private float secondsSinceDamage = FULL_HEAL_DELAY_SECONDS;
    private boolean jumpQueued;
    private boolean onGround;
    private int health = MAX_HEALTH;
    private int diamondCount;
    private final Map<String, Integer> inventory = new HashMap<>();

    private World world;

    public Player(int id, String username) {
        this.id = id;
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(float velocityX) {
        this.velocityX = velocityX;
    }

    public float getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(float velocityY) {
        this.velocityY = velocityY;
    }

    public float getHorizontalInput() {
        return horizontalInput;
    }

    public void setHorizontalInput(float horizontalInput) {
        this.horizontalInput = horizontalInput;
    }

    public float getHazardCooldownSeconds() {
        return hazardCooldownSeconds;
    }

    public void setHazardCooldownSeconds(float hazardCooldownSeconds) {
        this.hazardCooldownSeconds = Math.max(0.0f, hazardCooldownSeconds);
    }

    public float getSecondsSinceDamage() {
        return secondsSinceDamage;
    }

    public void setSecondsSinceDamage(float secondsSinceDamage) {
        this.secondsSinceDamage = Math.max(0.0f, secondsSinceDamage);
    }

    public boolean isJumpQueued() {
        return jumpQueued;
    }

    public void setJumpQueued(boolean jumpQueued) {
        this.jumpQueued = jumpQueued;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(MAX_HEALTH, health));
    }

    public int getDiamondCount() {
        return diamondCount;
    }

    public void setDiamondCount(int diamondCount) {
        this.diamondCount = Math.max(0, diamondCount);
    }

    public Map<String, Integer> getInventory() {
        return Collections.unmodifiableMap(inventory);
    }

    public int getItemCount(String entityId) {
        return inventory.getOrDefault(entityId, 0);
    }

    public void addItem(String entityId, int amount) {
        if (amount <= 0) {
            return;
        }
        inventory.merge(entityId, amount, Integer::sum);
    }

    public void applyDamage(int damage) {
        if (damage <= 0 || health == 0) {
            return;
        }

        setHealth(health - damage);
        secondsSinceDamage = 0.0f;

        if (health == 0) {
            loseDiamondsOnDeath();
        }
    }

    public void tickRecovery(float deltaSeconds) {
        secondsSinceDamage += Math.max(0.0f, deltaSeconds);
        if (secondsSinceDamage >= FULL_HEAL_DELAY_SECONDS && health < MAX_HEALTH) {
            setHealth(MAX_HEALTH);
        }
    }

    private void loseDiamondsOnDeath() {
        if (diamondCount <= 0) {
            return;
        }

        int diamondsLost = Math.max(1, (int) Math.ceil(diamondCount * 0.10));
        diamondCount = Math.max(0, diamondCount - diamondsLost);
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }
}
