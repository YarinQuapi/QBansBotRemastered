package me.yarinlevi.qbansbotremastered.listeners;

import me.yarinlevi.qbansbotremastered.QBansBot;
import me.yarinlevi.qbansbotremastered.mysql.MySQL;
import me.yarinlevi.qbansbotremastered.utilities.StringUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Iterator;

public class OnGuildBanEvent extends ListenerAdapter {

    @Override
    public void onGuildBan(@NotNull GuildBanEvent banEvent) {
        if (!banEvent.getUser().isBot()) {
            // Getting latest entry
            banEvent.getGuild().retrieveAuditLogs().limit(1).type(ActionType.BAN).queue(auditLogEntries -> {
                AuditLogEntry entry = auditLogEntries.get(0); // Selecting the last entry
                boolean validEntry = entry.getTargetId().equals(banEvent.getUser().getId()); // Is the  entry real?
                String reason = validEntry ? entry.getReason() : "permanent ban"; // The reason for the ban
                User executor = validEntry ? entry.getUser() : QBansBot.getInstance().getJda().getSelfUser(); // Who banned the user or if not found, the bot
                String logId = validEntry ? entry.getId() : "BAN_EXCEPTION_INVALID_ID"; // Entry ID

                if (reason != null) {
                    // Correctly splitting arguments
                    String[] args = StringUtils.split(reason);

                    if (args.length > 0) {
                        // Parsing ban duration
                        Timestamp duration = new Timestamp(StringUtils.parseDuration(args[0]));

                        MessageChannel messageChannel;
                        if (banEvent.getGuild().getTextChannels().stream().anyMatch(x-> x.getName().equals("qbansbot_logs"))) {
                            messageChannel = banEvent.getGuild().getTextChannels().stream().filter(x -> x.getName().equals("qbansbot_logs")).findAny().get();
                        } else {
                            banEvent.getGuild().createTextChannel("qbansbot_logs")
                                    .addPermissionOverride(banEvent.getGuild().getPublicRole(), 0, Permission.ALL_GUILD_PERMISSIONS)
                                    .queue();
                            messageChannel = banEvent.getGuild().getTextChannels().stream().filter(x -> x.getName().equals("qbansbot_logs")).findFirst().get();
                        }

                        // Reason construction
                        String[] rReason = new String[args.length - 1];

                        for (int i = 0; i < args.length; i++) {
                            if (i != 0) {
                                rReason[i-1] = args[i];
                            }
                        }
                        StringBuilder constructedReason = new StringBuilder();


                        Iterator<String> stringIterator = Arrays.stream(rReason).iterator();

                        while (stringIterator.hasNext()) {
                            String str = stringIterator.next();
                            constructedReason.append(str);

                            if (stringIterator.hasNext()) {
                                constructedReason.append(" ");
                            }
                        }

                        messageChannel.sendMessage(String.format("%s#%s banned %s#%s for %s",
                                executor.getName(),
                                executor.getDiscriminator(),
                                banEvent.getUser().getName(),
                                banEvent.getUser().getDiscriminator(),
                                constructedReason))
                                .queue();

                        long executorUserId = executor.getIdLong();
                        long bannedUserId = banEvent.getUser().getIdLong();

                        // SQL handling
                        //Date date = new Date(timestamp.getTime());
                        String sql = String.format("INSERT INTO `bans`(`guildId`, `userId`, `staff`, `timestamp`) VALUES (\"%s\", \"%s\",\"%s\",\"%s\")",
                                banEvent.getGuild().getIdLong(),
                                bannedUserId,
                                executorUserId,
                                duration.getTime());

                        new Thread(() -> {
                            if (!MySQL.insert(sql)) {
                                messageChannel.sendMessage("ERROR! Something went wrong while adding ban to database! please contact the developer ASAP!").queue();
                            }
                        }).start();
                    }
                }
            });
        }
    }
}
