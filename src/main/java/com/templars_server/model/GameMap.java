package com.templars_server.model;

public class GameMap {

    private final String name;
    private final int maxRounds;

    public GameMap(String name, int maxRounds) {
        this.name = name;
        this.maxRounds = maxRounds;
    }

    public String getName() {
        return name;
    }

    public int getMaxRounds() {
        return maxRounds;
    }

}
