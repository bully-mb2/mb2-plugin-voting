package com.templars_server.commands;

import com.templars_server.model.Context;
import com.templars_server.render.Display;
import com.templars_server.util.command.InvalidArgumentException;
import com.templars_server.util.rcon.RconClient;

public class RtmCommand extends PreVoteCommand {

    public RtmCommand() {
        super(
                "rtm",
                false,
                "!rtm");
    }

    @Override
    protected void onExecute(int slot, Context context, RconClient rcon) throws InvalidArgumentException {
        rcon.print(slot, Display.PREFIX + "RTM not yet implemented, sorry");
    }

}
