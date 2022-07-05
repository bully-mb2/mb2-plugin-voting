package com.templars_server;

import com.templars_server.commands.*;
import com.templars_server.model.Context;
import com.templars_server.model.GameMap;
import com.templars_server.model.Player;
import com.templars_server.util.command.Command;
import com.templars_server.util.command.InvalidArgumentException;
import com.templars_server.util.rcon.RconClient;
import generated.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Voting {

    public static final String PREFIX = "^2Vote » ^7";
    public static final int DEFAULT_MAX_ROUNDS = 20;
    private static final Logger LOG = LoggerFactory.getLogger(Voting.class);
    private static final int PAGE_SIZE = 24;

    private final Context context;
    private final RconClient rcon;
    private final List<Command<Context>> commands;
    private final List<Command<Context>> adminCommands;

    public Voting(Context context, RconClient rcon) {
        this.context = context;
        this.rcon = rcon;
        this.commands = new ArrayList<>();
        this.adminCommands = new ArrayList<>();
    }

    public void setup() {
        LOG.info("Setting up commands:");
        commands.clear();
        commands.add(new NominateCommand());
        commands.add(new RtmCommand());
        commands.add(new RtvCommand());
        commands.add(new MapListCommand(PAGE_SIZE));
        commands.add(new SearchCommand(PAGE_SIZE));
        commands.add(new VoteCommand());
        for (Command<Context> command : commands) {
            LOG.info("    - " + command.getClass().getSimpleName());
        }

        LOG.info("Setting up admin commands:");
        adminCommands.clear();
        adminCommands.add(new PollCommand());
        adminCommands.add(new ForceRTVCommand());
        for (Command<Context> command : adminCommands) {
            LOG.info("    - " + command.getClass().getSimpleName());
        }

        LOG.info("Setting cvar and announcing it has been launched");
        String version = getClass().getPackage().getImplementationVersion();
        if (version == null) {
            version = "dev";
        }

        rcon.send("sets RTVRTM 3807/" + version);
        rcon.printAll(PREFIX + "Voting is now enabled");
    }

    void onClientConnectEvent(ClientConnectEvent event) {
        putPlayer(event.getSlot(), event.getName());
        LOG.debug("Player " + event.getSlot() + " connected");
    }

    void onClientDisconnectEvent(ClientDisconnectEvent event) {
        context.getPlayers().remove(event.getSlot());
        LOG.debug("Player " + event.getSlot() + " disconnected");
    }

    void onServerInitializationEvent(ServerInitializationEvent event) {
        context.reset();
        LOG.debug("Players and Nominations cleared");
    }

    void onInitGameEvent(InitGameEvent event) {
        GameMap gameMap = context.getMaps().get(event.getMapName());
        if (gameMap == null) {
            gameMap = new GameMap(event.getMapName(), DEFAULT_MAX_ROUNDS);
        }

        context.setCurrentMap(gameMap);
        GameMap nextMap = context.getNextMap();
        if (nextMap != null) {
            rcon.send("map \"" + nextMap.getName() +"\"");
            context.setNextMap(null);
        }

        LOG.debug("Map registered " + context.getCurrentMap());
    }

    void onShutdownGameEvent(ShutdownGameEvent event) {
        GameMap nextMap = context.getNextMap();
        if (nextMap != null) {
            rcon.send("map \"" + nextMap.getName() +"\"");
            context.setNextMap(null);
        }

        context.addRounds(1);
        if (context.getRound() > context.getCurrentMap().getMaxRounds()) {
            rcon.printAll(PREFIX + "Round limit reached");
            RtvCommand.startVote(context, rcon);
        }
    }

    void onAdminSayEvent(AdminSayEvent event) {
        String message = event.getMessage();
        for (Command<Context> command : adminCommands) {
            try {
                if (command.execute(-1, message, context)) {
                    break;
                }
            } catch (InvalidArgumentException e) {
                rcon.printAll(PREFIX + command.getUsage());
            } catch (Exception e) {
                LOG.error("Uncaught exception during command execution", e);
            }
        }
    }

    void onSayEvent(SayEvent event) {
        if (event.getChatChannel() != ChatChannel.SAY) {
            return;
        }

        putPlayer(event.getSlot(), event.getName());

        String message = event.getMessage();
        for (Command<Context> command : commands) {
            try {
                if (command.execute(event.getSlot(), message, context)) {
                    break;
                }
            } catch (InvalidArgumentException e) {
                rcon.print(event.getSlot(), PREFIX + command.getUsage());
            } catch (Exception e) {
                LOG.error("Uncaught exception during command execution", e);
            }
        }
    }

    private void putPlayer(int slot, String name) {
        Player player = context.getPlayers().get(slot);
        if (player == null || player.getName() == null) {
            context.getPlayers().put(slot, new Player(slot, name));
            LOG.debug("Player " + slot + " inserted");
        }
    }

}
