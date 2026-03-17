package com.example.block;

public class BlockDefinition {

    private int id;
    private String name;
    private boolean solid;
    private boolean breakable;
    private String dropEntity;
    private String dropSeedEntity;
    private float chanceDropSeed;
    private Integer health;
    private Integer protectionRadius;
    private Integer maximumDiamondDrop;
    private Integer maximumHarvestDropAmount;
    private Integer growthTicksRequired;
    private BlockType type;

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
        return type == null ? BlockType.NORMAL : type;
    }

    public String getDropEntity() {
        return dropEntity;
    }

    public float getChanceDropSeed() {
        return chanceDropSeed;
    }

    public String getDropSeedEntity() {
        return dropSeedEntity;
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

    public int getMaximumHarvestDropAmount() {
        return maximumHarvestDropAmount == null ? 1 : Math.max(1, maximumHarvestDropAmount);
    }

    public int getGrowthTicksRequired() {
        return growthTicksRequired == null ? 3 : Math.max(1, growthTicksRequired);
    }
}
