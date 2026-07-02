package com.pokergame.App;

import java.util.*;

public class HandEvaluator {

    public static HandResult evalHand(List<Card> cards) {
        if (cards == null || cards.size() < 5) {
            throw new IllegalArgumentException(
                    "Se necesitan al menos 5 cartas para evaluar una mano.");
        }
        HandResult best = null;
        for (List<Card> combo : combinations(cards, 5)) {
            HandResult current = evalFive(combo);
            if (best == null || current.compareTo(best) > 0) {
                best = current;
            }
        }
        return best;
    }

    private static List<List<Card>> combinations(List<Card> arr, int k) {
        List<List<Card>> result = new ArrayList<>();
        combineHelper(arr, k, 0, new ArrayList<>(), result);
        return result;
    }

    private static void combineHelper(List<Card> arr, int k, int start,
                                      List<Card> current, List<List<Card>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i <= arr.size() - (k - current.size()); i++) {
            current.add(arr.get(i));
            combineHelper(arr, k, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    private static HandResult evalFive(List<Card> cards) {
        List<Integer> vals = new ArrayList<>();
        for (Card c : cards) vals.add(c.value());
        Collections.sort(vals, Collections.reverseOrder());

        Set<String> suits = new HashSet<>();
        for (Card c : cards) suits.add(c.suit);
        boolean isFlush = (suits.size() == 1);

        Map<Integer, Integer> count = new HashMap<>();
        for (int v : vals) count.merge(v, 1, Integer::sum);

        List<int[]> groups = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : count.entrySet())
            groups.add(new int[]{e.getKey(), e.getValue()});
        groups.sort((a, b) -> a[1] != b[1] ? b[1] - a[1] : b[0] - a[0]);

        boolean isStraight = false;
        int straightHigh = 0;
        List<Integer> uniqueVals = new ArrayList<>(new TreeSet<>(vals));
        Collections.reverse(uniqueVals);

        if (uniqueVals.size() == 5) {
            if (uniqueVals.get(0) - uniqueVals.get(4) == 4) {
                isStraight  = true;
                straightHigh = uniqueVals.get(0);
            }
            if (!isStraight
                    && uniqueVals.contains(14)
                    && uniqueVals.contains(5)
                    && uniqueVals.contains(4)
                    && uniqueVals.contains(3)
                    && uniqueVals.contains(2)) {
                isStraight  = true;
                straightHigh = 5;
            }
        }

        int[] g0 = groups.size() > 0 ? groups.get(0) : null;
        int[] g1 = groups.size() > 1 ? groups.get(1) : null;
        int[] g2 = groups.size() > 2 ? groups.get(2) : null;

        if (isFlush && isStraight && straightHigh == 14)
            return new HandResult(8, "Escalera Real",  Arrays.asList(14));
        if (isFlush && isStraight)
            return new HandResult(7, "Escalera de Color", Arrays.asList(straightHigh));
        if (g0 != null && g0[1] == 4)
            return new HandResult(6, "Poker",      Arrays.asList(g0[0], g1 != null ? g1[0] : 0));
        if (g0 != null && g0[1] == 3 && g1 != null && g1[1] == 2)
            return new HandResult(5, "Full House",  Arrays.asList(g0[0], g1[0]));
        if (isFlush)
            return new HandResult(4, "Color",       vals);
        if (isStraight)
            return new HandResult(3, "Escalera",    Arrays.asList(straightHigh));
        if (g0 != null && g0[1] == 3)
            return new HandResult(2, "Trio",        Arrays.asList(
                    g0[0], g1 != null ? g1[0] : 0, g2 != null ? g2[0] : 0));
        if (g0 != null && g0[1] == 2 && g1 != null && g1[1] == 2)
            return new HandResult(1, "Doble Par",   Arrays.asList(
                    g0[0], g1[0], g2 != null ? g2[0] : 0));
        if (g0 != null && g0[1] == 2) {
            List<Integer> tb = new ArrayList<>();
            tb.add(g0[0]);
            for (int[] g : groups) if (g[1] == 1) tb.add(g[0]);
            return new HandResult(0, "Par", tb);
        }
        return new HandResult(-1, "Carta Alta", vals);
    }
}
