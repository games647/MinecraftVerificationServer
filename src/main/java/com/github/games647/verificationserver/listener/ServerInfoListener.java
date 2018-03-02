package com.github.games647.verificationserver.listener;

import com.github.games647.verificationserver.Config;
import com.github.games647.verificationserver.VerificationServer;
import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.data.message.TextMessage;
import com.github.steveice10.mc.protocol.data.status.PlayerInfo;
import com.github.steveice10.mc.protocol.data.status.ServerStatusInfo;
import com.github.steveice10.mc.protocol.data.status.VersionInfo;
import com.github.steveice10.mc.protocol.data.status.handler.ServerInfoBuilder;
import com.github.steveice10.packetlib.Session;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

public class ServerInfoListener implements ServerInfoBuilder {

    private final VerificationServer verificationServer;

    private final BufferedImage favicon;

    private final PlayerInfo playerInfo;
    private final TextMessage textMessage;

    private final String gameVersion;
    private final int protocolVersion;

    public ServerInfoListener(VerificationServer verificationServer, Config config) throws IOException {
        this.verificationServer = verificationServer;

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

        Path file = Paths.get("favicon.png");
        if (Files.exists(file)) {
            favicon = ImageIO.read(Files.newInputStream(file));
        } else {
            favicon = null;
        }
    }

    @Override
    public ServerStatusInfo buildInfo(Session session) {
        String host = session.getHost();
        VerificationServer.getLogger().info("Pinging client: {}", host);

        int clientProtocol = verificationServer.getProtocolVersions().getOrDefault(host, protocolVersion);
        VersionInfo versionInfo = new VersionInfo(gameVersion, clientProtocol);
        return new ServerStatusInfo(versionInfo, playerInfo, textMessage, favicon);
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
                .filter(key -> key.startsWith("fakePlayer."))
                .forEach(key -> {
                    String playerName = key.replace("fakePlayer.", "");
                    UUID uuid = UUID.fromString(properties.getProperty(key));

                    profiles.add(new GameProfile(uuid, playerName));
                });

        return profiles.toArray(new GameProfile[profiles.size()]);
    }
}
