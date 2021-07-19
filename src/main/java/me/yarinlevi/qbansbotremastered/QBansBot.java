package me.yarinlevi.qbansbotremastered;

import lombok.Getter;
import lombok.Setter;
import me.yarinlevi.qbansbotremastered.configuration.Configuration;
import me.yarinlevi.qbansbotremastered.listeners.OnGuildBanEvent;
import me.yarinlevi.qbansbotremastered.listeners.OnGuildLeaveEvent;
import me.yarinlevi.qbansbotremastered.listeners.OnGuildUnbanEvent;
import me.yarinlevi.qbansbotremastered.listeners.OnReactionEvent;
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
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author YarinQuapi
 */
public class QBansBot {
    @Getter private static QBansBot instance;
    @Getter private final JDA jda;
    @Getter private final MySQLUtils mysql;
    @Getter @Setter private GuildBanRemover guildBanRemover;
    @Getter private final String version;


    /**
     *  Configurations *
     */
    @Getter private final Map<String, Configuration> configurations = new HashMap<>();

    public QBansBot() throws LoginException {
        QBansBot.instance = this;

        this.loadConfigs();

        mysql = new MySQLUtils(this.configurations.get("mysql"));

        String token = configurations.get("qbot").getString("token");

        version = configurations.get("qbot").getString("version");

        JDABuilder jdaBuilder = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_BANS, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.of(Activity.ActivityType.DEFAULT, "QBansOS v" + version))
                .addEventListeners(new OnGuildBanEvent())
                .addEventListeners(new OnGuildUnbanEvent())
                .addEventListeners(new OnGuildLeaveEvent())
                .addEventListeners(new OnReactionEvent());


        jda = jdaBuilder.build();

        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                QBansBot.getInstance().setGuildBanRemover(new GuildBanRemover());
            }
        }, 10000);
    }


    private void loadConfigs() {
        Configuration config = Configuration.load("qbot.yml");
        configurations.put("qbot", config);

        Configuration mysql = Configuration.load("mysql.yml");
        configurations.put("mysql", mysql);
    }
}
