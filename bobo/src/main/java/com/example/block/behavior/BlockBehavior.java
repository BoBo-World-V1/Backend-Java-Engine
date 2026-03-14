package com.example.block.behavior;

import com.example.player.Player;
import com.example.world.World;

public interface BlockBehavior {
    default void onPLaced(World world, int x, int y, Player player) {};
    default void onBroken(World world, int x, int y, Player player) {};
    default void onNeighBorChanged(World world, int x, int y, Player player) {};
    default void onTick(World world, int x, int y){};
}
