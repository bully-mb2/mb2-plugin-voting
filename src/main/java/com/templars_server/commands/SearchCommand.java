package com.templars_server.commands;

import com.templars_server.model.Context;
import com.templars_server.Voting;
import com.templars_server.util.command.Command;
import com.templars_server.util.command.InvalidArgumentException;
import com.templars_server.util.rcon.RconClient;

import java.util.List;
import java.util.stream.Collectors;

public class SearchCommand extends Command<Context> {

    private final int pageSize;

    public SearchCommand(int pageSize) {
        super(
                "search",
                false,
                "!search <query>"
        );
        this.pageSize = pageSize;
    }

    @Override
    protected void onExecute(int slot, Context context) throws InvalidArgumentException {
        RconClient rcon = context.getRconClient();
        String query = getArg(0);
        List<String> maps = context.getMaps().keySet().stream()
                .filter(map -> map.contains(query))
                .limit(pageSize)
                .collect(Collectors.toList());

        if (maps.isEmpty()) {
            rcon.print(slot, Voting.PREFIX + "No maps found");
            return;
        }

        context.printMaps(slot, rcon, maps, Voting.PREFIX);
        if (maps.size() == pageSize) {
            rcon.print(slot, Voting.PREFIX + "Search result too large, try being more specific");
        }
    }

}
