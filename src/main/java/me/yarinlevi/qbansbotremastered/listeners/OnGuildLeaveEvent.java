package me.yarinlevi.qbansbotremastered.listeners;

import me.yarinlevi.qbansbotremastered.mysql.MySQLUtils;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class OnGuildLeaveEvent extends ListenerAdapter {
    public void onGuildLeave(@NotNull GuildLeaveEvent leaveEvent) {
        new Thread(() -> MySQLUtils.update(String.format("DELETE * FROM `bans` WHERE `guildId`=\"%s\"", leaveEvent.getGuild().getIdLong()))).start();
    }
}
