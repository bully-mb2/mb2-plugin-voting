package com.templars_server.model;

public enum GameMode {


    OPEN(0, "open", "Open"),
    SEMI_AUTHENTIC(1, "sa", "Semi-Authentic"),
    FULL_AUTHENTIC(2, "fa", "Full-Authentic"),
    DUEL(3, "duel", "Duel"),
    LEGENDS(4, "legends", "Legends");

    private final int id;
    private final String key;
    private final String display;

    GameMode(int id, String key, String display) {
        this.id = id;
        this.key = key;
        this.display = display;
    }

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getDisplay() {
        return display;
    }

    public static GameMode fromValue(String value) {
        try {
            return fromId(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return fromKey(value);
        }
    }

    public static GameMode fromId(int id) {
        for (GameMode mode : GameMode.values()) {
            if (mode.getId() == id) {
                return mode;
            }
        }

        return null;
    }

    public static GameMode fromKey(String key) {
        if (key == null) {
            return null;
        }

        for (GameMode mode : GameMode.values()) {
            if (mode.getKey().equalsIgnoreCase(key) || mode.getDisplay().equalsIgnoreCase(key)) {
                return mode;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "GameMode{" +
                "id=" + id +
                ", key='" + key + '\'' +
                '}';
    }

}
