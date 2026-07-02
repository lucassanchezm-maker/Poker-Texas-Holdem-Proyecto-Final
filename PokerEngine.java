package com.pokergame.App;

import javax.swing.Timer;
import java.util.ArrayList;
import java.util.List;

public class PokerEngine {
    public final GameState g   = new GameState();
    private final Database db  = new Database();
    private final CpuPlayer cpu = new CpuPlayer();
    private GameListener listener;

    public void setListener(GameListener listener) { this.listener = listener; }
    public Database getDatabase() { return db; }

    public void start() {
        db.loadStateInto(g);
        listener.onStateChanged();
        listener.onLog(db.isAvailable()
                ? "Datos cargados. Bienvenido de vuelta."
                : "Sin conexión a base de datos. El progreso no se guardará.");
        listener.onRoundEnded();
    }

    public void newRound() {
        if (g.playerChips <= 0 || g.cpuChips <= 0) {
            listener.onLog(g.playerChips <= 0
                    ? "Te quedaste sin fichas. Reinicia el juego."
                    : "La CPU se quedó sin fichas. ¡Ganaste el juego!");
            listener.onRoundEnded();
            return;
        }

        g.round++;
        g.deck = new Deck();
        g.playerHand.clear();
        g.cpuHand.clear();
        g.community.clear();
        g.playerHand.add(g.deck.draw());
        g.playerHand.add(g.deck.draw());
        g.cpuHand.add(g.deck.draw());
        g.cpuHand.add(g.deck.draw());

        g.pot = 0; g.playerBet = 0; g.cpuBet = 0; g.currentBet = 0;
        g.phase = Phase.PREFLOP;

        int sb = Math.min(g.blind / 2, g.playerChips);
        int bb = Math.min(g.blind,     g.cpuChips);
        g.playerChips -= sb; g.cpuChips -= bb;
        g.playerBet = sb; g.cpuBet = bb;
        g.pot = sb + bb;
        g.currentBet = bb;

        listener.onStateChanged();
        listener.onLog(String.format(
                "Ronda %d  |  Small blind: %d  |  Big blind: %d  |  Para igualar: %d fichas.",
                g.round, sb, bb, Math.max(0, g.currentBet - g.playerBet)));
        listener.onAwaitingPlayerAction();
    }

    public int callAmount() {
        return Math.min(g.currentBet - g.playerBet, g.playerChips);
    }

    public boolean canCheck() {
        return g.playerBet >= g.currentBet;
    }

    public int minRaise() {
        return Math.max(g.currentBet - g.playerBet + g.blind, g.blind);
    }

    public void playerAct(ActionType action) {
        switch (action) {
            case FOLD:
                listener.onLog("Tiraste las cartas. La CPU toma el bote.");
                awardPot("cpu", "Fold", "", "");
                break;
            case CHECK:
                if (!canCheck()) {
                    listener.onLog("No puedes hacer check, hay una apuesta activa.");
                    listener.onAwaitingPlayerAction();
                    break;
                }
                listener.onLog("Check.");
                cpuTurn();
                break;
            case CALL: {
                int amt = callAmount();
                if (amt <= 0) { cpuTurn(); break; }
                g.playerChips -= amt; g.playerBet += amt; g.pot += amt;
                listener.onLog(String.format("Call de %d fichas. Bote: %d", amt, g.pot));
                listener.onStateChanged();
                cpuTurn();
                break;
            }
            case ALLIN: {
                int amt = g.playerChips;
                g.pot += amt; g.playerBet += amt; g.playerChips = 0;
                if (g.playerBet > g.currentBet) g.currentBet = g.playerBet;
                listener.onLog(String.format("¡ALL-IN! %d fichas. Bote: %d", amt, g.pot));
                listener.onStateChanged();
                cpuTurn();
                break;
            }
            default:
                break;
        }
    }

    public void playerRaise(int amt) {
        int minR = minRaise();
        if (amt < minR) amt = minR;
        if (amt > g.playerChips) amt = g.playerChips;
        if (amt <= 0) { listener.onLog("Sin fichas para subir."); return; }

        g.playerChips -= amt; g.playerBet += amt; g.pot += amt;
        g.currentBet = g.playerBet;
        listener.onLog(String.format("Raise a %d fichas. Bote: %d", g.playerBet, g.pot));
        listener.onStateChanged();
        cpuTurn();
    }

    private void cpuTurn() {
        Timer t = new Timer(900, e -> resolveCpuTurn());
        t.setRepeats(false);
        t.start();
    }

    private void resolveCpuTurn() {
        int callAmt = Math.max(0, g.currentBet - g.cpuBet);
        CpuDecision d = cpu.decide(g);

        switch (d.type) {
            case FOLD:
                listener.onLog("La CPU tira sus cartas. ¡Ganaste el bote!");
                awardPot("player", "CPU fold", "", "");
                break;
            case CALL: {
                int pay = Math.min(callAmt, g.cpuChips);
                g.cpuChips -= pay; g.cpuBet += pay; g.pot += pay;
                listener.onLog(String.format("CPU hace call (%d). Bote: %d", pay, g.pot));
                listener.onStateChanged();
                advancePhase();
                break;
            }
            case CHECK:
                listener.onLog("CPU hace check.");
                listener.onStateChanged();
                advancePhase();
                break;
            default: { // RAISE
                int pay = Math.min(d.amount, g.cpuChips);
                if (pay <= callAmt) {
                    pay = Math.min(callAmt, g.cpuChips);
                    g.cpuChips -= pay; g.cpuBet += pay; g.pot += pay;
                    listener.onLog(String.format("CPU hace call (%d). Bote: %d", pay, g.pot));
                    listener.onStateChanged();
                    advancePhase();
                } else {
                    g.cpuChips -= pay; g.cpuBet += pay; g.pot += pay;
                    g.currentBet = g.cpuBet;
                    listener.onLog(String.format("CPU sube a %d. Bote: %d", g.cpuBet, g.pot));
                    listener.onStateChanged();
                    listener.onAwaitingPlayerAction();
                }
                break;
            }
        }
    }

