package com.pokergame.App;

public class Card {
    public final String rank;
    public final String suit;

    public Card(String rank, String suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public int value() {
        switch (rank) {
            case "J": return 11;
            case "Q": return 12;
            case "K": return 13;
            case "A": return 14;
            default:  return Integer.parseInt(rank);
        }
    }

    public int getValue() { return value(); }

    public String getSuit() { return suit; }

    public boolean isRed() {
        return suit.equals("♥") || suit.equals("♦");
    }

    @Override
    public String toString() {
        return rank + suit;
    }
}
