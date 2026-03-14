package com.example.block;

import java.lang.ProcessBuilder.Redirect.Type;
import java.util.Optional;

public class BlockDefinition {

    private int id;
    private String name;
    private boolean solid;
    private boolean breakable;
    private String dropEntity;
    private float chanceDropSeed;
    private Integer health;
    private Integer protectionRadius;
    private Integer maximumDiamondDrop;
    private Optional<BlockType> type;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isSolid() {
        return solid;
    }

    public boolean isBreakable() {
        return breakable;
    }

    public BlockType getType(){
        return type.orElse(BlockType.NORMAL);
    }

    public String getDropEntity() {
        return dropEntity;
    }

    public float getChanceDropSeed() {
        return chanceDropSeed;
    }

    public int getHealth() {
        return health == null ? 0 : health;
    }

    public int getProtectionRadius() {
        return protectionRadius == null ? 0 : protectionRadius;
    }

    public int getMaximumDiamondDrop() {
        return maximumDiamondDrop == null ? 0 : maximumDiamondDrop;
    }
}
