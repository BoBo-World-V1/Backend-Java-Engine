package com.example.player;
import com.example.world.World;


public class Player {

    private final int id;
    private final String username;

    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private float horizontalInput;
    private boolean jumpQueued;
    private boolean onGround;

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

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }
}
