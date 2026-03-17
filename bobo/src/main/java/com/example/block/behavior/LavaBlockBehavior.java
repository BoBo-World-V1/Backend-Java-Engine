package com.example.block.behavior;

import com.example.player.Player;
import com.example.world.World;

public class LavaBlockBehavior implements BlockBehavior {
    public static final int LAVA_BLOCK_ID = 4;
    private static final int TOUCH_DAMAGE = 10;
    private static final float HORIZONTAL_KNOCKBACK = 5.0f;
    private static final float VERTICAL_KNOCKBACK = 3.5f;

    @Override
    public void onPlayerTouch(World world, int x, int y, Player player) {
        if (player.getHazardCooldownSeconds() > 0.0f) {
            return;
        }

        player.applyDamage(TOUCH_DAMAGE);
        player.setHazardCooldownSeconds(0.5f);

        float lavaCenterX = x + 0.5f;
        float lavaCenterY = y + 0.5f;
        float directionX = player.getX() >= lavaCenterX ? 1.0f : -1.0f;
        float directionY = player.getY() >= lavaCenterY ? 1.0f : -1.0f;

        player.setVelocityX(directionX * HORIZONTAL_KNOCKBACK);
        player.setVelocityY(directionY < 0.0f ? -VERTICAL_KNOCKBACK : VERTICAL_KNOCKBACK);
    }
}
