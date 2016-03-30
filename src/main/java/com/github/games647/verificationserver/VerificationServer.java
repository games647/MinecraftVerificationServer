package com.github.games647.verificationserver;

import com.github.games647.verificationserver.listener.ConnectionListener;
import com.github.games647.verificationserver.listener.LoginListener;
import com.github.games647.verificationserver.listener.ServerInfoListener;

import java.util.logging.Logger;

import org.spacehq.mc.protocol.MinecraftConstants;
import org.spacehq.mc.protocol.MinecraftProtocol;
import org.spacehq.packetlib.Server;
import org.spacehq.packetlib.tcp.TcpSessionFactory;

public class VerificationServer {

    private static final Logger LOGGER = Logger.getLogger(VerificationServer.class.getName());

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 25565;

    public static void main(String[] args) throws Exception {
        VerificationServer server = new VerificationServer(HOST, PORT);
        server.startServer();

        while (!Thread.currentThread().isInterrupted()) {
            Thread.sleep(1_000);
        }
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    private final Server server;

    public VerificationServer(String hostname, int port) {
        this.server = new Server(hostname, port, MinecraftProtocol.class, new TcpSessionFactory());

        //activae online mode
        server.setGlobalFlag(MinecraftConstants.VERIFY_USERS_KEY, true);
        server.setGlobalFlag(MinecraftConstants.SERVER_COMPRESSION_THRESHOLD, 100);

        //motd
        server.setGlobalFlag(MinecraftConstants.SERVER_INFO_BUILDER_KEY, new ServerInfoListener());

        server.setGlobalFlag(MinecraftConstants.SERVER_LOGIN_HANDLER_KEY, new LoginListener());
        server.addListener(new ConnectionListener());
    }

    public void startServer() {
        LOGGER.finer("Binding server");
        server.bind(true);
        LOGGER.info("Server started");
    }
}
