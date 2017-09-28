package com.github.games647.verificationserver.listener;

import com.github.games647.verificationserver.Config;
import com.github.games647.verificationserver.VerificationServer;
import com.github.steveice10.mc.protocol.data.handshake.HandshakeIntent;
import com.github.steveice10.mc.protocol.packet.handshake.client.HandshakePacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;

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
