package com.example.player;

import com.example.block.behavior.BehaviorRegistry;
import com.example.block.behavior.BlockBehavior;
import com.example.world.World;
import java.util.HashSet;
import java.util.Set;

public class PlayerService {
    private static final float SPAWN_OFFSET = 0.5f;
    private static final float PLAYER_HALF_WIDTH = 0.3f;
    private static final float PLAYER_HALF_HEIGHT = 0.45f;
    private static final float MAX_INPUT = 1.0f;
    private static final float MOVE_SPEED = 4.5f;
    private static final float GRAVITY = 18.0f;
    private static final float JUMP_VELOCITY = 8.0f;
    private static final float MAX_FALL_SPEED = 20.0f;
    private static final float MAX_POSITION_ERROR = 1.5f;
    private final BehaviorRegistry behaviorRegistry;

    public PlayerService() {
        this(new BehaviorRegistry());
    }

    public PlayerService(BehaviorRegistry behaviorRegistry) {
        this.behaviorRegistry = behaviorRegistry;
    }

    public void spawnPlayer(Player player, World world) {
        world.addPlayer(player);
        player.setWorld(world);
        respawnPlayer(player);
    }

    public boolean move(Player player, int deltaX, int deltaY) {
        return move(player, (float) deltaX, (float) deltaY);
    }

    public boolean move(Player player, float deltaX, float deltaY) {
        World world = requireWorld(player);
        boolean moved = false;
        float currentX = player.getX();
        float currentY = player.getY();
        float targetX = currentX + deltaX;
        float targetY = currentY + deltaY;

        if (deltaX != 0.0f && canOccupy(world, player, targetX, currentY)) {
            player.setPosition(targetX, currentY);
            currentX = targetX;
            moved = true;
        }

        if (deltaY != 0.0f && canOccupy(world, player, currentX, targetY)) {
            player.setPosition(currentX, targetY);
            moved = true;
        }

        if (moved) {
            applyTouchBehaviors(player, world);
            handleDeathIfNeeded(player, world);
            world.markDirty();
        }
        return moved;
    }

    public void setMovementInput(Player player, float horizontalInput) {
        requireWorld(player);
        player.setHorizontalInput(clamp(horizontalInput, -MAX_INPUT, MAX_INPUT));
    }

    public void queueJump(Player player) {
        requireWorld(player);
        player.setJumpQueued(true);
    }

    public boolean validatePosition(Player player, float requestedX, float requestedY) {
        World world = requireWorld(player);
        if (!canOccupy(world, player, requestedX, requestedY)) {
            return false;
        }

        float deltaX = requestedX - player.getX();
        float deltaY = requestedY - player.getY();
        return (deltaX * deltaX) + (deltaY * deltaY) <= MAX_POSITION_ERROR * MAX_POSITION_ERROR;
    }

    public void tickPlayer(Player player, float deltaSeconds) {
        World world = requireWorld(player);
        float dt = Math.max(0.0f, deltaSeconds);
        if (dt == 0.0f) {
            return;
        }

        float velocityX = player.getHorizontalInput() * MOVE_SPEED;
        float velocityY = player.getVelocityY();
        boolean onGround = player.isOnGround() || isGrounded(world, player.getX(), player.getY());
        player.setHazardCooldownSeconds(player.getHazardCooldownSeconds() - dt);
        player.tickRecovery(dt);

        if (player.isJumpQueued() && onGround) {
            velocityY = -JUMP_VELOCITY;
            onGround = false;
        }
        player.setJumpQueued(false);

        velocityY = Math.min(MAX_FALL_SPEED, velocityY + (GRAVITY * dt));

        float nextX = player.getX() + (velocityX * dt);
        float nextY = player.getY();

        if (canOccupy(world, player, nextX, nextY)) {
            player.setPosition(nextX, nextY);
        } else {
            velocityX = 0.0f;
        }

        nextY = player.getY() + (velocityY * dt);
        if (canOccupy(world, player, player.getX(), nextY)) {
            player.setPosition(player.getX(), nextY);
            onGround = false;
        } else {
            if (velocityY > 0.0f) {
                player.setPosition(player.getX(), alignToSurface(player.getY()));
                onGround = true;
            }
            velocityY = 0.0f;
        }

        player.setVelocityX(velocityX);
        player.setVelocityY(velocityY);
        player.setOnGround(onGround || isGrounded(world, player.getX(), player.getY()));
        applyTouchBehaviors(player, world);
        handleDeathIfNeeded(player, world);
        world.markDirty();
    }

    public boolean moveTo(Player player, int x, int y) {
        return moveTo(player, (float) x, (float) y);
    }

