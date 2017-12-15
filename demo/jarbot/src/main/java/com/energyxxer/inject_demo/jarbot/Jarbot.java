package com.energyxxer.inject_demo.jarbot;

/**
 * Created by User on 4/12/2017.
 */
public class Jarbot {
    private static String lastAnswer = null;

    public static String ask(String message) {
        Sentence s = new Sentence(message);
        if(lastAnswer != null) AnswerBank.insert(new Sentence(lastAnswer), s, 0.2);
        return lastAnswer = AnswerBank.fetch(s);
    }
}
