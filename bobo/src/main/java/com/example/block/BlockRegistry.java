package com.example.block;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;


public class BlockRegistry {
    private final Map<Integer, BlockDefinition> blocks = new HashMap<>();

    private static class BlockFile {
        public BlockDefinition[] blocks;
    }
    public void load(){
        try{
            ObjectMapper mapper = new ObjectMapper();

            InputStream stream = getClass().getClassLoader().getResourceAsStream("blocks.json");

            BlockFile blockFile = mapper.readValue(stream, BlockFile.class);

            for(BlockDefinition block : blockFile.blocks){
                blocks.put(block.getId(), block);
            }
            System.out.println("Loaded " + blocks.size() + " blocks");
        }
        catch (Exception e){
            throw new RuntimeException("Failed to Load Block Data from blocks.json", e);
        }
    }


    public BlockDefinition get(int BlockId){
        return blocks.get(BlockId);
    }


    
}
