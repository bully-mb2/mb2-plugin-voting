package com.templars_server;

import com.templars_server.commands.*;
import com.templars_server.model.Context;
import com.templars_server.model.GameMap;
import com.templars_server.model.Player;
import com.templars_server.render.Display;
import com.templars_server.util.command.Command;
import com.templars_server.util.command.InvalidArgumentException;
import com.templars_server.util.rcon.RconClient;
import generated.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Voting {

    public static final int DEFAULT_MAX_ROUNDS = 20;
    private static final Logger LOG = LoggerFactory.getLogger(Voting.class);
    private static final int PAGE_SIZE = 24;

    private final Context context;
    private final RconClient rcon;
    private final int defaultMBMode;
    private final List<Command<Context>> commands;
    private final List<Command<Context>> adminCommands;

    public Voting(Context context, RconClient rcon, int defaultMBMode) {
        this.context = context;
        this.rcon = rcon;
        this.defaultMBMode = defaultMBMode;
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

        printReady();
    }

    void onClientConnectEvent(ClientConnectEvent event) {
        putPlayer(event.getSlot(), event.getName());
    }

    void onClientDisconnectEvent(ClientDisconnectEvent event) {
        context.getPlayers().remove(event.getSlot());
        if (context.isVoting()) {
            LOG.info(event.getSlot() + " disconnected during vote, removing from list");
            context.getVote().vote(event.getSlot(), null);
        }
    }

    void onServerInitializationEvent(ServerInitializationEvent event) {
        LOG.info("Server init detected, resetting context and cancelling votes");
        context.reset();
        printReady();
    }

    void onInitGameEvent(InitGameEvent event) {
        GameMap gameMap = context.getMapByName(event.getMapName());
        context.setCurrentMap(gameMap);
        LOG.info("Map " + gameMap.getName() + " round " + context.getRound() + "/" + gameMap.getMaxRounds());
        if (!context.isVoting()) {
            rcon.printConAll(Display.PREFIX + "You are playing on " + gameMap.getName() + " round " + context.getRound() + "/" + gameMap.getMaxRounds());
        }
    }

    void onShutdownGameEvent(ShutdownGameEvent event) {
        GameMap nextMap = context.getNextMap();
        if (nextMap != null) {
            LOG.info("New round with next map set, switching to " + nextMap.getName());
            rcon.mode(defaultMBMode, nextMap.getName());
            context.reset();
        }

        context.addRounds(1);
        int maxRounds = DEFAULT_MAX_ROUNDS;
        if (context.getCurrentMap() != null) {
            maxRounds = context.getCurrentMap().getMaxRounds();
        }

        int round = context.getRound();
        if (round > maxRounds && !context.isVoting()) {
            LOG.info("Round limit reached, starting vote");
            rcon.printAll(Display.PREFIX + "Round limit reached");
            RtvCommand.startVote(context, rcon);
            context.setRound(1);
        }
    }

    public void onClientUserinfoChangedEvent(ClientUserinfoChangedEvent event) {
        putPlayer(event.getSlot(), event.getName());
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
                rcon.printAll(Display.PREFIX + command.getUsage());
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
                rcon.print(event.getSlot(), Display.PREFIX + command.getUsage());
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
        } else {
            player.setName(name);
        }
    }

    private void printReady() {
        LOG.info("Setting cvar and announcing it is ready");
        String version = getClass().getPackage().getImplementationVersion();
        if (version == null) {
            version = "dev";
        }

        rcon.send("sets RTVRTM 3807/" + version);
        rcon.printAll(Display.PREFIX + "Voting is now enabled");
    }

}
