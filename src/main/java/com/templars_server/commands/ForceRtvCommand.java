package com.templars_server.commands;

import com.templars_server.model.Context;
import com.templars_server.util.command.InvalidArgumentException;
import com.templars_server.util.rcon.RconClient;

public class ForceRtvCommand extends PreVoteCommand {

    public ForceRtvCommand() {
        super(
                "forcertv",
                false,
                "!forcertv"
        );
    }

    @Override
    protected void onExecute(int slot, Context context, RconClient rcon) throws InvalidArgumentException {
        RtvCommand.startVote(context, rcon);
    }

}
