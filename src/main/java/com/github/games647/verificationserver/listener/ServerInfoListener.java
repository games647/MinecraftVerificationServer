package com.github.games647.verificationserver.listener;

import com.github.games647.verificationserver.Config;
import com.github.games647.verificationserver.VerificationServer;

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

    public ServerInfoListener(Config config) {
        int onlinePlayers = Integer.parseInt(config.get("onlinePlayers"));
        int maxPlayers = Integer.parseInt(config.get("maxPlayers"));

        playerInfo = new PlayerInfo(onlinePlayers, maxPlayers, new GameProfile[0]);

        String motd = config.get("motd");
        textMessage = new TextMessage(motd);
    }

    @Override
    public ServerStatusInfo buildInfo(Session session) {
        VerificationServer.getLogger().info("Pinging client: {}", session);

        VersionInfo versionInfo = new VersionInfo(MinecraftConstants.GAME_VERSION, MinecraftConstants.PROTOCOL_VERSION);
        return new ServerStatusInfo(versionInfo, playerInfo, textMessage, null);
    }
}
