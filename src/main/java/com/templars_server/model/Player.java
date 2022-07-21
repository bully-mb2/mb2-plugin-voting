package com.templars_server.model;

public class Player {

    private final int slot;
    private int vote;
    private boolean rtv;
    private boolean rtm;
    private String nomination;
    private String name;

    public Player(int slot, String name) {
        this.slot = slot;
        this.name = name;
        this.rtv = false;
        this.rtm = false;
    }

    public int getSlot() {
        return slot;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVote() {
        return vote;
    }

    public void setVote(int vote) {
        this.vote = vote;
    }

    public boolean isRtv() {
        return rtv;
    }

    public void setRtv(boolean rtv) {
        this.rtv = rtv;
    }

    public boolean isRtm() {
        return rtm;
    }

    public void setRtm(boolean rtm) {
        this.rtm = rtm;
    }

    public String getNomination() {
        return nomination;
    }

    public void setNomination(String nomination) {
        this.nomination = nomination;
    }

}
