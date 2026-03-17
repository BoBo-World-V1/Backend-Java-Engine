package com.example.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EntityRegistry {
    private final Map<String, EntityDefinition> entities = new HashMap<>();

    private static class EntityFile {
        public EntityDefinition[] entities;
    }

    public void load() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream stream = getClass().getClassLoader().getResourceAsStream("entities.json");
            EntityFile entityFile = mapper.readValue(stream, EntityFile.class);

            for (EntityDefinition entity : entityFile.entities) {
                entities.put(entity.getId(), entity);
            }
            System.out.println("Loaded " + entities.size() + " entities");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load entity data from entities.json", e);
        }
    }

    public EntityDefinition get(String entityId) {
        return entities.get(entityId);
    }

    public boolean has(String entityId) {
        return entities.containsKey(entityId);
    }
}
