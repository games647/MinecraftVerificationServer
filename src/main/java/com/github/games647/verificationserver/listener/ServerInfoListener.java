package com.github.games647.verificationserver.listener;

import com.github.games647.verificationserver.Config;
import com.github.games647.verificationserver.VerificationServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.spacehq.mc.auth.data.GameProfile;
import org.spacehq.mc.protocol.MinecraftConstants;
import org.spacehq.mc.protocol.data.message.TextMessage;
import org.spacehq.mc.protocol.data.status.PlayerInfo;
import org.spacehq.mc.protocol.data.status.ServerStatusInfo;
import org.spacehq.mc.protocol.data.status.VersionInfo;
import org.spacehq.mc.protocol.data.status.handler.ServerInfoBuilder;
import org.spacehq.packetlib.Session;

public class ServerInfoListener implements ServerInfoBuilder {

    private final PlayerInfo playerInfo;
    private final TextMessage textMessage;

    private final String gameVersion;
    private final int protocolVersion;

    public ServerInfoListener(Config config) {
        int onlinePlayers = Integer.parseInt(config.get("onlinePlayers"));
        int maxPlayers = Integer.parseInt(config.get("maxPlayers"));

        GameProfile[] players = getPlayers(config);
        playerInfo = new PlayerInfo(onlinePlayers, maxPlayers, players);

        String motd = config.get("motd");
        textMessage = new TextMessage(motd);

        String version = config.get("version");
        if (version.trim().isEmpty()) {
            version = MinecraftConstants.GAME_VERSION;
        }

        this.gameVersion = version;
        String protocol = config.get("protocol");
        if (protocol.trim().isEmpty()) {
            this.protocolVersion = MinecraftConstants.PROTOCOL_VERSION;
        } else {
            this.protocolVersion = Integer.parseInt(protocol);
        }
    }

    @Override
    public ServerStatusInfo buildInfo(Session session) {
        VerificationServer.getLogger().info("Pinging client: {}", session);

        VersionInfo versionInfo = new VersionInfo(gameVersion, protocolVersion);
        return new ServerStatusInfo(versionInfo, playerInfo, textMessage, null);
    }

    private GameProfile[] getPlayers(Config config) {
        boolean disabled = Boolean.parseBoolean(config.get("disabledSlots"));
        if (disabled) {
            return new GameProfile[0];
        }

        Properties properties = config.getProperties();
        Set<String> propertyNames = properties.stringPropertyNames();

        List<GameProfile> profiles = new ArrayList<>();
        propertyNames.stream()
                .filter((key) -> key.startsWith("fakePlayer."))
                .forEach((key) -> {
                    String playerName = key.replace(key, "fakePlayer.");
                    UUID uuid = UUID.fromString(properties.getProperty(key));
                    profiles.add(new GameProfile(uuid, playerName));
                });

        return profiles.toArray(new GameProfile[profiles.size()]);
    }
}
