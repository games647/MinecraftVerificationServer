package com.github.games647.verificationserver.listener;

import com.github.games647.verificationserver.VerificationServer;

import java.util.UUID;

import org.spacehq.mc.auth.data.GameProfile;
import org.spacehq.mc.protocol.MinecraftConstants;

import org.spacehq.mc.protocol.ServerLoginHandler;
import org.spacehq.mc.protocol.packet.ingame.server.ServerDisconnectPacket;
import org.spacehq.packetlib.Session;

public class LoginListener implements ServerLoginHandler {

    @Override
    public void loggedIn(Session session) {
        GameProfile profile = session.getFlag(MinecraftConstants.PROFILE_KEY);
        UUID uuid = profile.getId();
        String username = profile.getName();

        VerificationServer.getLogger().info("Session verified: {} {}", uuid, username);

        ServerDisconnectPacket kickPacket = new ServerDisconnectPacket("Session verified");
        session.send(kickPacket);
    }
}
