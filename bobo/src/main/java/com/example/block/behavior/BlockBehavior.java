package com.example.block.behavior;

import com.example.block.BlockDefinition;
import com.example.player.Player;
import com.example.world.World;

public interface BlockBehavior {
    default void onPLaced(World world, int x, int y, Player player) {};
    default void onBroken(World world, int x, int y, Player player) {};
    default void onBroken(World world, int x, int y, Player player, BlockDefinition block) {
        onBroken(world, x, y, player);
    };
    default void onNeighBorChanged(World world, int x, int y, Player player) {};
    default void onPlayerTouch(World world, int x, int y, Player player) {};
    default void onTick(World world, int x, int y){};
    default int getBreakHealth(World world, int x, int y, BlockDefinition block) { return block.getHealth(); };
    default boolean usesCustomDrops() { return false; };
}
