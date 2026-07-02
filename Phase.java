package com.pokergame.App;

public enum Phase {
    IDLE, PREFLOP, FLOP, TURN, RIVER, SHOWDOWN;

    public String label() {
        switch (this) {
            case PREFLOP:  return "Pre-Flop";
            case FLOP:     return "Flop";
            case TURN:     return "Turn";
            case RIVER:    return "River";
            case SHOWDOWN: return "Showdown";
            default:       return "Idle";
        }
    }
}