    public boolean moveTo(Player player, float x, float y) {
        World world = requireWorld(player);
        if (!canOccupy(world, player, x, y)) {
            return false;
        }

        player.setPosition(x, y);
        applyTouchBehaviors(player, world);
        handleDeathIfNeeded(player, world);
        world.markDirty();
        return true;
    }

    public void respawnPlayer(Player player) {
        World world = requireWorld(player);
        float worldX = world.getSpawnX() + SPAWN_OFFSET;
        float worldY = world.getSpawnY() + SPAWN_OFFSET;

        player.setPosition(worldX, worldY);
        player.setVelocityX(0.0f);
        player.setVelocityY(0.0f);
        player.setHorizontalInput(0.0f);
        player.setHazardCooldownSeconds(0.0f);
        player.setSecondsSinceDamage(5.0f);
        player.setJumpQueued(false);
        player.setOnGround(isGrounded(world, worldX, worldY));
        player.setHealth(Player.MAX_HEALTH);
    }

    private World requireWorld(Player player) {
        if (player.getWorld() == null) {
            throw new IllegalStateException("Player is not connected to a world");
        }
        return player.getWorld();
    }

    private boolean canOccupy(World world, Player player, float x, float y) {
        if (intersectsSolid(world, x, y)) {
            return false;
        }

        for (Player other : world.getPlayers()) {
            if (other == player) {
                continue;
            }
            if (Math.abs(other.getX() - x) < PLAYER_HALF_WIDTH * 2
                    && Math.abs(other.getY() - y) < PLAYER_HALF_HEIGHT * 2) {
                return false;
            }
        }

        return true;
    }

    private boolean isGrounded(World world, float x, float y) {
        return world.isSolidAt(x - PLAYER_HALF_WIDTH, y + PLAYER_HALF_HEIGHT + 0.06f)
                || world.isSolidAt(x + PLAYER_HALF_WIDTH, y + PLAYER_HALF_HEIGHT + 0.06f);
    }

    private float alignToSurface(float y) {
        return (float) Math.floor(y) + 0.5f;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private boolean intersectsSolid(World world, float x, float y) {
        return world.isSolidAt(x - PLAYER_HALF_WIDTH, y - PLAYER_HALF_HEIGHT)
                || world.isSolidAt(x + PLAYER_HALF_WIDTH, y - PLAYER_HALF_HEIGHT)
                || world.isSolidAt(x - PLAYER_HALF_WIDTH, y + PLAYER_HALF_HEIGHT)
                || world.isSolidAt(x + PLAYER_HALF_WIDTH, y + PLAYER_HALF_HEIGHT);
    }

    private float[] findSpawnPosition(World world, Player player, int spawnX, int spawnY) {
        for (int offset = 0; offset < world.getWidth(); offset++) {
            int[] candidates = offset == 0 ? new int[] { spawnX } : new int[] { spawnX - offset, spawnX + offset };
            for (int candidateX : candidates) {
                if (!world.isInBounds(candidateX, spawnY)) {
                    continue;
                }

                float worldX = candidateX + SPAWN_OFFSET;
                float worldY = spawnY + SPAWN_OFFSET;
                if (canOccupy(world, player, worldX, worldY)) {
                    return new float[] { worldX, worldY };
                }
            }
        }
        throw new IllegalStateException("Could not find an open spawn position");
    }

    private void applyTouchBehaviors(Player player, World world) {
        Set<String> touchedBlocks = new HashSet<>();
        touchBehaviorAt(world, player, player.getX() - PLAYER_HALF_WIDTH - 0.02f, player.getY(), touchedBlocks);
        touchBehaviorAt(world, player, player.getX() + PLAYER_HALF_WIDTH + 0.02f, player.getY(), touchedBlocks);
        touchBehaviorAt(world, player, player.getX(), player.getY() - PLAYER_HALF_HEIGHT - 0.02f, touchedBlocks);
        touchBehaviorAt(world, player, player.getX(), player.getY() + PLAYER_HALF_HEIGHT + 0.02f, touchedBlocks);
    }

    private void touchBehaviorAt(World world, Player player, float sampleX, float sampleY, Set<String> touchedBlocks) {
        int tileX = (int) Math.floor(sampleX);
        int tileY = (int) Math.floor(sampleY);
        if (!world.isInBounds(tileX, tileY)) {
            return;
        }

        String key = tileX + ":" + tileY;
        if (!touchedBlocks.add(key)) {
            return;
        }

        int blockId = world.getTile(tileX, tileY).getForeground();
        BlockBehavior behavior = behaviorRegistry.get(blockId);
        if (behavior != null) {
            behavior.onPlayerTouch(world, tileX, tileY, player);
        }
    }

    private void handleDeathIfNeeded(Player player, World world) {
        if (player.getHealth() > 0) {
            return;
        }
        respawnPlayer(player);
        world.markDirty();
    }
}
