package com.pokergame.App;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CpuPlayer {
    private final Random random = new Random();

    public CpuDecision decide(GameState g) {
        int callAmt = Math.max(0, g.currentBet - g.cpuBet);
        List<Card> all = new ArrayList<>(g.cpuHand);
        all.addAll(g.community);

        HandResult ev;
        if (all.size() >= 5) {
            ev = HandEvaluator.evalHand(all);
        } else {
            ev = heuristicPreflop(g.cpuHand);
        }

        double r = random.nextDouble();
        ActionType action;
        int amount = 0;

        if (ev.rank >= 2) {
            if (r < 0.55) {
                action = ActionType.RAISE;
                amount = Math.min((int)(callAmt + g.pot * 0.4), g.cpuChips);
            } else {
                action = ActionType.CALL;
            }
        } else if (ev.rank >= 0) {
            if (callAmt == 0) {
                action = r < 0.35 ? ActionType.RAISE : ActionType.CHECK;
                amount = Math.min(35, g.cpuChips);
            } else if (callAmt <= 120) {
                action = r < 0.65 ? ActionType.CALL : ActionType.FOLD;
            } else {
                action = r < 0.35 ? ActionType.CALL : ActionType.FOLD;
            }
        } else {
            if (callAmt == 0) {
                action = r < 0.15 ? ActionType.RAISE : ActionType.CHECK;
                amount = Math.min(20, g.cpuChips);
            } else if (callAmt <= 60) {
                action = r < 0.45 ? ActionType.CALL : ActionType.FOLD;
            } else {
                action = ActionType.FOLD;
            }
        }

        if (action == ActionType.RAISE) {
            amount = Math.max(amount, callAmt + 10);
            if (amount > g.cpuChips) {
                amount = g.cpuChips;
                if (amount == 0) action = ActionType.CHECK;
            }
        }
        return new CpuDecision(action, amount);
    }

    private HandResult heuristicPreflop(List<Card> hand) {
        if (hand.size() == 2 && hand.get(0).getValue() == hand.get(1).getValue()) {
            return new HandResult(0, "Par", Arrays.asList(hand.get(0).getValue()));
        }
        int high = 0;
        for (Card c : hand) high = Math.max(high, c.getValue());
        return new HandResult(-1, "Carta Alta", Arrays.asList(high));
    }
}
