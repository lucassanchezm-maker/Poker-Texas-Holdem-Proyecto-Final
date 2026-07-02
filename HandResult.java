package com.pokergame.App;

import java.util.List;

public class HandResult {
    public final int rank;
    public final String name;
    public final List<Integer> tiebreakers;

    public HandResult(int rank, String name, List<Integer> tiebreakers) {
        this.rank        = rank;
        this.name        = name;
        this.tiebreakers = tiebreakers;
    }

    public int compareTo(HandResult other) {
        if (this.rank != other.rank) return this.rank - other.rank;
        int n = Math.min(this.tiebreakers.size(), other.tiebreakers.size());
        for (int i = 0; i < n; i++) {
            int diff = this.tiebreakers.get(i) - other.tiebreakers.get(i);
            if (diff != 0) return diff;
        }
        return 0;
    }
}
