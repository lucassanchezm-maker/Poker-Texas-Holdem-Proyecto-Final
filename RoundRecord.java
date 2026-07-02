package com.pokergame.App;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RoundRecord {
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public int    round;
    public String playerCards;
    public String community;
    public String playerHandName;
    public String cpuHandName;
    public String resultado;
    public int    delta;
    public int    fichas;
    public String timestamp;

    public RoundRecord(int round, String playerCards, String community,
                       String playerHandName, String cpuHandName,
                       String resultado, int delta, int fichas) {
        this.round         = round;
        this.playerCards   = playerCards;
        this.community     = community;
        this.playerHandName = playerHandName;
        this.cpuHandName   = cpuHandName;
        this.resultado     = resultado;
        this.delta         = delta;
        this.fichas        = fichas;
        this.timestamp     = LocalDateTime.now().format(FMT);
    }

    public RoundRecord(int round, String playerCards, String community,
                       String playerHandName, String cpuHandName,
                       String resultado, int delta, int fichas, String timestamp) {
        this(round, playerCards, community, playerHandName, cpuHandName,
             resultado, delta, fichas);
        this.timestamp = timestamp;
    }

    public String toCsvLine() {
        return round + ","
                + csvEscape(playerCards)    + ","
                + csvEscape(community)      + ","
                + csvEscape(playerHandName) + ","
                + csvEscape(cpuHandName)    + ","
                + resultado + ","
                + delta     + ","
                + fichas    + ","
                + csvEscape(timestamp);
    }

    private static String csvEscape(String s) {
        return "\"" + (s == null ? "" : s.replace("\"", "'")) + "\"";
    }

    public static RoundRecord fromCsvLine(String line) {
        List<String> f = splitCsv(line);
        return new RoundRecord(
                Integer.parseInt(f.get(0)),
                f.get(1), f.get(2), f.get(3), f.get(4),
                f.get(5), Integer.parseInt(f.get(6)),
                Integer.parseInt(f.get(7)), f.get(8));
    }

    private static List<String> splitCsv(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                out.add(cur.toString()); cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString());
        return out;
    }
}
