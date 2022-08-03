package com.templars_server.commands;

import com.templars_server.Application;
import com.templars_server.Voting;
import com.templars_server.model.Context;
import com.templars_server.model.GameMap;
import com.templars_server.render.Display;
import com.templars_server.util.command.Command;
import com.templars_server.util.command.InvalidArgumentException;
import com.templars_server.util.rcon.RconClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReloadMapsCommand extends Command<Context> {

    private static final Logger LOG = LoggerFactory.getLogger(ReloadMapsCommand.class);
    private static final String MAPS_PATH = "maps.txt";

    public ReloadMapsCommand() {
        super(
                "reloadmaps",
                false,
                "!reloadmaps"
        );
    }

    @Override
    protected void onExecute(int slot, Context context) throws InvalidArgumentException {
        RconClient rcon = context.getRconClient();
        if (context.isVoting()) {
            rcon.printAll(Display.PREFIX + "Can't reload maps while there is a vote in progress");
            return;
        }

        if (context.getNextMap() != null) {
            rcon.printAll(Display.PREFIX + "Can't reload maps while waiting to switch, please try again on the next map");
            return;
        }

        try {
            int lastSize = context.getMaps().size();
            LinkedHashMap<String, GameMap> maps = loadMaps();
            context.setMaps(maps);
            rcon.printAll(Display.PREFIX + "Successfully reloaded maps, old size (" + lastSize + ") new size (" + maps.size() + ")");
        } catch (NumberFormatException e) {
            rcon.printAll(Display.PREFIX + e.getMessage() + ", aborting");
        } catch (Exception e) {
            rcon.printAll(Display.PREFIX + "Couldn't reload map, try again another time");
            LOG.error("Exception during map reload", e);
        }
    }

    public static LinkedHashMap<String, GameMap> loadMaps() throws IOException {
        LOG.info("Loading maps");
        File file = new File(MAPS_PATH);
        LinkedHashMap<String, GameMap> gameMaps = new LinkedHashMap<>();
        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                LOG.info(MAPS_PATH + " not found, creating from default");
                InputStream defaultContentStream = Application.class.getResourceAsStream("/" + MAPS_PATH);
                if (defaultContentStream == null) {
                    throw new NullPointerException("Couldn't find resource " + MAPS_PATH);
                }

                String defaultContent = new String(defaultContentStream.readAllBytes(), Charset.defaultCharset());
                writer.write(defaultContent);
            }
        }

        int lastLine = 0;
        try (Stream<String> stream = Files.lines(Paths.get(MAPS_PATH))) {
            List<String> lines = stream.collect(Collectors.toList());
            for (String line : lines) {
                int maxRounds = Voting.DEFAULT_MAX_ROUNDS;
                String name = line;
                String[] split = line.split(" ");
                if (split.length == 2) {
                    maxRounds = Integer.parseInt(split[0]);
                    name = split[1];
                }

                gameMaps.put(name, new GameMap(name, maxRounds));
                lastLine++;
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Couldn't convert max round on line " + lastLine + " to a number");
        }

        LOG.info("Found " + gameMaps.size() + " maps");
        return gameMaps;
    }
}
