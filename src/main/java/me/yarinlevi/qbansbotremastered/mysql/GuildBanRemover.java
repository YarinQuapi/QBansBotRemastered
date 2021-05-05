package me.yarinlevi.qbansbotremastered.mysql;

import me.yarinlevi.qbansbotremastered.QBansBot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

public class GuildBanRemover {
    private final Thread removerThread;

    public GuildBanRemover() {
        removerThread = new Thread(() -> {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    remove();
                }
            }, (long) 0, (long) 60*1000);
        });

        removerThread.start();
    }

    /**
     * RUN ASYNC!
     */
    public void remove() {
        String sql = String.format("SELECT * FROM `bans` WHERE `timestamp`<\"%s\"", System.currentTimeMillis());

        ResultSet rs = MySQL.get(sql);

        if (rs != null) {
            try {
                if (!rs.next()) {
                    System.out.println("No need to remove any bans.");
                } else {
                    do {
                        String userId = rs.getString("userId");
                        long guildId = rs.getLong("guildId");


                        QBansBot.getInstance().getJda().getGuildById(guildId).unban(userId).queue();

                        MySQL.update(String.format("DELETE FROM `bans` WHERE `userId`=\"%s\" AND `guildId`=\"%s\"", userId, guildId));
                    } while (rs.next());
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public void stop() {
        removerThread.suspend();
    }

    public void resume() {
        removerThread.resume();
    }
}
