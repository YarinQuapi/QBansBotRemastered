package me.yarinlevi.qbansbotremastered.listeners;

import me.yarinlevi.qbansbotremastered.QBansBot;
import me.yarinlevi.qbansbotremastered.mysql.MySQLUtils;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OnReactionEvent extends ListenerAdapter {

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        boolean isBot = QBansBot.getInstance().getJda().getUserById(event.getUserId()).isBot();
        boolean isChannel = event.getTextChannel().getName().equalsIgnoreCase("qbansbot_logs");
        boolean isEmoji = event.getReactionEmote().getName().equalsIgnoreCase(":x:");

        String msg = isBot + ", " + isChannel + ", " + isEmoji + "\n```\n" + event.getReactionEmote().getEmoji() + "\n```";

        event.getChannel().sendMessage(msg).queue();

        if (!QBansBot.getInstance().getJda().getUserById(event.getUserId()).isBot() && event.getTextChannel().getName().equalsIgnoreCase("qbansbot_logs") && event.getReactionEmote().getAsReactionCode().equals("")) {
            long id = event.getMessageIdLong();

            String sql = "SELECT 1 FROM `bans` WHERE `messageId`=\"" + id + "\"";

            Thread thr = new Thread(() -> {
                ResultSet rs = MySQLUtils.get(sql);

                if (rs != null) {
                    try {
                        if (rs.next()) {
                            do {
                                String userId = rs.getString("userId");
                                long guildId = rs.getLong("guildId");

                                QBansBot.getInstance().getJda().getGuildById(guildId).unban(userId).queue();

                                MySQLUtils.update(String.format("DELETE * FROM `bans` WHERE `guildId`=\"%s\" AND `userId`=\"%s\" AND `messageId`=\"%s\"", guildId, userId, id));

                            } while (rs.next());
                        }
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            });

            thr.start();
        } else {
            String str = "message id: " + event.getMessageId() + ", emote id: " + event.getReactionEmote().getName();

            event.getChannel().sendMessage(str).queue();
        }
    }
}
