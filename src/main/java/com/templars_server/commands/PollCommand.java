package com.templars_server.commands;

import com.templars_server.Context;
import com.templars_server.Voting;
import com.templars_server.util.command.Command;
import com.templars_server.util.command.InvalidArgumentException;
import com.templars_server.util.rcon.RconClient;
import com.templars_server.voting.Vote;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PollCommand extends Command<Context> {

    public PollCommand() {
        super(
                "poll",
                false,
                "!poll <option1>, <option2>, ...",
                ","
        );
    }

    @Override
    protected void onExecute(int slot, Context context) throws InvalidArgumentException {
        RconClient rcon = context.getRconClient();
        if (context.isVoting()) {
            rcon.printAll(Voting.PREFIX + "There is already a vote in progress");
            return;
        }

        List<String> args = getArgs().stream()
                .map(String::stripLeading)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        if (args.size() < 2) {
            throw new InvalidArgumentException();
        }

        Vote vote = new Vote(Voting.PREFIX, args, rcon, (result) -> onVoteComplete(result, rcon, context));
        vote.start();
        context.setVote(vote);
    }

    private void onVoteComplete(String result, RconClient rcon, Context context) {
        context.setVote(null);
    }

}
