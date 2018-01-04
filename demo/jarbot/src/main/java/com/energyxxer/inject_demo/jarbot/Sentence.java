package com.energyxxer.inject_demo.jarbot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by User on 4/12/2017.
 */
public class Sentence {
    private static final List<String> INTERROGATIONS = Arrays.asList("how", "why", "when", "what", "where", "can", "are", "is", "were", "would", "could" );
    private static final List<String> PRONOUNS = Arrays.asList("you", "he", "she", "we", "they" ); //Pronoun "i" is omitted
    private static final List<String> NEGATABLE_WORDS = Arrays.asList("do", "does", "did", "is", "are", "was", "were", "should", "could", "would", "have", "has", "had", "will", "can");
    private static final String WHITELISTED_SYMBOLS = "'";
    private static final HashMap<String, String> IRREGULAR_NEGATION_SHORTENED = new HashMap<>();
    private static final double WORD_DIFFERENCE_PENALTY = 0.075;
    static {
        IRREGULAR_NEGATION_SHORTENED.put("will", "won't");
        IRREGULAR_NEGATION_SHORTENED.put("can", "can't");
    }

    private ArrayList<String> words = new ArrayList<>();

    public Sentence(String raw) {
        raw = raw.trim().toLowerCase().replace("'m"," am").replace("'re"," are").replace("'s"," is").replace("'ve"," have");

        StringBuilder sb = new StringBuilder();

        char lastChar = '\000';
        for(char ch : raw.toCharArray()) {
            boolean valid = false;
            if(ch == '.') {
                valid = Character.isDigit(lastChar);
            }
            if(Character.isAlphabetic(ch)) {
                if(lastChar != '\000' && !Character.isAlphabetic(lastChar) && !Character.isWhitespace(lastChar) && !WHITELISTED_SYMBOLS.contains("" + lastChar)) {
                    sb.append(" ");
                }
                valid = true;
            } else if(Character.isDigit(ch)) {
                if(lastChar != '\000' && !Character.isDigit(lastChar) && !Character.isWhitespace(lastChar)) {
                    sb.append(" ");
                }
                valid = true;
            } else if(Character.isWhitespace(ch)) {
                valid = !Character.isWhitespace(lastChar);
            } else if(WHITELISTED_SYMBOLS.contains("" + ch)) {
                valid = true;
            }
            if(valid) sb.append(ch);
            lastChar = ch;
        }
        String[] words = sb.toString().split(" ");
        this.words.addAll(Arrays.asList(words));
    }

    public Sentence(String[] words) {
        this(Arrays.asList(words));
    }

    public Sentence(Collection<String> words) {
        this.words.addAll(words);
    }

    String normalize() {
        StringBuilder sb = new StringBuilder();
        boolean isQuestion = false;

        String lastWord = null;
        for(int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            if (sb.length() <= 0) {
                if (INTERROGATIONS.contains(word)) isQuestion = true;
                sb.append(Character.toUpperCase(word.charAt(0)));
                sb.append(word.substring(1));
                word = sb.toString();
            } else if (word.equals("is") && sb.length() > 0 && PRONOUNS.contains(lastWord) && i+1 < words.size()) {
                sb.setLength(sb.length() - 1);
                sb.append("'s");
                word = lastWord + "'s";
            } else if (word.equals("are") && sb.length() > 0 && PRONOUNS.contains(lastWord) && i+1 < words.size()) {
                sb.setLength(sb.length() - 1);
                sb.append("'re");
                word = lastWord + "'re";
            } else if (word.equals("am") && sb.length() > 0 && "i".equals(lastWord) && i+1 < words.size()) {
                sb.setLength(sb.length() - 1);
                sb.append("'m");
                word = lastWord + "'m";
            } else if (word.equals("not") && sb.length() > 0 && lastWord != null && NEGATABLE_WORDS.contains(lastWord)) {
                sb.setLength(sb.length() - 1);
                if (IRREGULAR_NEGATION_SHORTENED.keySet().contains(lastWord)) {
                    sb.setLength(sb.length() - (lastWord.length()));
                    sb.append(IRREGULAR_NEGATION_SHORTENED.get(lastWord));
                    word = IRREGULAR_NEGATION_SHORTENED.get(lastWord);
                } else {
                    sb.append("n't");
                    word = lastWord + "n't";
                }
            } else if(word.equals("i")) {
                sb.append("I");
                word = "I";
            } else {
                sb.append(word);
            }
            sb.append(' ');
            lastWord = word;
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        if (isQuestion) sb.append('?');
        return sb.toString();
    }

    public ArrayList<String> getWords() {
        return words;
    }

    public double compare(Sentence s) {
        double topPercentage = 0;
        for(int i = 0; i < this.words.size(); i++) {
            topPercentage = Math.max(topPercentage, this.compare(s, i));
        }
        return topPercentage * (1 - (Math.abs(this.getLength() - s.getLength()) * WORD_DIFFERENCE_PENALTY));
    }

    public double compare(Sentence s, int index) {
        int totalMatches = 0;
        int topMatches = 0;
        int matches = 0;

        int is = 0;
        for(int i = index; i < words.size() && is < s.words.size(); i++,is++) {
            if(this.words.get(i).equals(s.words.get(is))) {
                totalMatches++;
                matches++;
            } else {
                topMatches = Math.max(topMatches, matches);
                matches = 0;
            }
        }
        topMatches = Math.max(topMatches, matches);

        return (totalMatches + (2 * topMatches)) / (this.words.size() * 3d);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sentence sentence = (Sentence) o;

        return words != null ? words.equals(sentence.words) : sentence.words == null;
    }

    @Override
    public int hashCode() {
        return words != null ? words.hashCode() : 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(String word : words) {
            sb.append(word);
            sb.append(' ');
        }
        if(!words.isEmpty()) sb.setLength(sb.length()-1);
        return sb.toString();
    }

    public int getLength() {
        return words.size();
    }
}
