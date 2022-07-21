package com.templars_server;

import com.templars_server.model.Context;
import com.templars_server.model.GameMap;
import com.templars_server.model.Player;
import com.templars_server.util.mqtt.MBMqttClient;
import com.templars_server.util.rcon.RconClient;
import com.templars_server.util.settings.Settings;
import generated.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private static final String MAPS_PATH = "maps.txt";

    public static void main(String[] args) throws Exception {
        LOG.info("======== Starting mb2-plugin-voting ========");
        LOG.info("Loading settings");
        Settings settings = new Settings();
        settings.load("application.properties");

        LOG.info("Reading properties");
        String uri = "tcp://localhost:" + settings.getInt("mqtt.port");
        String topic = settings.get("mqtt.topic");

        LOG.info("Setting up rcon client");
        RconClient rcon = new RconClient();
        rcon.connect(
                settings.getAddress("rcon.host"),
                new InetSocketAddress(0),
                settings.get("rcon.password"),
                100

        );

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
            }
        }

        LOG.info("Found " + gameMaps.size() + " maps");
        LOG.info("Fetching current player count");
        Context context = new Context(rcon, gameMaps);
        for (Integer slot : rcon.playerSlots()) {
            context.getPlayers().put(slot, new Player(slot, null));
        }
        LOG.info("Found " + context.getPlayers().size() + " players");

        LOG.info("Setting up voting");
        Voting voting = new Voting(
                context,
                rcon,
                settings.getInt("voting.default.mbmode")
        );
        voting.setup();

        LOG.info("Registering event callbacks");
        MBMqttClient client = new MBMqttClient();
        client.putEventListener(voting::onClientConnectEvent, ClientConnectEvent.class);
        client.putEventListener(voting::onClientDisconnectEvent, ClientDisconnectEvent.class);
        client.putEventListener(voting::onInitGameEvent, InitGameEvent.class);
        client.putEventListener(voting::onShutdownGameEvent, ShutdownGameEvent.class);
        client.putEventListener(voting::onSayEvent, SayEvent.class);
        client.putEventListener(voting::onClientUserinfoChangedEvent, ClientUserinfoChangedEvent.class);
        client.putEventListener(voting::onAdminSayEvent, AdminSayEvent.class);
        client.putEventListener(voting::onServerInitializationEvent, ServerInitializationEvent.class);

        LOG.info("Connecting to MQTT broker");
        client.connect(uri, topic);
    }

}
