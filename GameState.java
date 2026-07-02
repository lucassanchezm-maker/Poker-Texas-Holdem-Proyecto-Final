package com.pokergame.App;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    public Deck deck;
    public List<Card> playerHand = new ArrayList<>();
    public List<Card> cpuHand    = new ArrayList<>();
    public List<Card> community  = new ArrayList<>();

    public int playerChips = 1000;
    public int cpuChips    = 1000;
    public int pot         = 0;
    public int playerBet   = 0;
    public int cpuBet      = 0;
    public int currentBet  = 0;
    public final int blind = 20;

    public Phase phase = Phase.IDLE;
    public int round   = 0;

    public int wins       = 0;
    public int losses     = 0;
    public int ties       = 0;
    public int streak     = 0;
    public int bestStreak = 0;
    public int profit     = 0;

    public void resetToDefaults() {
        playerChips = 1000;
        cpuChips    = 1000;
        wins = 0; losses = 0; ties = 0;
        streak = 0; bestStreak = 0; profit = 0;
        round = 0;
        phase = Phase.IDLE;
        pot = 0; playerBet = 0; cpuBet = 0; currentBet = 0;
        playerHand.clear(); cpuHand.clear(); community.clear();
    }
}
