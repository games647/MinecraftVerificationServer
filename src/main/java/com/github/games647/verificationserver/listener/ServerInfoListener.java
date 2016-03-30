package com.github.games647.verificationserver.listener;

import com.github.games647.verificationserver.VerificationServer;

import java.util.logging.Level;

import org.spacehq.mc.auth.data.GameProfile;
import org.spacehq.mc.protocol.MinecraftConstants;
import org.spacehq.mc.protocol.data.message.TextMessage;
import org.spacehq.mc.protocol.data.status.PlayerInfo;
import org.spacehq.mc.protocol.data.status.ServerStatusInfo;
import org.spacehq.mc.protocol.data.status.VersionInfo;
import org.spacehq.mc.protocol.data.status.handler.ServerInfoBuilder;
import org.spacehq.packetlib.Session;

public class ServerInfoListener implements ServerInfoBuilder {

    @Override
    public ServerStatusInfo buildInfo(Session session) {
        VerificationServer.getLogger().log(Level.INFO, "Pinging client: {0}", session);

        VersionInfo versionInfo = new VersionInfo(MinecraftConstants.GAME_VERSION, MinecraftConstants.PROTOCOL_VERSION);
        PlayerInfo playerInfo = new PlayerInfo(-1, -1, new GameProfile[0]);
        TextMessage textMessage = new TextMessage("Hello world!");

        return new ServerStatusInfo(versionInfo, playerInfo, textMessage, null);
    }
}
