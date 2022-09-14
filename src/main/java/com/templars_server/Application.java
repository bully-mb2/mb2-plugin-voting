package com.templars_server;

import com.templars_server.commands.ReloadMapsCommand;
import com.templars_server.mb2_log_reader.schema.*;
import com.templars_server.model.*;
import com.templars_server.util.mqtt.MBMqttClient;
import com.templars_server.util.rcon.RconClient;
import com.templars_server.util.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        LOG.info("======== Starting mb2-plugin-voting ========");
        LOG.info("Loading settings");
        Settings settings = new Settings();
        settings.load("application.properties");

        LOG.info("Loading maps");
        GameMapList gameMaps = ReloadMapsCommand.loadMaps();

        LOG.info("Reading properties");
        String uri = "tcp://localhost:" + settings.getInt("mqtt.port");
        String topic = settings.get("mqtt.topic");
        int defaultCooldown = settings.getInt("voting.default.cooldown");
        boolean resetOnEmpty = settings.getBoolean("voting.reset_on_empty");
        boolean rtvEnabled = settings.getBoolean("voting.rtv.enabled");
        boolean rtmEnabled = settings.getBoolean("voting.rtm.enabled");
        String defaultMapString = settings.get("voting.default.map");
        GameMap defaultMap = gameMaps.get(defaultMapString);
        if (defaultMap == null) {
            LOG.error("Default map " + defaultMapString + " not found in map list, make sure it is in there");
            LOG.error("Exiting...");
            return;
        }

        GameMode defaultGameMode = readMode(settings.get("voting.default.mbmode"));
        if (defaultGameMode == null) {
            LOG.error("Selecting default mode: open");
            defaultGameMode = GameMode.OPEN;
        }

        String rtmModesString = settings.get("voting.rtm.modes");
        List<GameMode> rtmGameModes = new ArrayList<>();
        LOG.info("Reading RTM Modes");
        for (String value : rtmModesString.split(",")) {
            if (value.isEmpty()) {
                continue;
            }

            GameMode mode = readMode(value.trim());
            if (mode == null) {
                LOG.error("Exiting...");
                return;
            }

            rtmGameModes.add(mode);
        }

        if (rtmEnabled && rtmGameModes.isEmpty()) {
            LOG.warn("No game modes found for RTM, disabling RTM!");
            rtmEnabled = false;
        }

        LOG.info("Setting up rcon client");
        RconClient rcon = new RconClient();
        rcon.connect(
                settings.getAddress("rcon.host"),
                new InetSocketAddress(0),
                settings.get("rcon.password"),
                100

        );

        LOG.info("Creating context");
        Context context = new Context(
                rcon,
                gameMaps,
                defaultCooldown,
                defaultMap,
                defaultGameMode,
                resetOnEmpty,
                rtvEnabled,
                rtmEnabled,
                rtmGameModes
        );

        LOG.info("Fetching current player count");
        for (Integer slot : rcon.playerSlots()) {
            context.getPlayers().put(slot, new Player(slot, null));
        }

        LOG.info("Found " + context.getPlayers().size() + " players");
        LOG.info("Setting up voting");
        Voting voting = new Voting(
                context,
                rcon
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

    private static GameMode readMode(String value) {
        GameMode mode = GameMode.fromValue(value);
        if (mode == null) {
            LOG.error("Can't find game mode for value: " + value);
            LOG.error("Make sure you pick any of the following ids or keys: " + Arrays.toString(GameMode.values()));
            return null;
        }

        return mode;
    }

}
