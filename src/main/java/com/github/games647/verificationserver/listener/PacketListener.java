package com.github.games647.verificationserver.listener;

import com.github.games647.verificationserver.Config;
import com.github.games647.verificationserver.VerificationServer;

import org.spacehq.mc.protocol.data.handshake.HandshakeIntent;
import org.spacehq.mc.protocol.packet.handshake.client.HandshakePacket;
import org.spacehq.packetlib.Session;
import org.spacehq.packetlib.event.session.PacketReceivedEvent;
import org.spacehq.packetlib.event.session.SessionAdapter;
import org.spacehq.packetlib.packet.Packet;

public class PacketListener extends SessionAdapter {

    private final VerificationServer verificationServer;

    private final boolean guessProtocol;

    public PacketListener(VerificationServer verificationServer, Config config) {
        this.verificationServer = verificationServer;

        this.guessProtocol = Boolean.parseBoolean(config.get("guessProtocol"));
    }

    @Override
    public void packetReceived(PacketReceivedEvent receiveEvent) {
        Packet packet = receiveEvent.getPacket();
        if (guessProtocol && packet instanceof HandshakePacket) {
            HandshakePacket handshakePacket = (HandshakePacket) packet;
            if (handshakePacket.getIntent() == HandshakeIntent.STATUS) {
                Session session = receiveEvent.getSession();
                int protocolVersion = handshakePacket.getProtocolVersion();
                verificationServer.getProtocolVersions().put(session, protocolVersion);
            }
        }

        super.packetReceived(receiveEvent);
    }
}
