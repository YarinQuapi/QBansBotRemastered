package me.yarinlevi.qbansbotremastered.mysql;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import me.yarinlevi.qbansbotremastered.QBansBot;
import me.yarinlevi.qbansbotremastered.configuration.Configuration;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author YarinQuapi
 */
public class MySQLUtils {
    @Getter private final Connection connection;

    public MySQLUtils(Configuration config) {
        String hostName = config.getString("mysql_host");
        String database = config.getString("mysql_database");
        int port = config.getInt("mysql_port");
        String user = config.getString("mysql_user");
        String pass = config.getString("mysql_pass");


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


        String sql = "CREATE TABLE IF NOT EXISTS `bans` (`guildId` VARCHAR(18) NOT NULL, `userId` VARCHAR(18) NOT NULL, `messageId` VARCHAR(18) NOT NULL, `staff` VARCHAR(18) NOT NULL, `timestamp` TEXT NOT NULL, `reason` TEXT)";
        String enabled_servers_sql = "CREATE TABLE IF NOT EXISTS `enabled_servers` (`guildId` VARCHAR(18) NOT NULL)";


        Connection conn = null;

        System.out.println("Please await mysql hook...");
        try {
            conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
            {
                statement.executeUpdate(sql);
                statement.executeUpdate(enabled_servers_sql);
                System.out.println("Successfully connected to MySQL database!");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            System.out.println("Something went horribly wrong while connecting to database!");
        }

        this.connection = conn;
    }

    @Nullable
    public static ResultSet get(String query) {
        try {
            return QBansBot.getInstance().getMysql().connection.prepareStatement(query).executeQuery();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static int update(String query) {
        try {
            return QBansBot.getInstance().getMysql().connection.prepareStatement(query).executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    public static boolean insert(String query) {
        try {
            QBansBot.getInstance().getMysql().connection.prepareStatement(query).execute();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }
}
