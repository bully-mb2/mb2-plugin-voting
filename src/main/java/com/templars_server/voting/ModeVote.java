package com.templars_server.voting;

import com.templars_server.model.Context;
import com.templars_server.model.GameMode;
import com.templars_server.render.Display;
import com.templars_server.util.rcon.RconClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ModeVote {

    private static final Logger LOG = LoggerFactory.getLogger(ModeVote.class);

    public static void startVote(Context context) {
        List<String> choices = Arrays.stream(GameMode.values())
                .map(GameMode::getDisplay)
                .collect(Collectors.toList());
        LOG.info("Creating vote with choices: " + Arrays.toString(choices.toArray()));
        Vote vote = new Vote(
                Display.RTM_PREFIX,
                choices,
                context,
                ModeVote::onVoteComplete
        );
        vote.start();
        context.setVote(vote);
    }

    private static void onVoteComplete(Context context, String result) {
        RconClient rcon = context.getRconClient();
        context.getPlayers().values().forEach(player -> player.setRtm(false));
        if (result.isEmpty()) {
            return;
        }

        GameMode gameMode = GameMode.fromValue(result);
        if (gameMode == null) {
            LOG.error("Panic! A mode that was voted that wasn't found in the mode list: " + result);
            gameMode = context.getDefaultGameMode();
        }

        rcon.printAll(Display.RTV_PREFIX + "Switching to mode " + result + " next round");
        context.setNextGameMode(gameMode);
    }

}
