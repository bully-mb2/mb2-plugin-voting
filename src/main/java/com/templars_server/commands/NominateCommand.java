package com.templars_server.commands;

import com.templars_server.model.Context;
import com.templars_server.model.GameMap;
import com.templars_server.model.Player;
import com.templars_server.render.Display;
import com.templars_server.util.command.InvalidArgumentException;
import com.templars_server.util.rcon.RconClient;

import java.util.Set;

public class NominateCommand extends PreVoteCommand {

    public NominateCommand() {
        super(
                "nominate",
                false,
                "!nominate <map>"
        );
    }

    @Override
    protected void onExecute(int slot, Context context, RconClient rcon) throws InvalidArgumentException {
        if (context.isVoting()) {
            rcon.print(slot, Display.PREFIX + "There is already a vote in progress");
            return;
        }

        if (context.getNextMap() != null) {
            rcon.print(slot, Display.PREFIX + "Vote already ended, switching to " + context.getNextMap().getName() + " next round");
            return;
        }

        String nomination = getArg(0).toLowerCase();
        Player player = context.getPlayers().get(slot);
        Set<String> mapList = context.getMaps().keySet();
        if (!mapList.contains(nomination)) {
            rcon.print(slot, Display.PREFIX + "Map not found");
            return;
        }

        GameMap gameMap = context.getCurrentMap();
        if (gameMap != null) {
            if (nomination.equals(gameMap.getName())) {
                rcon.print(slot, Display.PREFIX + "You are already on this map");
                return;
            }
        }

        String oldNomination = player.getNomination();
        if (nomination.equals(oldNomination)) {
            rcon.print(slot, Display.PREFIX + "You already nominated that map");
            return;
        }

        player.setNomination(nomination);
        if (oldNomination == null) {
            rcon.printAll(Display.PREFIX + context.getPlayers().get(slot).getName() + "^7 nominated " + nomination);
        } else {
            rcon.printAll(Display.PREFIX + context.getPlayers().get(slot).getName() + "^7 changed their nomination from " + oldNomination + " to " + nomination);
        }
    }

}
