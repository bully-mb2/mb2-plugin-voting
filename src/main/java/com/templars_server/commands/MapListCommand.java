package com.templars_server.commands;

import com.templars_server.model.Context;
import com.templars_server.render.Display;
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
        if (!context.isRtvEnabled()) {
            rcon.printAll(String.format("%sRTV is disabled", Display.RTV_PREFIX));
            return;
        }
        
        int page = getArgInt(0, 1);
        if (page < 1) {
            page = 1;
        }

        page = Math.abs(page);
        long skipped = (long) (page-1) * pageSize;
        List<String> result = context.getMaps().keySet()
                .stream()
                .skip(skipped)
                .limit(pageSize)
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            getArgs().clear();
            onExecute(slot, context);
            return;
        }

        rcon.print(slot, Display.PREFIX + "Map list printed to console, press (ALT + ~) to view");
        Display.renderMaps(result).forEach(line -> rcon.printCon(slot, line));
        int pages = context.getMaps().size() / pageSize + 1;
        rcon.printCon(slot, String.format("%sPage %d/%d - %s", Display.PREFIX, page, pages, getUsage()));
    }

}
