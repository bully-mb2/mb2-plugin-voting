package com.templars_server.commands;

import com.templars_server.model.Context;
import com.templars_server.model.GameMap;
import com.templars_server.model.Player;
import com.templars_server.Voting;
import com.templars_server.render.Display;
import com.templars_server.util.command.InvalidArgumentException;
import com.templars_server.util.rcon.RconClient;
import com.templars_server.voting.Vote;

import java.util.*;
import java.util.stream.Collectors;

public class RtvCommand extends PreVoteCommand {

    private static final float THRESHOLD_PERCENTAGE = 0.5f;
    private static final String DONT_CHANGE = "Don't change";

    public RtvCommand() {
        this(
                "(rtv|unrtv)",
                "!rtv or !unrtv"
        );
    }

    RtvCommand(String regex, String usage) {
        super(
                regex,
                false,
                usage
        );
    }

    @Override
    protected void onExecute(int slot, Context context, RconClient rcon) throws InvalidArgumentException {
        boolean mode = getArg(0).equalsIgnoreCase("rtv");
        rtv(slot, context, context.getRconClient(), mode);
    }

    void rtv(int slot, Context context, RconClient rcon, boolean rtv) {
        Map<Integer, Player> players = context.getPlayers();
        Player player = players.get(slot);
        boolean before = player.isRtv();
        player.setRtv(rtv);
        long voters = players.values().stream()
                .filter(Player::isRtv)
                .count();
        int threshold = (int) Math.ceil(players.size() * THRESHOLD_PERCENTAGE);
        if (player.isRtv()) {
            if (before) {
                rcon.printAll(String.format("%s%s^7 really wants you to rock the vote (%d/%d)", Display.PREFIX, players.get(slot).getName(), voters, threshold));
            } else {
                rcon.printAll(String.format("%s%s^7 wants to rock the vote (%d/%d)", Display.PREFIX, players.get(slot).getName(), voters, threshold));
            }
        } else {
            if (before) {
                rcon.printAll(String.format("%s%s^7 no longer wants to rock the vote (%d/%d)", Display.PREFIX, players.get(slot).getName(), voters, threshold));
                return;
            } else {
                rcon.print(slot, String.format("%sYou haven't rocked the vote yet (%d/%d)", Display.PREFIX, voters, threshold));
            }
        }

        if (voters >= threshold) {
            startVote(context, rcon);
        }
    }

    public static void startVote(Context context, RconClient rcon) {
        Vote vote = makeVote(context, rcon);
        vote.start();
        context.setVote(vote);
    }

    // TODO :: Messy, should probably move it to a MapVoteBuilder or something
    private static Vote makeVote(Context context, RconClient rcon) {
        Map<Integer, Player> players = context.getPlayers();
        players.values().forEach(player -> player.setRtv(false));
        List<String> nominations = players.values().stream()
                .map(Player::getNomination)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Collections.shuffle(nominations);
        nominations = nominations.stream()
                .limit(Vote.MAX_CHOICES - 1)
                .collect(Collectors.toList());

        if (nominations.size() < Vote.MAX_CHOICES - 1) {
            List<String> mapChoices = new ArrayList<>(context.getMaps().keySet());
            Collections.shuffle(mapChoices);
            mapChoices = mapChoices.stream()
                    .limit(Vote.MAX_CHOICES - 1 - nominations.size())
                    .collect(Collectors.toList());
            nominations.addAll(mapChoices);
        }


        nominations.add(DONT_CHANGE);
        return new Vote(Display.PREFIX, nominations, context, (result) -> {
            onVoteComplete(result, rcon, context);
        });
    }

    private static void onVoteComplete(String result, RconClient rcon, Context context) {
        if (result.isEmpty()) {
            return;
        }

        if (result.equals(DONT_CHANGE)) {
            context.setVote(null);
            return;
        }

        GameMap gameMap = context.getMaps().get(result);
        if (gameMap == null) {
            gameMap = new GameMap(result, Voting.DEFAULT_MAX_ROUNDS);
        }

        rcon.printAll(Display.PREFIX + "Switching to map " + result + " next round");
        context.setNextMap(gameMap);
    }

}
