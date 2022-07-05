package com.templars_server;

import com.templars_server.util.rcon.RconClient;
import com.templars_server.voting.Vote;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context {

    private static final int MAX_MESSAGE_LENGTH = 118;
    private final RconClient rconClient;
    private final List<String> maps;
    private final Map<Integer, Player> players;
    private String currentMap;
    private String nextMap;
    private Vote vote;

    public Context(RconClient rconClient, List<String> maps) {
        this.rconClient = rconClient;
        this.maps = maps;
        this.players = new HashMap<>();
        this.currentMap = "";
        this.nextMap = "";
    }

    public RconClient getRconClient() {
        return rconClient;
    }

    public List<String> getMaps() {
        return maps;
    }

    public Map<Integer, Player> getPlayers() {
        return players;
    }

    public String getCurrentMap() {
        return currentMap;
    }

    public void setCurrentMap(String currentMap) {
        this.currentMap = currentMap;
    }

    public String getNextMap() {
        return nextMap;
    }

    public void setNextMap(String nextMap) {
        this.nextMap = nextMap;
    }

    public Vote getVote() {
        return vote;
    }

    public void setVote(Vote vote) {
        if (isVoting()) {
            this.vote.cancel();
        }

        this.vote = vote;
    }

    public boolean isVoting() {
        return vote != null && vote.isAlive();
    }

    public void reset() {
        players.clear();
        if (vote != null) {
            vote.cancel();
            vote = null;
        }
        currentMap = "";
        nextMap = "";
    }

    // TODO :: Very unhappy about this placement but I can't be bothered to move it somewhere less common
    public void printMaps(int slot, RconClient rcon, List<String> mapList, String prefix) {
        StringBuilder builder = new StringBuilder();
        for (String map : mapList) {
            map = map + ", ";
            if (builder.length() + map.length() > MAX_MESSAGE_LENGTH - prefix.length()) {
                rcon.print(slot, prefix + builder.substring(0, builder.length() - 2));
                builder = new StringBuilder();
            }

            builder.append(map);
        }

        if (builder.length() > 0) {
            rcon.print(slot, prefix + builder.substring(0, builder.length() - 2));
        }
    }

}
