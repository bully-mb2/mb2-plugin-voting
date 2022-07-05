package com.templars_server.commands;

import com.templars_server.Context;
import com.templars_server.Voting;
import com.templars_server.util.command.Command;
import com.templars_server.util.command.InvalidArgumentException;
import com.templars_server.util.rcon.RconClient;

import java.util.List;
import java.util.stream.Collectors;

public class MapListCommand extends Command<Context> {

    private final int pageSize;

    public MapListCommand(int pageSize) {
        super(
                "maplist",
                false,
                "!maplist <page number>"
        );
        this.pageSize = pageSize;
    }

    @Override
    protected void onExecute(int slot, Context context) throws InvalidArgumentException {
        RconClient rcon = context.getRconClient();
        int page = getArgInt(0, 1);
        if (page < 1) {
            page = 1;
        }

        page = Math.abs(page);
        long skipped = (long) (page-1) * pageSize;
        List<String> result = context.getMaps().stream()
                .skip(skipped)
                .limit(pageSize)
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            getArgs().clear();
            onExecute(slot, context);
            return;
        }

        context.printMaps(slot, rcon, result, Voting.PREFIX);
        int pages = context.getMaps().size() / pageSize + 1;
        rcon.print(slot, String.format("%sPage %d/%d - %s", Voting.PREFIX, page, pages, getUsage()));
    }

}
