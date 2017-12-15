package com.energyxxer.inject_demo.jarbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by User on 4/12/2017.
 */
public class AnswerBank {
    private static final ArrayList<ChatDataEntry> chatData = new ArrayList<>();
    private static final Random random = new Random();

    static {
        try(BufferedReader br = new BufferedReader(new InputStreamReader(AnswerBank.class.getResourceAsStream("/resources/jarbot/chatData.txt")))) {
            String line;
            while((line = br.readLine()) != null) {
                if(line.length() > 0 && !line.startsWith("#")) {
                    String[] pair = line.split("=",2);
                    chatData.add(new ChatDataEntry(new Sentence(pair[0].trim()),new Sentence(pair[1].trim())));
                }
            }
        } catch(NullPointerException | IOException x) {
            x.printStackTrace();
        }
    }

    public static String fetch(Sentence s) {
        ArrayList<ChatDataMatch> matches = new ArrayList<>();

        for(ChatDataEntry entry : chatData) {
            ChatDataMatch match = new ChatDataMatch(s, entry);

            boolean added = false;

            for(int i = 0; i < matches.size(); i++) {
                if(matches.get(i).percent < match.percent) {
                    matches.add(i, match);
                    added = true;
                    break;
                }
            }
            if(!added) matches.add(match);
        }

        ArrayList<ChatDataMatch> topMatches = new ArrayList<>();
        for(ChatDataMatch match : matches) {
            if(topMatches.isEmpty() || topMatches.get(0).percent <= match.percent) topMatches.add(match);
            else break;
        }

        ChatDataMatch topMatch = topMatches.get(random.nextInt(topMatches.size()));

        if(topMatch.percent < 1 && topMatch.percent >= 0.5) {
            chatData.add(new ChatDataEntry(s, topMatch.entry.answer, topMatch.percent));
        } else if(topMatch.percent == 0) {
            return chatData.get(random.nextInt(chatData.size())).answer.normalize();
        }

        topMatch.entry.accuracyIndex = Math.min(topMatch.entry.accuracyIndex + 0.15,1);

        return topMatch.entry.answer.normalize();
    }

    public static List<ChatDataEntry> getEntries(Sentence s) {
        ArrayList<ChatDataEntry> entries = new ArrayList<>();
        for(ChatDataEntry e : chatData) {
            if(e.question.equals(s)) entries.add(e);
        }
        return entries;
    }

    public static ChatDataEntry getEntry(Sentence s) {
        for(ChatDataEntry e : chatData) {
            if(e.question.equals(s)) return e;
        }
        return null;
    }

    public static void insert(Sentence q, Sentence a, double accuracy) {
        chatData.add(new ChatDataEntry(q, a, accuracy));
    }

    public static void insert(Sentence q, Sentence a) {
        chatData.add(new ChatDataEntry(q, a));
    }

}
