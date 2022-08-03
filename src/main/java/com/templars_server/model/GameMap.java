package com.templars_server.model;

public class GameMap {

    private final String name;
    private final int maxRounds;
    private int cooldown;

    public GameMap(String name, int maxRounds) {
        this.name = name;
        this.maxRounds = maxRounds;
        this.cooldown = 0;
    }

    public String getName() {
        return name;
    }

    public int getMaxRounds() {
        return maxRounds;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

}
