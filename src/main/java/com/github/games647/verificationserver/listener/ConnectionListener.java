package com.github.games647.verificationserver.listener;

import com.github.games647.verificationserver.VerificationServer;

import org.spacehq.packetlib.Session;
import org.spacehq.packetlib.event.server.ServerAdapter;
import org.spacehq.packetlib.event.server.SessionAddedEvent;
import org.spacehq.packetlib.event.server.SessionRemovedEvent;

public class ConnectionListener extends ServerAdapter {

    @Override
    public void sessionAdded(SessionAddedEvent sessionAddedEvent) {
        Session session = sessionAddedEvent.getSession();
        session.addListener(new PacketListener());

        VerificationServer.getLogger().debug("Connecting client: {0}", session);

        super.sessionAdded(sessionAddedEvent);
    }

    @Override
    public void sessionRemoved(SessionRemovedEvent sessionRemovedEvent) {
        Session session = sessionRemovedEvent.getSession();
        VerificationServer.getLogger().info("Disconnecting client: {0}", session);

        super.sessionRemoved(sessionRemovedEvent);
    }
}
