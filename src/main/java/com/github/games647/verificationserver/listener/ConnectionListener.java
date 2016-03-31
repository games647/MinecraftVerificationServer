package com.github.games647.verificationserver.listener;

import com.github.games647.verificationserver.Config;
import com.github.games647.verificationserver.VerificationServer;

import org.spacehq.packetlib.Session;
import org.spacehq.packetlib.event.server.ServerAdapter;
import org.spacehq.packetlib.event.server.SessionAddedEvent;
import org.spacehq.packetlib.event.server.SessionRemovedEvent;

public class ConnectionListener extends ServerAdapter {

    private final VerificationServer verificationServer;
    private final Config config;

    public ConnectionListener(VerificationServer verificationServer, Config config) {
        this.verificationServer = verificationServer;
        this.config = config;
    }

    @Override
    public void sessionAdded(SessionAddedEvent sessionAddedEvent) {
        Session session = sessionAddedEvent.getSession();
        session.addListener(new PacketListener(verificationServer, config));

        VerificationServer.getLogger().debug("Connecting client: {0}", session);

        super.sessionAdded(sessionAddedEvent);
    }

    @Override
    public void sessionRemoved(SessionRemovedEvent sessionRemovedEvent) {
        Session session = sessionRemovedEvent.getSession();
        VerificationServer.getLogger().info("Disconnecting client: {0}", session);
        verificationServer.getProtocolVersions().remove(session);

        super.sessionRemoved(sessionRemovedEvent);
    }
}
