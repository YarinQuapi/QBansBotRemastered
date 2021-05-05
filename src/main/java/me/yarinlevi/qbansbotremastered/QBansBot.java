package me.yarinlevi.qbansbotremastered;

import lombok.Getter;
import me.yarinlevi.qbansbotremastered.configuration.Configuration;
import me.yarinlevi.qbansbotremastered.listeners.OnGuildBanEvent;
import me.yarinlevi.qbansbotremastered.listeners.OnGuildUnbanEvent;
import me.yarinlevi.qbansbotremastered.mysql.GuildBanRemover;
import me.yarinlevi.qbansbotremastered.mysql.MySQLUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.Map;

public class QBansBot {
    @Getter private static QBansBot instance;
    @Getter private final JDA jda;
    @Getter private final MySQLUtils mysql;
    @Getter private final GuildBanRemover guildBanRemover;

    /**
     *  Configurations *
     */
    @Getter private final Map<String, Configuration> configurations = new HashMap<>();

    public QBansBot() throws LoginException {
        instance = this;

        this.loadConfigs();

        mysql = new MySQLUtils(this.configurations.get("mysql"));

        String token = configurations.get("qbot").getString("token");

        JDABuilder jdaBuilder = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_BANS)
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.of(Activity.ActivityType.DEFAULT, "QBansOS v" + configurations.get("qbot").getString("version")))
                .addEventListeners(new OnGuildBanEvent())
                .addEventListeners(new OnGuildUnbanEvent());


        jda = jdaBuilder.build();

        guildBanRemover = new GuildBanRemover();
    }


    private void loadConfigs() {
        Configuration config = Configuration.load("qbot.yml");
        configurations.put("qbot", config);

        Configuration mysql = Configuration.load("mysql.yml");
        configurations.put("mysql", mysql);
    }
}
