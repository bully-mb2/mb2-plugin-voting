package com.templars_server.model;

import com.templars_server.Voting;
import com.templars_server.util.rcon.RconClient;
import com.templars_server.voting.Vote;

import java.util.Map;

public class Context {

    private static final GameMap INIT_MAP = new GameMap("Voting restarted, loading...", 99);

    private final RconClient rconClient;
    private final PlayerList players;
    private final int defaultCooldown;
    private Map<String, GameMap> maps;
    private int round;
    private GameMap currentMap;
    private GameMap nextMap;
    private Vote vote;

    public Context(RconClient rconClient, Map<String, GameMap> maps, int defaultCooldown) {
        this.rconClient = rconClient;
        this.maps = maps;
        this.players = new PlayerList();
        this.defaultCooldown = defaultCooldown;
        this.round = 0;
        this.currentMap = INIT_MAP;
        this.nextMap = null;
    }

    public RconClient getRconClient() {
        return rconClient;
    }

    public Map<String, GameMap> getMaps() {
        return maps;
    }

    public void setMaps(Map<String, GameMap> maps) {
        this.maps = maps;
    }

    public PlayerList getPlayers() {
        return players;
    }

    public int getDefaultCooldown() {
        return defaultCooldown;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public void addRounds(int round) {
        this.round += round;
    }

    public GameMap getMapByName(String mapName) {
        return getMaps().getOrDefault(
                mapName,
                new GameMap(mapName, Voting.DEFAULT_MAX_ROUNDS)
        );
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
        currentMap = INIT_MAP;
        nextMap = null;
    }

}
