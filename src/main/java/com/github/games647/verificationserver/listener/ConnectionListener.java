package com.github.games647.verificationserver.listener;

import com.github.games647.verificationserver.VerificationServer;

import java.util.logging.Level;

import org.spacehq.packetlib.Session;
import org.spacehq.packetlib.event.server.ServerAdapter;
import org.spacehq.packetlib.event.server.SessionAddedEvent;
import org.spacehq.packetlib.event.server.SessionRemovedEvent;

public class ConnectionListener extends ServerAdapter {

    @Override
    public void sessionAdded(SessionAddedEvent sessionAddedEvent) {
        Session session = sessionAddedEvent.getSession();
        VerificationServer.getLogger().log(Level.FINE, "Connecting client: {0}", session);

        super.sessionAdded(sessionAddedEvent);
    }

    @Override
    public void sessionRemoved(SessionRemovedEvent sessionRemovedEvent) {
        Session session = sessionRemovedEvent.getSession();
        VerificationServer.getLogger().log(Level.INFO, "Disconnecting client: {0}", session);

        super.sessionRemoved(sessionRemovedEvent);
    }
}
