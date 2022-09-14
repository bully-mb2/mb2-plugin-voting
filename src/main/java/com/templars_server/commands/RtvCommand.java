package com.templars_server.commands;

import com.templars_server.model.Context;
import com.templars_server.model.Player;
import com.templars_server.model.PlayerList;
import com.templars_server.render.Display;
import com.templars_server.util.command.InvalidArgumentException;
import com.templars_server.util.rcon.RconClient;
import com.templars_server.voting.MapVote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RtvCommand extends PreVoteCommand {

    private static final Logger LOG = LoggerFactory.getLogger(RtvCommand.class);
    private static final float THRESHOLD_PERCENTAGE = 0.5f;

    public RtvCommand() {
        super(
                "(rtv|unrtv)",
                false,
                "!rtv or !unrtv"
        );
    }

    @Override
    protected void onExecute(int slot, Context context, RconClient rcon) throws InvalidArgumentException {
        if (!context.isRtvEnabled()) {
            rcon.printAll(String.format("%sRTV is disabled", Display.RTV_PREFIX));
            return;
        }

        boolean mode = getArg(0).equalsIgnoreCase("rtv");
        rtv(slot, context, context.getRconClient(), mode);
    }

    void rtv(int slot, Context context, RconClient rcon, boolean rtv) {
        PlayerList players = context.getPlayers();
        Player player = players.get(slot);
        boolean before = player.isRtv();
        player.setRtv(rtv);
        long voters = players.values().stream()
                .filter(Player::isRtv)
                .count();
        int threshold = (int) Math.ceil(players.size() * THRESHOLD_PERCENTAGE);
        if (player.isRtv()) {
            if (before) {
                rcon.printAll(String.format("%s%s^7 really wants you to rock the vote (%d/%d)", Display.RTV_PREFIX, player.getName(), voters, threshold));
            } else {
                rcon.printAll(String.format("%s%s^7 wants to rock the vote (%d/%d)", Display.RTV_PREFIX, player.getName(), voters, threshold));
                LOG.info(String.format("Player slot: %d, name: %s rtv now (%d/%d)", player.getSlot(), player.getName(), voters, threshold));
            }
        } else {
            if (before) {
                rcon.printAll(String.format("%s%s^7 no longer wants to rock the vote (%d/%d)", Display.RTV_PREFIX, player.getName(), voters, threshold));
                LOG.info(String.format("Player slot: %d, name: %s unrtv now (%d/%d)", player.getSlot(), player.getName(), voters, threshold));
                return;
            } else {
                rcon.print(slot, String.format("%sYou haven't rocked the vote yet (%d/%d)", Display.RTV_PREFIX, voters, threshold));
            }
        }

        if (voters >= threshold) {
            MapVote.startVote(context);
        }
    }

}