    private void advancePhase() {
        switch (g.phase) {
            case PREFLOP: g.phase = Phase.FLOP;      break;
            case FLOP:    g.phase = Phase.TURN;      break;
            case TURN:    g.phase = Phase.RIVER;     break;
            case RIVER:   g.phase = Phase.SHOWDOWN;  break;
            default: break;
        }
        if (g.phase != Phase.SHOWDOWN) {
            g.playerBet = 0; g.cpuBet = 0; g.currentBet = 0;
        }
        switch (g.phase) {
            case FLOP:
                g.community.add(g.deck.draw());
                g.community.add(g.deck.draw());
                g.community.add(g.deck.draw());
                listener.onLog("Flop revelado. ¿Cuál es tu movimiento?");
                listener.onStateChanged();
                listener.onAwaitingPlayerAction();
                break;
            case TURN:
                g.community.add(g.deck.draw());
                listener.onLog("Turn en la mesa. Tu turno.");
                listener.onStateChanged();
                listener.onAwaitingPlayerAction();
                break;
            case RIVER:
                g.community.add(g.deck.draw());
                listener.onLog("River. Última carta. ¡Decide bien!");
                listener.onStateChanged();
                listener.onAwaitingPlayerAction();
                break;
            case SHOWDOWN:
                showdown();
                break;
            default: break;
        }
    }

    private void showdown() {
        listener.onStateChanged();
        List<Card> pCards = concat(g.playerHand, g.community);
        List<Card> cCards = concat(g.cpuHand,    g.community);
        HandResult pEv = HandEvaluator.evalHand(pCards);
        HandResult cEv = HandEvaluator.evalHand(cCards);
        int cmp = pEv.compareTo(cEv);

        String winner;
        if (cmp > 0) {
            winner = "player";
            listener.onLog(String.format(
                    "Showdown: tu %s gana contra el %s de la CPU. ¡Ganaste!", pEv.name, cEv.name));
        } else if (cmp < 0) {
            winner = "cpu";
            listener.onLog(String.format(
                    "Showdown: CPU gana con %s vs tu %s.", cEv.name, pEv.name));
        } else {
            winner = "tie";
            listener.onLog(String.format(
                    "Empate: ambos tienen %s. El bote se divide.", pEv.name));
        }
        awardPot(winner, "Showdown", pEv.name, cEv.name);
    }

    private static List<Card> concat(List<Card> a, List<Card> b) {
        List<Card> out = new ArrayList<>(a);
        out.addAll(b);
        return out;
    }

    private void awardPot(String winner, String reason, String pHand, String cHand) {
        int pot = g.pot;
        int delta = 0;

        if (winner.equals("player")) {
            delta = pot; g.playerChips += pot; g.wins++;
            g.streak = g.streak > 0 ? g.streak + 1 : 1;
            listener.onBanner("¡Ganaste! +" + pot + " fichas 🏆");
        } else if (winner.equals("cpu")) {
            delta = -pot; g.cpuChips += pot; g.losses++;
            g.streak = g.streak < 0 ? g.streak - 1 : -1;
            listener.onBanner("CPU gana este bote");
        } else {
            int half = pot / 2;
            g.playerChips += half; g.cpuChips += (pot - half);
            delta = 0; g.ties++; g.streak = 0;
            listener.onBanner("Empate — bote dividido");
        }

        if (Math.abs(g.streak) > g.bestStreak) g.bestStreak = Math.abs(g.streak);
        g.profit += delta;
        g.pot = 0;

        StringBuilder pc = new StringBuilder();
        for (Card c : g.playerHand) pc.append(c).append(' ');
        StringBuilder cc = new StringBuilder();
        for (Card c : g.community)  cc.append(c).append(' ');

        RoundRecord row = new RoundRecord(
                g.round,
                pc.toString().trim(),
                cc.toString().trim(),
                pHand.isEmpty() ? "Fold" : pHand,
                cHand.isEmpty() ? (winner.equals("player") ? "CPU Fold" : "—") : cHand,
                winner.equals("player") ? "Ganó" : (winner.equals("cpu") ? "Perdió" : "Empate"),
                delta,
                g.playerChips);
        db.saveRound(row);
        db.saveState(g);

        listener.onStateChanged();
        listener.onRoundEnded();

        if (g.playerChips <= 0)
            listener.onLog("¡Te quedaste sin fichas! Juego terminado. Presiona Reiniciar.");
        else if (g.cpuChips <= 0)
            listener.onLog("¡La CPU se quedó sin fichas! ¡Ganaste el juego completo! 🎉");
    }

    public void resetGame() {
        g.resetToDefaults();
        db.saveState(g);
        listener.onStateChanged();
        listener.onLog("Juego reiniciado. Ambos jugadores vuelven a 1000 fichas.");
        listener.onRoundEnded();
    }
}
