package com.pokergame.App;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private static final String[] SUITS = {"♠", "♥", "♦", "♣"};
    private static final String[] RANKS = {"2","3","4","5","6","7","8","9","10","J","Q","K","A"};

    private final List<Card> cards = new ArrayList<>();

    public Deck() {
        for (String s : SUITS) {
            for (String r : RANKS) {
                cards.add(new Card(r, s));
            }
        }
        Collections.shuffle(cards);
    }

    public Card draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("El mazo está vacío");
        }
        return cards.remove(cards.size() - 1);
    }

    public int remaining() {
        return cards.size();
    }
}
