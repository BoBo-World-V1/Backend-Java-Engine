package com.example;

import com.example.block.BlockRegistry;

/**
 * Hello world!
 *
 */
public class App 
{

    public static BlockRegistry BLOCKS;
    public static void main( String[] args )
    {
        BLOCKS = new BlockRegistry();
        BLOCKS.load();
        System.out.println( "Hello World!" );
    }
}
