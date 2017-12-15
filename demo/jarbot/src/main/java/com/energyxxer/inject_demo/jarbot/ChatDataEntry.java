package com.energyxxer.inject_demo.jarbot;

/**
 * Created by User on 4/12/2017.
 */
public class ChatDataEntry {
    Sentence question;
    Sentence answer;
    double accuracyIndex = 1;

    public ChatDataEntry(Sentence question, Sentence answer) {
        this.question = question;
        this.answer = answer;
    }

    public ChatDataEntry(Sentence question, Sentence answer, double accuracyIndex) {
        this.question = question;
        this.answer = answer;
        this.accuracyIndex = accuracyIndex;
    }

    @Override
    public String toString() {
        return "q: " + question + "; a: " + answer + "; %: " + accuracyIndex;
    }
}
