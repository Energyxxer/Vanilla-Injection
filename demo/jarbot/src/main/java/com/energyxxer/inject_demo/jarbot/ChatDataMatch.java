package com.energyxxer.inject_demo.jarbot;

/**
 * Created by User on 4/12/2017.
 */
public class ChatDataMatch {
    Sentence sentence;
    ChatDataEntry entry;
    double percent = 0;

    public ChatDataMatch(Sentence sentence, ChatDataEntry entry) {
        this.sentence = sentence;
        this.entry = entry;

        Sentence sample = entry.question;
        this.percent = ((sample.compare(sentence) + sentence.compare(sample)) / 2) * (0.5 + entry.accuracyIndex/2);
    }

    @Override
    public String toString() {
        return entry.answer + " (" + percent + ")";
    }
}
