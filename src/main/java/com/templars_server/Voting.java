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
        adminCommands.add(new ForceRtvCommand());
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
    }

    void onClientDisconnectEvent(ClientDisconnectEvent event) {
        context.getPlayers().remove(event.getSlot());
    }

    void onServerInitializationEvent(ServerInitializationEvent event) {
        LOG.info("Server init detected, resetting context and cancelling votes");
        context.reset();
    }

    void onInitGameEvent(InitGameEvent event) {
        GameMap nextMap = context.getNextMap();
        if (nextMap != null) {
            LOG.info("New round with next map set, switching to " + nextMap.getName());
            rcon.send("map \"" + nextMap.getName() +"\"");
            context.setNextMap(null);
            return;
        }

        GameMap gameMap = context.getMapByName(event.getMapName());
        context.setCurrentMap(gameMap);
        rcon.printConAll(PREFIX + "You are playing on " + gameMap.getName() + " round " + context.getRound() + "/" + gameMap.getMaxRounds());
        LOG.info("Map " + gameMap.getName() + " round " + context.getRound() + "/" + gameMap.getMaxRounds());
    }

    void onShutdownGameEvent(ShutdownGameEvent event) {
        context.addRounds(1);
        int maxRounds = DEFAULT_MAX_ROUNDS;
        if (context.getCurrentMap() != null) {
            maxRounds = context.getCurrentMap().getMaxRounds();
        }

        int round = context.getRound();
        if (round > maxRounds && !context.isVoting()) {
            LOG.info("Round limit reached, starting vote");
            rcon.printAll(PREFIX + "Round limit reached");
            RtvCommand.startVote(context, rcon);
            context.setRound(0);
        }
    }

    void onAdminSayEvent(AdminSayEvent event) {
        String message = event.getMessage();
        for (Command<Context> command : adminCommands) {
            try {
                if (command.execute(-1, message, context)) {
                    LOG.info("Executed admin command " + command.getClass().getSimpleName());
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
                    LOG.info("Executed user command " + command.getClass().getSimpleName() + " for player " + event.getSlot() + " " + event.getName());
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
