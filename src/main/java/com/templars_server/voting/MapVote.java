package com.templars_server.voting;

import com.templars_server.Voting;
import com.templars_server.model.Context;
import com.templars_server.model.GameMap;
import com.templars_server.model.Player;
import com.templars_server.render.Display;
import com.templars_server.util.rcon.RconClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class MapVote {

    private static final Logger LOG = LoggerFactory.getLogger(MapVote.class);

    public static void startVote(Context context) {
        List<String> choices = collectChoices(context);
        LOG.info("Creating vote with choices: " + Arrays.toString(choices.toArray()));
        Vote vote = new Vote(
                Display.RTV_PREFIX,
                choices,
                context,
                MapVote::onVoteComplete
        );
        vote.start();
        context.setVote(vote);
    }

    static List<String> collectChoices(Context context) {
        List<String> nominations = collectNominations(context.getPlayers());
        List<String> excludedMaps = new ArrayList<>();
        excludedMaps.add(context.getCurrentMap().getName());
        excludedMaps.addAll(nominations);

        List<String> choices = new ArrayList<>();
        choices.addAll(nominations);
        choices.addAll(collectRandomMaps(
                context.getMaps(),
                excludedMaps,
                Vote.MAX_CHOICES - choices.size() - 1
        ));
        choices.add(Vote.DONT_CHANGE);
        return choices;
    }

    static List<String> collectNominations(Map<Integer, Player> players) {
        List<String> nominations = players.values().stream()
                .map(Player::getNomination)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Collections.shuffle(nominations);
        return nominations.stream()
                .limit(Vote.MAX_CHOICES - 1)
                .collect(Collectors.toList());
    }

    static List<String> collectRandomMaps(Map<String, GameMap> maps, List<String> excludedMaps, int amount) {
        List<String> mapChoices = new ArrayList<>(maps.keySet());
        Collections.shuffle(mapChoices);
        return mapChoices.stream()
                .filter(choice -> !excludedMaps.contains(choice))
                .filter(choice -> maps.get(choice).getCooldown() < 1)
                .limit(amount)
                .collect(Collectors.toList());
    }

    private static void onVoteComplete(Context context, String result) {
        RconClient rcon = context.getRconClient();
        context.getPlayers().values().forEach(player -> player.setRtv(false));
        if (result.isEmpty()) {
            return;
        }

        if (result.equals(Vote.DONT_CHANGE)) {
            context.setVote(null);
            return;
        }

        GameMap gameMap = context.getMaps().get(result);
        if (gameMap == null) {
            LOG.error("Panic! A map that was voted that wasn't found in the maplist: " + result);
            gameMap = new GameMap(result, Voting.DEFAULT_MAX_ROUNDS);
        }

        rcon.printAll(Display.RTV_PREFIX + "Switching to map " + result + " next round");
        context.setNextMap(gameMap);
    }

}
