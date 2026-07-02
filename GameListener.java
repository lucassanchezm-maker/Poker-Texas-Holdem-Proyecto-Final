package com.pokergame.App;

public interface GameListener {
    void onLog(String text);
    void onBanner(String text);
    void onStateChanged();
    void onAwaitingPlayerAction();
    void onRoundEnded();
}
