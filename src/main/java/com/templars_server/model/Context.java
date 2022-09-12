package com.templars_server.model;

import com.templars_server.Voting;
import com.templars_server.util.rcon.RconClient;
import com.templars_server.voting.Vote;

import java.util.List;

public class Context {

    private static final GameMap INIT_MAP = new GameMap("Voting restarted, loading...", 99);

    private final RconClient rconClient;
    private final PlayerList players;
    private final int defaultCooldown;
    private final GameMode defaultGameMode;
    private final boolean rtvEnabled;
    private final boolean rtmEnabled;
    private final List<GameMode> rtmGameModes;
    private GameMapList maps;
    private int round;
    private GameMap currentMap;
    private GameMap nextMap;
    private GameMode currentGameMode;
    private GameMode nextGameMode;
    private Vote vote;

    public Context(
            RconClient rconClient,
            GameMapList maps,
            int defaultCooldown,
            GameMode defaultGameMode,
            boolean rtvEnabled,
            boolean rtmEnabled,
            List<GameMode> rtmGameModes
    ) {
        this.rconClient = rconClient;
        this.maps = maps;
        this.players = new PlayerList();
        this.defaultCooldown = defaultCooldown;
        this.defaultGameMode = defaultGameMode;
        this.rtvEnabled = rtvEnabled;
        this.rtmEnabled = rtmEnabled;
        this.rtmGameModes = rtmGameModes;
        this.round = 0;
        this.currentMap = INIT_MAP;
        this.nextMap = null;
        this.currentGameMode = defaultGameMode;
        this.nextGameMode = null;
    }

    public RconClient getRconClient() {
        return rconClient;
    }

    public GameMapList getMaps() {
        return maps;
    }

    public void setMaps(GameMapList maps) {
        this.maps = maps;
    }

    public PlayerList getPlayers() {
        return players;
    }

    public boolean isRtvEnabled() {
        return rtvEnabled;
    }

    public boolean isRtmEnabled() {
        return rtmEnabled;
    }

    public List<GameMode> getRtmGameModes() {
        return rtmGameModes;
    }

    public int getDefaultCooldown() {
        return defaultCooldown;
    }

    public GameMode getDefaultGameMode() {
        return defaultGameMode;
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

    public GameMode getCurrentGameMode() {
        return currentGameMode;
    }

    public void setCurrentGameMode(GameMode currentGameMode) {
        this.currentGameMode = currentGameMode;
    }

    public GameMode getNextGameMode() {
        return nextGameMode;
    }

    public void setNextGameMode(GameMode nextGameMode) {
        this.nextGameMode = nextGameMode;
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
        nextGameMode = null;
    }

}
