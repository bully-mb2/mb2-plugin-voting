package com.templars_server.commands;

import com.templars_server.model.Context;
import com.templars_server.model.Player;
import com.templars_server.model.PlayerList;
import com.templars_server.render.Display;
import com.templars_server.util.command.InvalidArgumentException;
import com.templars_server.util.rcon.RconClient;
import com.templars_server.voting.ModeVote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RtmCommand extends PreVoteCommand {

    private static final Logger LOG = LoggerFactory.getLogger(RtmCommand.class);
    private static final float THRESHOLD_PERCENTAGE = 0.5f;

    public RtmCommand() {
        super(
                "(rtm|unrtm)",
                false,
                "!rtm or !unrtm");
    }

    @Override
    protected void onExecute(int slot, Context context, RconClient rcon) throws InvalidArgumentException {
        if (!context.isRtmEnabled()) {
            rcon.printAll(String.format("%sRTM is disabled", Display.RTV_PREFIX));
            return;
        }

        boolean mode = getArg(0).equalsIgnoreCase("rtm");
        rtm(slot, context, context.getRconClient(), mode);
    }

    void rtm(int slot, Context context, RconClient rcon, boolean rtm) {
        PlayerList players = context.getPlayers();
        Player player = players.get(slot);
        boolean before = player.isRtm();
        player.setRtm(rtm);
        long voters = players.values().stream()
                .filter(Player::isRtm)
                .count();
        int threshold = (int) Math.ceil(players.size() * THRESHOLD_PERCENTAGE);
        if (player.isRtm()) {
            if (before) {
                rcon.printAll(String.format("%s%s^7 really wants you to rock the mode (%d/%d)", Display.RTM_PREFIX, player.getName(), voters, threshold));
            } else {
                rcon.printAll(String.format("%s%s^7 wants to rock the mode (%d/%d)", Display.RTM_PREFIX, player.getName(), voters, threshold));
                LOG.info(String.format("Player slot: %d, name: %s rtm now (%d/%d)", player.getSlot(), player.getName(), voters, threshold));
            }
        } else {
            if (before) {
                rcon.printAll(String.format("%s%s^7 no longer wants to rock the mode (%d/%d)", Display.RTM_PREFIX, player.getName(), voters, threshold));
                LOG.info(String.format("Player slot: %d, name: %s unrtm now (%d/%d)", player.getSlot(), player.getName(), voters, threshold));
                return;
            } else {
                rcon.print(slot, String.format("%sYou haven't rocked the mode yet (%d/%d)", Display.RTM_PREFIX, voters, threshold));
            }
        }

        if (voters >= threshold) {
            ModeVote.startVote(context);
        }
    }

}
