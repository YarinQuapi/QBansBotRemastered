package me.yarinlevi.qbansbotremastered.history;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HistoryHandler {
    public List<Message> getLatestMessagesByUser(MessageChannel messageChannel, Member member) {
        List<Message> messageList = new ArrayList<>();

        messageChannel.getHistory().getRetrievedHistory().stream().filter(x -> x.getMember().getIdLong() == member.getIdLong() && x.getTimeCreated().isAfter(OffsetDateTime.now().minusHours(1)))
                .forEach(messageList::add);

        return messageList.stream().sorted(Comparator.comparing(Message::getTimeCreated)).toList();
    }
}
