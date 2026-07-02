package com.pokergame.App;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String URL      = "jdbc:mariadb://localhost:3306/poker_db";
    private static final String USER     = "root";
    private static final String PASSWORD = "1234567890";

    private boolean available = false;

    public Database() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                available = conn != null;
                if (available) ensureTables(conn);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MariaDB no encontrado: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("No se pudo conectar a MariaDB: " + e.getMessage());
        }
    }

    private void ensureTables(Connection conn) throws SQLException {
        String rounds = "CREATE TABLE IF NOT EXISTS poker_rounds ("
                + "id              INT AUTO_INCREMENT PRIMARY KEY,"
                + "round_number    INT,"
                + "player_cards    VARCHAR(50),"
                + "community_cards VARCHAR(80),"
                + "player_hand_name VARCHAR(30),"
                + "cpu_hand_name   VARCHAR(30),"
                + "resultado       VARCHAR(10),"
                + "delta           INT,"
                + "fichas          INT,"
                + "timestamp       VARCHAR(25)"
                + ")";
        String state = "CREATE TABLE IF NOT EXISTS poker_state ("
                + "id          INT AUTO_INCREMENT PRIMARY KEY,"
                + "player_chips INT,"
                + "cpu_chips    INT,"
                + "wins         INT,"
                + "losses       INT,"
                + "ties         INT,"
                + "streak       INT,"
                + "best_streak  INT,"
                + "profit       INT"
                + ")";
        try (Statement st = conn.createStatement()) {
            st.execute(rounds);
            st.execute(state);
        }
    }

    public boolean isAvailable() { return available; }


    public void saveRound(RoundRecord row) {
        if (!available) return;
        String sql = "INSERT INTO poker_rounds "
                + "(round_number, player_cards, community_cards, player_hand_name, "
                + "cpu_hand_name, resultado, delta, fichas, timestamp) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt   (1, row.round);
            ps.setString(2, row.playerCards);
            ps.setString(3, row.community);
            ps.setString(4, row.playerHandName);
            ps.setString(5, row.cpuHandName);
            ps.setString(6, row.resultado);
            ps.setInt   (7, row.delta);
            ps.setInt   (8, row.fichas);
            ps.setString(9, row.timestamp);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saveRound: " + e.getMessage());
        }
    }
//GUARDA

    public void saveState(GameState g) {
        if (!available) return;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM poker_state")) {
                del.executeUpdate();
            }
            String sql = "INSERT INTO poker_state "
                    + "(player_chips, cpu_chips, wins, losses, ties, streak, best_streak, profit) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, g.playerChips);
                ps.setInt(2, g.cpuChips);
                ps.setInt(3, g.wins);
                ps.setInt(4, g.losses);
                ps.setInt(5, g.ties);
                ps.setInt(6, g.streak);
                ps.setInt(7, g.bestStreak);
                ps.setInt(8, g.profit);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error saveState: " + e.getMessage());
        }
    }
//CARGA

    public void loadStateInto(GameState g) {
        if (!available) return;
        String sql = "SELECT player_chips, cpu_chips, wins, losses, ties, "
                + "streak, best_streak, profit FROM poker_state LIMIT 1";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                g.playerChips = rs.getInt("player_chips");
                g.cpuChips    = rs.getInt("cpu_chips");
                g.wins        = rs.getInt("wins");
                g.losses      = rs.getInt("losses");
                g.ties        = rs.getInt("ties");
                g.streak      = rs.getInt("streak");
                g.bestStreak  = rs.getInt("best_streak");
                g.profit      = rs.getInt("profit");
            }
        } catch (SQLException e) {
            System.err.println("Error loadStateInto: " + e.getMessage());
        }
    }
//HISTORIAL

    public List<RoundRecord> getRounds() {
        List<RoundRecord> list = new ArrayList<>();
        if (!available) return list;
        String sql = "SELECT round_number, player_cards, community_cards, "
                + "player_hand_name, cpu_hand_name, resultado, delta, fichas, timestamp "
                + "FROM poker_rounds ORDER BY id ASC";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new RoundRecord(
                        rs.getInt   ("round_number"),
                        rs.getString("player_cards"),
                        rs.getString("community_cards"),
                        rs.getString("player_hand_name"),
                        rs.getString("cpu_hand_name"),
                        rs.getString("resultado"),
                        rs.getInt   ("delta"),
                        rs.getInt   ("fichas"),
                        rs.getString("timestamp")));
            }
        } catch (SQLException e) {
            System.err.println("Error getRounds: " + e.getMessage());
        }
        return list;
    }


    public void clearRounds() {
        if (!available) return;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement("DELETE FROM poker_rounds")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error clearRounds: " + e.getMessage());
        }
    }

    public void exportCSV(Path dest) throws IOException {
        List<RoundRecord> rows = getRounds();
        List<String> lines = new ArrayList<>();
        lines.add("Ronda,Cartas Jugador,Cartas Comunitarias,Mano Jugador,"
                + "Mano CPU,Resultado,Delta,Fichas,Timestamp");
        for (RoundRecord r : rows) lines.add(r.toCsvLine());
        Files.write(dest, lines, StandardCharsets.UTF_8);
    }
}
