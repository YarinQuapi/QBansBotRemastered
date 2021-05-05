package me.yarinlevi.qbansbotremastered.listeners;

import me.yarinlevi.qbansbotremastered.mysql.MySQLUtils;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class OnGuildUnbanEvent extends ListenerAdapter {
    public void onGuildUnban(GuildUnbanEvent unbanEvent) {
        if (!unbanEvent.getUser().isBot()) {
            long bannedUserId = unbanEvent.getUser().getIdLong();
            long guildId = unbanEvent.getGuild().getIdLong();

            new Thread(() -> MySQLUtils.update(String.format("DELETE FROM `bans` WHERE `userId`=\"%s\" AND `guildId`=\"%s\"", bannedUserId, guildId))).start();
        }
    }
}
