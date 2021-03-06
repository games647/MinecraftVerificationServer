package com.github.games647.verificationserver;

import com.github.games647.verificationserver.listener.ConnectionListener;
import com.github.games647.verificationserver.listener.LoginListener;
import com.github.games647.verificationserver.listener.ServerInfoListener;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.packetlib.Server;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerificationServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerificationServer.class);

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 25_565;

    private static final AtomicBoolean running = new AtomicBoolean(true);

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        config.loadFile();

        String logName = LOGGER.getName();
        java.util.logging.Logger.getLogger(logName).setLevel(Level.parse(config.get("logLevel")));

        VerificationServer server = new VerificationServer(config);
        server.startServer();
        server.initDatabase();
        server.createTable();

        while (!Thread.currentThread().isInterrupted() && running.get()) {
            TimeUnit.SECONDS.sleep(1);
        }
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static void stop() {
        running.getAndSet(false);
    }

    private final Server server = new Server(HOST, PORT, MinecraftProtocol.class, new TcpSessionFactory());
    private final Config config;
    private final TokenGenerator tokenGenerator;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Map<String, Integer> protocolVersions = new ConcurrentHashMap<>();

    private HikariDataSource dataSource;

    public VerificationServer(Config config) throws IOException {
        this.config = config;
        this.tokenGenerator = new TokenGenerator(Integer.parseInt(config.get("tokenLength")));

        //activate online mode
        server.setGlobalFlag(MinecraftConstants.VERIFY_USERS_KEY, true);
        server.setGlobalFlag(MinecraftConstants.SERVER_COMPRESSION_THRESHOLD, 100);

        //motd
        server.setGlobalFlag(MinecraftConstants.SERVER_INFO_BUILDER_KEY, new ServerInfoListener(this, config));

        server.setGlobalFlag(MinecraftConstants.SERVER_LOGIN_HANDLER_KEY, new LoginListener(this, config));
        server.addListener(new ConnectionListener(this, config));
    }

    public void startServer() {
        LOGGER.debug("Binding server");
        server.bind(true);
        LOGGER.info("Server started");
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

        databaseConfig.setUsername("root");
        databaseConfig.setPassword(config.get("password"));
        databaseConfig.setConnectionTimeout(Long.parseLong(config.get("connectionTimeout")));

        if (driverType.contains("mysql")) {
            // default prepStmtCacheSize 25 - amount of cached statements - enough for us
            // default prepStmtCacheSqlLimit 256 - length of SQL - our queries are not longer
            // disabled by default - will return the same prepared statement instance
            databaseConfig.addDataSourceProperty("cachePrepStmts", true);
            // default false - available in newer versions caches the statements server-side
            databaseConfig.addDataSourceProperty("useServerPrepStmts", true);
        }

        dataSource = new HikariDataSource(databaseConfig);
    }

    public void createTable() throws SQLException {
        String createUsers = config.get("createUserTable");
        String createToken = config.get("createTokenTable");
        if (createToken.isEmpty() && createUsers.isEmpty()) {
            throw new IllegalStateException("No query specified");
        }

        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute(createUsers);
            stmt.execute(createToken);

            LOGGER.info("Finished database setup");
        } catch (SQLException sqlEx) {
            LOGGER.error("Failed to create Table", sqlEx);
            //rethrow it to abort execution
            throw sqlEx;
        }
    }

    public void close() throws InterruptedException {
        server.close();

        if (dataSource != null) {
            //end the last queries
            executor.awaitTermination(dataSource.getConnectionTimeout(), TimeUnit.MILLISECONDS);
            dataSource.close();
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public TokenGenerator getTokenGenerator() {
        return tokenGenerator;
    }

    public Map<String, Integer> getProtocolVersions() {
        return protocolVersions;
    }

    public Executor getExecutor() {
        return executor;
    }
}
