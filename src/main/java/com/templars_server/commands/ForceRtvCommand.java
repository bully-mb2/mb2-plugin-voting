package com.templars_server.commands;

import com.templars_server.model.Context;
import com.templars_server.render.Display;
import com.templars_server.util.command.Command;
import com.templars_server.util.command.InvalidArgumentException;
import com.templars_server.util.rcon.RconClient;

public class ForceRtvCommand extends Command<Context> {

    public ForceRtvCommand() {
        super(
                "forcertv",
                false,
                "!forcertv"
        );
    }

    @Override
    protected void onExecute(int slot, Context context) throws InvalidArgumentException {
        RconClient rcon = context.getRconClient();
        if (context.isVoting()) {
            rcon.printAll(Display.PREFIX + "There is already a vote in progress");
            return;
        }

        RtvCommand.startVote(context, context.getRconClient());
    }
}
