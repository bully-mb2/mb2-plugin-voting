package com.templars_server.commands;

import com.templars_server.model.Context;
import com.templars_server.Voting;
import com.templars_server.util.command.Command;
import com.templars_server.util.command.InvalidArgumentException;
import com.templars_server.util.rcon.RconClient;

public abstract class PreVoteCommand extends Command<Context> {

    public PreVoteCommand(String regex, boolean requireExclamation, String usage) {
        super(regex, requireExclamation, usage);
    }

    @Override
    protected void onExecute(int slot, Context context) throws InvalidArgumentException {
        RconClient rcon = context.getRconClient();
        if (context.isVoting()) {
            rcon.print(slot, Voting.PREFIX + "There is already a vote in progress");
            return;
        }

        if (context.getNextMap() != null) {
            rcon.print(slot, Voting.PREFIX + "Vote already ended, switching to " + context.getNextMap() + " next round");
            return;
        }

        onExecute(slot, context, rcon);
    }

    protected abstract void onExecute(int slot, Context context, RconClient rcon) throws InvalidArgumentException;

}
