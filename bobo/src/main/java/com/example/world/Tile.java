package com.example.world;

public class Tile {
    private int foreground;
    private int background; 
    private int damage; 
    private int flags;
    

    public Tile(){
        this.foreground = 0;
        this.background = 0; 
        this.damage = 0; 
        this.flags = 0;
    }

    public int getForeground() {
        return foreground;
    }

    public void setForeground(int foreground) {
        this.foreground = foreground;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }
}
