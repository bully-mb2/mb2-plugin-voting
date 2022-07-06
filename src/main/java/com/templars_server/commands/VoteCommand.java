package com.templars_server.commands;

import com.templars_server.model.Context;
import com.templars_server.Voting;
import com.templars_server.util.command.Command;
import com.templars_server.util.command.InvalidArgumentException;

public class VoteCommand extends Command<Context> {

    public VoteCommand() {
        super(
                "([0-9])",
                true,
                "!number"
        );
    }

    @Override
    protected void onExecute(int slot, Context context) throws InvalidArgumentException {
        if (!context.isVoting()) {
            context.getRconClient().print(slot, Voting.PREFIX + "There is no vote going on");
            return;
        }

        int vote = getArgInt(0) - 1;
        context.getVote().vote(slot, vote);
    }

}
