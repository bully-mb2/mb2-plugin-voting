package com.templars_server.model;

import com.templars_server.util.rcon.RconClient;
import com.templars_server.voting.Vote;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context {

    private static final int MAX_MESSAGE_LENGTH = 118;
    private final RconClient rconClient;
    private final Map<String, GameMap> maps;
    private final Map<Integer, Player> players;
    private int round;
    private GameMap currentMap;
    private GameMap nextMap;
    private Vote vote;

    public Context(RconClient rconClient, Map<String, GameMap> maps) {
        this.rconClient = rconClient;
        this.maps = maps;
        this.players = new HashMap<>();
        this.round = 0;
        this.currentMap = null;
        this.nextMap = null;
    }

    public RconClient getRconClient() {
        return rconClient;
    }

    public Map<String, GameMap> getMaps() {
        return maps;
    }

    public Map<Integer, Player> getPlayers() {
        return players;
    }

    public int getRound() {
        return round;
    }

    public void addRounds(int round) {
        this.round += round;
    }

    public GameMap getCurrentMap() {
        return currentMap;
    }

    public void setCurrentMap(GameMap currentMap) {
        this.currentMap = currentMap;
    }

    public GameMap getNextMap() {
        return nextMap;
    }

    public void setNextMap(GameMap nextMap) {
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

        round = 0;
        currentMap = null;
        nextMap = null;
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
