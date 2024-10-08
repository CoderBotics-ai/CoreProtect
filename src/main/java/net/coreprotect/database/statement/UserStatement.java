package net.coreprotect.database.statement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.Database;

public class UserStatement {

    private UserStatement() {
        throw new IllegalStateException("Database class");
    }

    public static int insert(Connection connection, String user) {
        int id = -1;

        try {
            int unixtimestamp = (int) (System.currentTimeMillis() / 1000L);

            String query = "INSERT INTO " + ConfigHandler.prefix + "user (time, user) VALUES (?, ?)";
            PreparedStatement preparedStmt = Database.hasReturningKeys() 
                ? connection.prepareStatement(query + " RETURNING rowid") 
                : connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            preparedStmt.setInt(1, unixtimestamp);
            preparedStmt.setString(2, user);

            if (Database.hasReturningKeys()) {
                try (ResultSet resultSet = preparedStmt.executeQuery()) {
                    if (resultSet.next()) {
                        id = resultSet.getInt(1);
                    }
                }
            } else {
                preparedStmt.executeUpdate();
                try (ResultSet keys = preparedStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        id = keys.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    public static int getId(PreparedStatement preparedStatement, String user, boolean load) throws SQLException {
        if (load && !ConfigHandler.playerIdCache.containsKey(user.toLowerCase(Locale.ROOT))) {
            UserStatement.loadId(preparedStatement.getConnection(), user, null);
        }

        return ConfigHandler.playerIdCache.getOrDefault(user.toLowerCase(Locale.ROOT), -1);
    }

    public static int loadId(Connection connection, String user, String uuid) {
        int id = -1;

        try {
            String collate = Config.getGlobal().MYSQL ? "" : " COLLATE NOCASE";
            String where = "user = ?" + collate;
            if (uuid != null) {
                where += " OR uuid = ?";
            }

            String query = "SELECT rowid as id, uuid FROM " + ConfigHandler.prefix + "user WHERE " + where + " ORDER BY rowid ASC LIMIT 1";
            try (PreparedStatement preparedStmt = connection.prepareStatement(query)) {
                preparedStmt.setString(1, user);
                if (uuid != null) {
                    preparedStmt.setString(2, uuid);
                }

                try (ResultSet resultSet = preparedStmt.executeQuery()) {
                    if (resultSet.next()) {
                        id = resultSet.getInt("id");
                        uuid = resultSet.getString("uuid");
                    }
                }
            }

            if (id == -1) {
                id = insert(connection, user);
            }

            ConfigHandler.playerIdCache.put(user.toLowerCase(Locale.ROOT), id);
            ConfigHandler.playerIdCacheReversed.put(id, user);
            if (uuid != null) {
                ConfigHandler.uuidCache.put(user.toLowerCase(Locale.ROOT), uuid);
                ConfigHandler.uuidCacheReversed.put(uuid, user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return id;
    }

    public static String loadName(Connection connection, int id) {
        String user = "";
        String uuid = null;

        try {
            String query = "SELECT user, uuid FROM " + ConfigHandler.prefix + "user WHERE rowid=? LIMIT 1";
            try (PreparedStatement preparedStmt = connection.prepareStatement(query)) {
                preparedStmt.setInt(1, id);

                try (ResultSet resultSet = preparedStmt.executeQuery()) {
                    if (resultSet.next()) {
                        user = resultSet.getString("user");
                        uuid = resultSet.getString("uuid");
                    }
                }
            }

            if (!user.isEmpty()) {
                ConfigHandler.playerIdCache.put(user.toLowerCase(Locale.ROOT), id);
                ConfigHandler.playerIdCacheReversed.put(id, user);
                if (uuid != null) {
                    ConfigHandler.uuidCache.put(user.toLowerCase(Locale.ROOT), uuid);
                    ConfigHandler.uuidCacheReversed.put(uuid, user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }
}