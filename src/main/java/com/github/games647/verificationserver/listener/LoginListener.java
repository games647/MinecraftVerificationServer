package com.github.games647.verificationserver.listener;

import com.github.games647.verificationserver.Config;
import com.github.games647.verificationserver.NamedParameterStatement;
import com.github.games647.verificationserver.VerificationServer;
import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.ServerLoginHandler;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerDisconnectPacket;
import com.github.steveice10.packetlib.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class LoginListener implements ServerLoginHandler {

    protected final VerificationServer verificationServer;
    protected final String updateUsers;
    protected final String updateToken;

    private final String kickMessage;
    private final String errorMessage;

    private final boolean shutdownOnError;

    public LoginListener(VerificationServer verificationServer, Config config) {
        this.verificationServer = verificationServer;

        this.updateUsers = config.get("updateUserTable");
        this.updateToken = config.get("updateTokenTable");

        this.kickMessage = config.get("kickMessage");
        this.errorMessage = config.get("errorMessage");

        this.shutdownOnError = Boolean.parseBoolean(config.get("shutdownOnError"));
    }

    @Override
    public void loggedIn(Session session) {
        String host = session.getHost();

        GameProfile profile = session.getFlag(MinecraftConstants.PROFILE_KEY);
        final UUID uuid = profile.getId();
        final String username = profile.getName();

        VerificationServer.getLogger().info("Session verified: {} {}", uuid, username);
        verificationServer.getExecutorService().execute(() -> saveVerification(uuid, username, host, session));
    }

    private void saveVerification(UUID uuid, String username, String host, Session session) {
        String token = verificationServer.getTokenGenerator().generateToken();
        try (Connection con = verificationServer.getDataSource().getConnection()) {
            con.setAutoCommit(false);

            if (!updateUsers.trim().isEmpty()) {
                    NamedParameterStatement statement = new NamedParameterStatement(con, updateUsers
                        , PreparedStatement.RETURN_GENERATED_KEYS);
                setParameters(statement, uuid, username, host, token);
                statement.execute();
            }

            if (!updateToken.trim().isEmpty()) {
                NamedParameterStatement statement = new NamedParameterStatement(con, updateToken);
                setParameters(statement, uuid, username, host, token);

                statement.executeUpdate();
            }

            //save it only if the all data are saved
            con.commit();
            con.setAutoCommit(true);
            onSuccess(session, token);
        } catch (SQLException sqlEx) {
            VerificationServer.getLogger().error("Error updating verifications status", sqlEx);
            onFailure(session);
        }
    }

    private void setParameters(NamedParameterStatement statement, UUID uuid, String username, String ip, String token)
            throws SQLException {
        if (statement.getIndexes("uuid") != null) {
            statement.setString("uuid", uuid.toString());
        }

        if (statement.getIndexes("username") != null) {
            statement.setString("username", username);
        }

        if (statement.getIndexes("ip") != null) {
            statement.setString("ip", ip);
        }

        if (statement.getIndexes("premium") != null) {
            statement.setObject("premium", true);
        }

        if (statement.getIndexes("token") != null) {
            statement.setString("token", token);
        }
    }

    private void onSuccess(Session session, String token) {
        String message = kickMessage.replace("%code", token);
        ServerDisconnectPacket kickPacket = new ServerDisconnectPacket(message);
        session.send(kickPacket);
    }

    private void onFailure(Session session) {
        ServerDisconnectPacket errorKickPacket = new ServerDisconnectPacket(errorMessage);
        session.send(errorKickPacket);
        if (shutdownOnError) {
            VerificationServer.getLogger().warn("Shuting down server because an error occured");
            VerificationServer.stop();
        }
    }
}
