package com.github.games647.verificationserver.listener;

import com.github.games647.verificationserver.Config;
import com.github.games647.verificationserver.VerificationServer;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.server.ServerAdapter;
import com.github.steveice10.packetlib.event.server.SessionAddedEvent;
import com.github.steveice10.packetlib.event.server.SessionRemovedEvent;

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

        VerificationServer.getLogger().debug("Connecting client: {}", session.getHost());

        super.sessionAdded(sessionAddedEvent);
    }

    @Override
    public void sessionRemoved(SessionRemovedEvent sessionRemovedEvent) {
        Session session = sessionRemovedEvent.getSession();

        String host = session.getHost();
        VerificationServer.getLogger().info("Disconnecting client: {}", host);
        verificationServer.getProtocolVersions().remove(host);

        super.sessionRemoved(sessionRemovedEvent);
    }
}
