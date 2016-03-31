package com.github.games647.verificationserver.listener;

import org.spacehq.mc.protocol.data.handshake.HandshakeIntent;
import org.spacehq.mc.protocol.packet.handshake.client.HandshakePacket;
import org.spacehq.packetlib.event.session.PacketReceivedEvent;
import org.spacehq.packetlib.event.session.PacketSentEvent;
import org.spacehq.packetlib.event.session.SessionAdapter;
import org.spacehq.packetlib.packet.Packet;

public class PacketListener extends SessionAdapter {

    @Override
    public void packetSent(PacketSentEvent sentEvent) {
        super.packetSent(sentEvent);
    }

    @Override
    public void packetReceived(PacketReceivedEvent receiveEvent) {
        Packet packet = receiveEvent.getPacket();
        if (packet instanceof HandshakePacket) {
            HandshakePacket handshakePacket = (HandshakePacket) packet;
            if (handshakePacket.getIntent() == HandshakeIntent.STATUS) {
                int protocolVersion = handshakePacket.getProtocolVersion();
            }
        }

        super.packetReceived(receiveEvent);
    }
}
