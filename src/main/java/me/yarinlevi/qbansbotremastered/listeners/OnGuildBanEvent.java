package me.yarinlevi.qbansbotremastered.listeners;

import me.yarinlevi.qbansbotremastered.QBansBot;
import me.yarinlevi.qbansbotremastered.exceptions.DurationNotDetectedException;
import me.yarinlevi.qbansbotremastered.mysql.MySQLUtils;
import me.yarinlevi.qbansbotremastered.utilities.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author YarinQuapi
 */
public class OnGuildBanEvent extends ListenerAdapter {
    long messageId;

    @Override
    public void onGuildBan(@NotNull GuildBanEvent banEvent) {
        if (!banEvent.getUser().isBot()) {
            ResultSet rs = MySQLUtils.get("SELECT COUNT(*) as count FROM `enabled_servers` WHERE `guildId`=\"" + banEvent.getGuild().getIdLong() + "\"");
            boolean authorized = false;

            if (rs == null) return;

            try {
                rs.next();
                authorized = rs.getInt("count") > 0;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            if (authorized) {
                // Getting latest entry
                banEvent.getGuild().retrieveAuditLogs().limit(1).type(ActionType.BAN).queue(auditLogEntries -> {
                    AuditLogEntry entry = auditLogEntries.get(0); // Selecting the last entry
                    boolean validEntry = entry.getTargetId().equals(banEvent.getUser().getId()); // Is the  entry real?
                    String reason = validEntry ? entry.getReason() : "permanent ban"; // The reason for the ban
                    User executor = validEntry ? entry.getUser() : QBansBot.getInstance().getJda().getSelfUser(); // Who banned the user or if not found, the bot

                    if (reason != null) {
                        // Correctly splitting arguments
                        String[] args = StringUtils.split(reason);

                        if (args.length > 0) {
                            // Parsing ban duration
                            Timestamp duration = null;
                            boolean perm = false;

                            try {
                                duration = new Timestamp(StringUtils.parseDuration(args[0]));
                            } catch (DurationNotDetectedException e) {
                                perm = true;
                            }

                            MessageChannel messageChannel;
                            if (banEvent.getGuild().getTextChannels().stream().anyMatch(x -> x.getName().equals("qbansbot_logs"))) {
                                messageChannel = banEvent.getGuild().getTextChannels().stream().filter(x -> x.getName().equals("qbansbot_logs")).findAny().get();
                            } else {
                                banEvent.getGuild().createTextChannel("qbansbot_logs")
                                        .addPermissionOverride(banEvent.getGuild().getPublicRole(), 0, Permission.ALL_GUILD_PERMISSIONS)
                                        .queue();
                                messageChannel = banEvent.getGuild().getTextChannels().stream().filter(x -> x.getName().equals("qbansbot_logs")).findFirst().get();
                            }

                            // Reason construction
                            StringBuilder constructedReason = new StringBuilder();

                            for (int i = 0; i < args.length; i++) {
                                if (i != 0) {
                                    constructedReason.append(args[i]).append(" ");
                                }
                            }

                            long executorUserId = executor.getIdLong();
                            long bannedUserId = banEvent.getUser().getIdLong();

                            EmbedBuilder embedBuilder = new EmbedBuilder();

                            embedBuilder.setColor(Color.cyan);

                            embedBuilder.addField("Banned user", banEvent.getUser().getName() + "#" + banEvent.getUser().getDiscriminator() + ", " + bannedUserId, false);
                            embedBuilder.addField("Reason", constructedReason.toString(), true);

                            if (!perm) {
                                embedBuilder.addField("Duration", duration.toString(), true);
                            } else {
                                embedBuilder.addField("Duration", "Permanent ban", true);
                            }

                            embedBuilder.addField("Banned by", executor.getName() + "#" + executor.getDiscriminator() + ", " + executorUserId, false);

                            embedBuilder.setFooter(new Timestamp(System.currentTimeMillis()) + ", QBanOS v" + QBansBot.getInstance().getVersion());


                            // final temp variables
                            final Timestamp sDuration = duration;
                            final boolean sPerm = perm;
                            final String sReason = constructedReason.toString();

                            messageChannel.sendMessage(embedBuilder.build())
                                    .queue(message2 -> {
                                        message2.addReaction("U+274C").queue();
                                        messageId = message2.getIdLong();

                                        Timer timer = new Timer();

                                        timer.schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                if (!sPerm) {
                                                    String sql = String.format("INSERT INTO `bans`(`guildId`, `userId`, `staff`, `timestamp`, `messageId`, `reason`) VALUES (\"%s\", \"%s\",\"%s\",\"%s\", \"%s\", \"%s\")",
                                                            banEvent.getGuild().getIdLong(),
                                                            bannedUserId,
                                                            executorUserId,
                                                            sDuration.getTime(),
                                                            messageId,
                                                            sReason);

                                                    new Thread(() -> {
                                                        if (!MySQLUtils.insert(sql)) {
                                                            messageChannel.sendMessage("ERROR! Something went wrong while adding ban to database! please contact the developer ASAP!").queue();
                                                        }
                                                    }).start();
                                                }
                                            }
                                        }, 70L);
                                    });
                        }
                    }
                });
            } else {
                // Tell server he is unauthorized
            }
        }
    }
}
