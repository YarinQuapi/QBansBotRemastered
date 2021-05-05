package me.yarinlevi.qbansbotremastered.mysql;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import me.yarinlevi.qbansbotremastered.configuration.Configuration;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL {
    @Getter private static Connection connection;

    public MySQL(Configuration config) {
        String hostName = config.getString("mysql_host");
        String table = "bans";
        String database = config.getString("mysql_database");
        int port = config.getInt("mysql_port");
        String user = config.getString("mysql_user");
        String pass = config.getString("mysql_pass");


        /*
        String hostName = "38.17.53.116";
        String table = "bans";
        String database = "QBansTest";
        int port = 30208;
        String user = "QBansBot";
        String pass = "PASSWORD123";
         */

        HikariDataSource dataSource = new HikariDataSource();

        dataSource.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        dataSource.addDataSourceProperty("serverName", hostName);
        dataSource.addDataSourceProperty("port", port);
        dataSource.addDataSourceProperty("databaseName", database);
        dataSource.addDataSourceProperty("user", user);
        dataSource.addDataSourceProperty("password", pass);
        dataSource.addDataSourceProperty("useSSL", false);
        dataSource.addDataSourceProperty("autoReconnect", true);
        dataSource.addDataSourceProperty("useUnicode", true);
        dataSource.addDataSourceProperty("characterEncoding", "UTF-8");

        String sql = String.format("CREATE TABLE IF NOT EXISTS `%s` (`guildId` VARCHAR(18) NOT NULL, `userId` VARCHAR(18) NOT NULL, `staff` VARCHAR(18) NOT NULL, `timestamp` TEXT NOT NULL)", table);

        System.out.println("Please await mysql hook...");
        try {
            connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            {
                statement.executeUpdate(sql);
                System.out.println("Successfully connected to MySQL database!");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            System.out.println("Something went horribly wrong while connecting to database!");
        }
    }

    @Nullable
    public static ResultSet get(String query) {
        try {
            return connection.prepareStatement(query).executeQuery();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static int update(String query) {
        try {
            return connection.prepareStatement(query).executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    public static boolean insert(String query) {
        try {
            connection.prepareStatement(query).execute();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }
}