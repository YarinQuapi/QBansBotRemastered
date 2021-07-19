package me.yarinlevi.qbansbotremastered.history;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.*;

public class HistoryHandler {
    public List<Message> getLatestMessagesByUser(MessageChannel messageChannel, Member member, int messages) {

        List<Message> latestMessages = messageChannel.getHistory().getRetrievedHistory().stream().filter(x -> x.getMember().getIdLong() == member.getIdLong()).toList().stream().toList();


        List<Message> messageList = new ArrayList<>();

        for (int i = 0; i < messages; i++) {
            messageChannel.getHistory().getRetrievedHistory().stream().filter(x -> x.getMember().getIdLong() == member.getIdLong());
        }

        return messageList;
    }
}
