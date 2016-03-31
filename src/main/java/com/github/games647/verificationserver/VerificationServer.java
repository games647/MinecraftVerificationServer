package com.github.games647.verificationserver;

import com.github.games647.verificationserver.listener.ConnectionListener;
import com.github.games647.verificationserver.listener.LoginListener;
import com.github.games647.verificationserver.listener.ServerInfoListener;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spacehq.mc.protocol.MinecraftConstants;
import org.spacehq.mc.protocol.MinecraftProtocol;
import org.spacehq.packetlib.Server;
import org.spacehq.packetlib.tcp.TcpSessionFactory;

public class VerificationServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerificationServer.class);

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 25565;

    public static void main(String[] args) throws Exception {
        VerificationServer server = null;
        try {
            Config config = new Config();
            config.loadFile();
            config.verify();

            server = new VerificationServer(config);
            server.startServer();
            server.initDatabase();
            server.createTable();

            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(1_000);
            }
        } finally {
            if (server != null) {
                HikariDataSource dataSource = server.getDataSource();
                if (dataSource != null) {
                    dataSource.close();
                }
            }
        }
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    private final Server server = new Server(HOST, PORT, MinecraftProtocol.class, new TcpSessionFactory());
    private final Config config;
    private final TokenGenerator tokenGenerator;

    private HikariDataSource dataSource;

    public VerificationServer(Config config) {
        this.config = config;
        this.tokenGenerator = new TokenGenerator(Integer.parseInt(config.get("tokenLength")));

        //activae online mode
        server.setGlobalFlag(MinecraftConstants.VERIFY_USERS_KEY, true);
        server.setGlobalFlag(MinecraftConstants.SERVER_COMPRESSION_THRESHOLD, 100);

        //motd
        server.setGlobalFlag(MinecraftConstants.SERVER_INFO_BUILDER_KEY, new ServerInfoListener(config));

        server.setGlobalFlag(MinecraftConstants.SERVER_LOGIN_HANDLER_KEY, new LoginListener());
        server.addListener(new ConnectionListener());
    }

    public void startServer() {
        if (!server.isListening()) {
            LOGGER.debug("Binding server");
            server.bind(true);
            LOGGER.info("Server started");
        }
    }

    public void initDatabase() {
        LOGGER.debug("Connecting to database");
        HikariConfig databaseConfig = new HikariConfig();

        String driverType = config.get("driverType");
        String host = config.get("host");
        String port = config.get("port");
        String database = config.get("database");
        String jdbcUrl = "jdbc:" + driverType + "://" + host + ':' + port + '/' + database;
        databaseConfig.setJdbcUrl(jdbcUrl);

        databaseConfig.setUsername(config.get("username"));
        databaseConfig.setPassword(config.get("password"));
        databaseConfig.setConnectionTimeout(Long.parseLong(config.get("connectionTimeout")));

        dataSource = new HikariDataSource(databaseConfig);
    }

    public void createTable() throws SQLException {
        String createUsers = config.get("createUserTable");
        String createToken = config.get("createTokenTable");
        if (createToken.isEmpty() && createUsers.isEmpty()) {
            throw new IllegalStateException("No query specified");
        }

        Connection con = null;
        try {
            con = dataSource.getConnection();
            Statement statement = con.createStatement();
            statement.execute(createUsers);
            statement.execute(createToken);
        } catch (SQLException sqlEx) {
            LOGGER.error("Failed to create Table", sqlEx);
            //rethrow it to abort execution
            throw sqlEx;
        } finally {
            if (con != null) {
                con.close();
            }
        }
    }

    public Config getConfig() {
        return config;
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public TokenGenerator getTokenGenerator() {
        return tokenGenerator;
    }
}
