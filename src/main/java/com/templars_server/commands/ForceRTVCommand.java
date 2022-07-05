package com.templars_server.commands;

import com.templars_server.Context;
import com.templars_server.util.command.InvalidArgumentException;
import com.templars_server.util.rcon.RconClient;

public class ForceRTVCommand extends PreVoteCommand {

    public ForceRTVCommand() {
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
