package com.pokergame.App;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class PokerFrame extends JFrame implements GameListener {
    private final PokerEngine engine = new PokerEngine();

    private static final Color BG       = new Color(0x0a0a0a);
    private static final Color PANEL_BG = new Color(0x111111);
    private static final Color FELT     = new Color(0x1a5c2e);
    private static final Color GOLD     = new Color(0xd4af37);
    private static final Color CREAM    = new Color(0xe8dcc8);

    private JLabel dbLabel;
    private JLabel sRound, sWins, sLoss, sBest, sProfit;
    private JLabel cpuChipBadge, cpuBetBadge;
    private JLabel playerChipBadge, playerBetBadge;
    private JLabel potAmountLabel;
    private CardRow cpuCardsRow, communityCardsRow, playerCardsRow;
    private JLabel[] phaseSteps;

    private JLabel bannerLabel;
    private JPanel bannerOverlay;
    private Timer  bannerTimer;

    private JLabel logBox;
    private JPanel actionPanel;
    private JPanel raisePanel;
    private JSpinner raiseSpinner;

    private DefaultTableModel historyModel;
    private JTable historyTable;

    public PokerFrame() {
        super("Texas Hold'em — Poker contra CPU");
        engine.setListener(this);
        buildUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(700, 860));
        pack();
        setLocationRelativeTo(null);
        SwingUtilities.invokeLater(() -> engine.start());
    }

    // ─── Construcción de UI ───────────────────────────────────────────────────

    private void buildUI() {
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(PANEL_BG);
        root.setBorder(new EmptyBorder(12, 14, 12, 14));

        addToBox(root, buildHeader());
        root.add(Box.createVerticalStrut(8));
        addToBox(root, buildStatsBar());
        root.add(Box.createVerticalStrut(8));
        addToBox(root, buildTablePanel());
        root.add(Box.createVerticalStrut(8));
        addToBox(root, buildActionArea());
        root.add(Box.createVerticalStrut(8));
        addToBox(root, buildHistoryPanel());

        JScrollPane scroll = new JScrollPane(root);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private void addToBox(JPanel box, JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height + 20));
        box.add(c);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel title = new JLabel("Texas Hold'em");
        title.setFont(new Font("Serif", Font.BOLD, 22));
        title.setForeground(GOLD);
        dbLabel = new JLabel("conectando...");
        dbLabel.setForeground(Color.GRAY);
        dbLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        p.add(title,   BorderLayout.WEST);
        p.add(dbLabel, BorderLayout.EAST);
        return p;
    }

    private JPanel buildStatsBar() {
        JPanel p = new JPanel(new GridLayout(1, 5, 6, 0));
        p.setOpaque(false);
        sRound  = statValue("0");
        sWins   = statValue("0");
        sLoss   = statValue("0");
        sBest   = statValue("0");
        sProfit = statValue("+0");
        p.add(statBox("Ronda",       sRound));
        p.add(statBox("Ganadas",     sWins));
        p.add(statBox("Perdidas",    sLoss));
        p.add(statBox("Mejor Racha", sBest));
        p.add(statBox("Ganancia",    sProfit));
        return p;
    }

    private JLabel statValue(String v) {
        JLabel l = new JLabel(v, SwingConstants.CENTER);
        l.setFont(new Font("Serif", Font.BOLD, 16));
        l.setForeground(GOLD);
        return l;
    }

    private JPanel statBox(String label, JLabel val) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(new Color(0x0d0d0d));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x4a4020)),
                new EmptyBorder(5, 4, 5, 4)));
        val.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lbl.setForeground(new Color(180, 180, 180));
        box.add(val); box.add(lbl);
        return box;
    }

    private JPanel buildTablePanel() {
        cpuChipBadge      = chipBadge();
        cpuBetBadge       = betBadge();
        playerChipBadge   = chipBadge();
        playerBetBadge    = betBadge();
        cpuCardsRow       = new CardRow(2);
        communityCardsRow = new CardRow(5);
        playerCardsRow    = new CardRow(2);

        JPanel felt = new JPanel(new GridBagLayout());
        felt.setBackground(FELT);
        felt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x7a5215), 6),
                new EmptyBorder(14, 14, 14, 14)));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0;

        JPanel cpuRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        cpuRight.setOpaque(false);
        cpuRight.add(cpuChipBadge); cpuRight.add(cpuBetBadge);
        JPanel cpuSeat = new JPanel(new BorderLayout());
        cpuSeat.setOpaque(false);
        cpuSeat.add(seatName("CPU"), BorderLayout.WEST);
        cpuSeat.add(cpuRight,        BorderLayout.EAST);

        potAmountLabel = new JLabel("0", SwingConstants.CENTER);
        potAmountLabel.setFont(new Font("Serif", Font.ITALIC | Font.BOLD, 20));
        potAmountLabel.setForeground(GOLD);
        JLabel potLabel = new JLabel("BOTE", SwingConstants.CENTER);
        potLabel.setForeground(new Color(255, 255, 255, 160));
        potLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        JPanel potPanel = new JPanel(new GridLayout(2, 1));
        potPanel.setOpaque(false);
        potPanel.add(potLabel); potPanel.add(potAmountLabel);

        JPanel playerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        playerRight.setOpaque(false);
        playerRight.add(playerChipBadge); playerRight.add(playerBetBadge);
        JPanel playerSeat = new JPanel(new BorderLayout());
        playerSeat.setOpaque(false);
        playerSeat.add(seatName("TÚ"), BorderLayout.WEST);
        playerSeat.add(playerRight,     BorderLayout.EAST);

        c.gridy = 0; c.insets = new Insets(2, 0, 4, 0);  felt.add(cpuSeat,                    c);
        c.gridy = 1; c.insets = new Insets(2, 0, 6, 0);  felt.add(centered(cpuCardsRow),       c);
        c.gridy = 2; c.insets = new Insets(6, 0, 6, 0);  felt.add(centered(communityCardsRow), c);
        c.gridy = 3; c.insets = new Insets(2, 0, 2, 0);  felt.add(potPanel,                    c);
        c.gridy = 4; c.insets = new Insets(6, 0, 6, 0);  felt.add(centered(playerCardsRow),    c);
        c.gridy = 5; c.insets = new Insets(4, 0, 2, 0);  felt.add(playerSeat,                  c);
        c.gridy = 6; c.insets = new Insets(8, 0, 2, 0);  felt.add(buildPhaseStrip(),           c);

        bannerLabel = new JLabel("", SwingConstants.CENTER);
        bannerLabel.setFont(new Font("Serif", Font.BOLD, 24));
        bannerLabel.setForeground(GOLD);
        bannerLabel.setOpaque(true);
        bannerLabel.setBackground(new Color(0, 0, 0, 210));
        bannerLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD, 2),
                new EmptyBorder(14, 28, 14, 28)));

        bannerOverlay = new JPanel(new GridBagLayout());
        bannerOverlay.setOpaque(false);
        bannerOverlay.setVisible(false);
        bannerOverlay.add(bannerLabel);

        JLayeredPane layered = new JLayeredPane() {
            @Override public void doLayout() {
                for (Component comp : getComponents())
                    comp.setBounds(0, 0, getWidth(), getHeight());
            }
            @Override public Dimension getPreferredSize() {
                return felt.getPreferredSize();
            }
        };
        layered.add(felt,          JLayeredPane.DEFAULT_LAYER);
        layered.add(bannerOverlay, JLayeredPane.PALETTE_LAYER);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(layered, BorderLayout.CENTER);
        return wrap;
    }

    private JLabel seatName(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(GOLD);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        return l;
    }

    private JLabel chipBadge() {
        JLabel l = new JLabel("1000");
        l.setForeground(GOLD);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(212, 175, 55, 110)),
                new EmptyBorder(2, 10, 2, 10)));
        l.setOpaque(true);
        l.setBackground(new Color(0, 0, 0, 140));
        return l;
    }

    private JLabel betBadge() {
        JLabel l = new JLabel("");
        l.setForeground(new Color(0xffd060));
        l.setFont(new Font("SansSerif", Font.ITALIC, 11));
        l.setVisible(false);
        return l;
    }

    private JComponent centered(JComponent comp) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.setOpaque(false);
        p.add(comp);
        return p;
    }

    private JPanel buildPhaseStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 5, 4, 0));
        strip.setOpaque(false);
        String[] labels = {"Pre-Flop", "Flop", "Turn", "River", "Showdown"};
        phaseSteps = new JLabel[5];
        for (int i = 0; i < 5; i++) {
            JLabel l = new JLabel(labels[i], SwingConstants.CENTER);
            l.setFont(new Font("SansSerif", Font.PLAIN, 9));
            l.setForeground(new Color(255, 255, 255, 90));
            l.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 40)));
            phaseSteps[i] = l;
            strip.add(l);
        }
        return strip;
    }

    private JPanel buildActionArea() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(new Color(0x0d0d0d));
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 20)),
                new EmptyBorder(10, 10, 10, 10)));

        logBox = new JLabel("Cargando...");
        logBox.setForeground(CREAM);
        logBox.setFont(new Font("SansSerif", Font.ITALIC, 12));
        logBox.setBorder(new EmptyBorder(4, 4, 8, 4));
        logBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(logBox);

        actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        actionPanel.setOpaque(false);
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(actionPanel);

        raisePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        raisePanel.setOpaque(false);
        raisePanel.setVisible(false);
        raisePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        raiseSpinner = new JSpinner(new SpinnerNumberModel(50, 10, 2000, 10));
        JButton confirmRaise = new JButton("Confirmar Raise");
        styleButton(confirmRaise, new Color(0x3d2f00), new Color(0xffd060));
        confirmRaise.addActionListener(e -> {
            int amt = (Integer) raiseSpinner.getValue();
            raisePanel.setVisible(false);
            engine.playerRaise(amt);
        });
        JLabel montoLabel = new JLabel("Monto:");
        montoLabel.setForeground(CREAM);
        raisePanel.add(montoLabel);
        raisePanel.add(raiseSpinner);
        raisePanel.add(confirmRaise);
        container.add(raisePanel);

        return container;
    }

    private JPanel buildHistoryPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(new Color(0x0d0d0d));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 20)),
                new EmptyBorder(10, 10, 10, 10)));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        JLabel title = new JLabel("HISTORIAL DE PARTIDAS");
        title.setForeground(new Color(212, 175, 55, 180));
        title.setFont(new Font("SansSerif", Font.BOLD, 11));

        JPanel histButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        histButtons.setOpaque(false);
        JButton exportBtn = new JButton("Exportar CSV");
        JButton clearBtn  = new JButton("Borrar Historial");
        styleButton(exportBtn, new Color(0x0f2a50), new Color(0x7db8ff));
        styleButton(clearBtn,  new Color(0x1a1a1a), new Color(0x999999));
        exportBtn.addActionListener(e -> exportCsv());
        clearBtn.addActionListener (e -> clearHistory());
        histButtons.add(exportBtn); histButtons.add(clearBtn);
        headerRow.add(title,       BorderLayout.WEST);
        headerRow.add(histButtons, BorderLayout.EAST);
        p.add(headerRow, BorderLayout.NORTH);

        String[] cols = {"Ronda","Tus Cartas","Tu Mano","Mano CPU",
                         "Comunitarias","Resultado","Δ Fichas","Fichas"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(historyModel);
        historyTable.setBackground(new Color(0x0d0d0d));
        historyTable.setForeground(new Color(0xc8bfb0));
        historyTable.setGridColor(new Color(255, 255, 255, 20));
        historyTable.setRowHeight(20);
        historyTable.getTableHeader().setForeground(GOLD);
        historyTable.getTableHeader().setBackground(new Color(0x0d0d0d));
        historyTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 10));
        JScrollPane sp = new JScrollPane(historyTable);
        sp.setPreferredSize(new Dimension(640, 200));
        sp.getViewport().setBackground(new Color(0x0d0d0d));
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private void styleButton(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBorder(new EmptyBorder(7, 14, 7, 14));
        b.setOpaque(true);
    }

    // ─── GameListener ─────────────────────────────────────────────────────────

    @Override
    public void onLog(String text) {
        logBox.setText("<html><div style='width:560px'>" + escapeHtml(text) + "</div></html>");
    }

    @Override
    public void onBanner(String text) {
        bannerLabel.setText(text);
        bannerOverlay.setVisible(true);
        if (bannerTimer != null) bannerTimer.stop();
        bannerTimer = new Timer(2400, e -> bannerOverlay.setVisible(false));
        bannerTimer.setRepeats(false);
        bannerTimer.start();
    }

    @Override
    public void onStateChanged() {
        GameState g = engine.g;

        boolean dbOk = engine.getDatabase().isAvailable();
        dbLabel.setText(dbOk ? "DB conectada ✓" : "Sin almacenamiento");
        dbLabel.setForeground(dbOk ? new Color(0x4dcc7a) : new Color(0xe05555));

        sRound.setText(String.valueOf(g.round));
        sWins.setText(String.valueOf(g.wins));
        sLoss.setText(String.valueOf(g.losses));
        sBest.setText(String.valueOf(g.bestStreak));
        sProfit.setText((g.profit >= 0 ? "+" : "") + g.profit);
        sProfit.setForeground(g.profit >= 0 ? new Color(0x4dcc7a) : new Color(0xe05555));

        playerChipBadge.setText("🪙 " + g.playerChips);
        cpuChipBadge.setText   ("🪙 " + g.cpuChips);
        potAmountLabel.setText(String.valueOf(g.pot));

        playerBetBadge.setVisible(g.playerBet > 0);
        if (g.playerBet > 0) playerBetBadge.setText("Apuesta: " + g.playerBet);
        cpuBetBadge.setVisible(g.cpuBet > 0);
        if (g.cpuBet > 0) cpuBetBadge.setText("Apuesta: " + g.cpuBet);

        playerCardsRow.show(g.playerHand, false);
        communityCardsRow.show(g.community, false);
        cpuCardsRow.show(g.cpuHand, g.phase != Phase.SHOWDOWN);

        updatePhaseStrip(g.phase);
    }

    private void updatePhaseStrip(Phase phase) {
        int cur;
        switch (phase) {
            case PREFLOP:  cur = 0; break;
            case FLOP:     cur = 1; break;
            case TURN:     cur = 2; break;
            case RIVER:    cur = 3; break;
            case SHOWDOWN: cur = 4; break;
            default:       cur = -1;
        }
        for (int i = 0; i < 5; i++) {
            JLabel l = phaseSteps[i];
            if (i < cur) {
                l.setForeground(new Color(212, 175, 55, 150));
                l.setBorder(BorderFactory.createLineBorder(new Color(212, 175, 55, 80)));
            } else if (i == cur) {
                l.setForeground(GOLD);
                l.setBorder(BorderFactory.createLineBorder(GOLD));
            } else {
                l.setForeground(new Color(255, 255, 255, 90));
                l.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 40)));
            }
        }
    }

    @Override
    public void onAwaitingPlayerAction() {
        raisePanel.setVisible(false);
        actionPanel.removeAll();

        JButton fold = new JButton("Fold");
        styleButton(fold, new Color(0x6b1515), new Color(0xffb5b5));
        fold.addActionListener(e -> engine.playerAct(ActionType.FOLD));
        actionPanel.add(fold);

        if (engine.canCheck()) {
            JButton check = new JButton("Check");
            styleButton(check, new Color(0x0f3d1f), new Color(0x7dffaa));
            check.addActionListener(e -> engine.playerAct(ActionType.CHECK));
            actionPanel.add(check);
        } else {
            int callAmt = engine.callAmount();
            JButton call = new JButton("Call " + callAmt);
            styleButton(call, new Color(0x0f2a50), new Color(0x7db8ff));
            call.addActionListener(e -> engine.playerAct(ActionType.CALL));
            actionPanel.add(call);
        }

        JButton raise = new JButton("Raise");
        styleButton(raise, new Color(0x3d2f00), new Color(0xffd060));
        raise.addActionListener(e -> openRaise());
        actionPanel.add(raise);

        if (engine.g.playerChips > 0) {
            JButton allin = new JButton("All-In (" + engine.g.playerChips + ")");
            styleButton(allin, new Color(0x4a0a4a), new Color(0xffaaff));
            allin.addActionListener(e -> engine.playerAct(ActionType.ALLIN));
            actionPanel.add(allin);
        }

        actionPanel.revalidate();
        actionPanel.repaint();
    }

    private void openRaise() {
        int min = engine.minRaise();
        int max = Math.max(min, engine.g.playerChips);
        int def = Math.min(min + engine.g.blind, engine.g.playerChips);
        if (def < min) def = min;
        raiseSpinner.setModel(new SpinnerNumberModel(def, min, max, 10));
        raisePanel.setVisible(true);
        raisePanel.revalidate();
    }

    @Override
    public void onRoundEnded() {
        raisePanel.setVisible(false);
        actionPanel.removeAll();

        JButton newRound = new JButton("Nueva Ronda");
        styleButton(newRound, new Color(0xc9a84c), new Color(0x1a0e00));
        newRound.addActionListener(e -> engine.newRound());
        actionPanel.add(newRound);

        JButton reset = new JButton("Reiniciar");
        styleButton(reset, new Color(0x2a1a1a), new Color(0xbbbbbb));
        reset.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Reiniciar el juego? Se perderán las fichas actuales.",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) engine.resetGame();
        });
        actionPanel.add(reset);

        actionPanel.revalidate();
        actionPanel.repaint();
        loadHistory();
    }

    private void loadHistory() {
        if (historyModel == null) return;
        historyModel.setRowCount(0);
        List<RoundRecord> rows = engine.getDatabase().getRounds();
        if (rows == null) return;
        for (int i = rows.size() - 1; i >= Math.max(0, rows.size() - 40); i--) {
            RoundRecord r = rows.get(i);
            String delta = r.delta > 0 ? "+" + r.delta : String.valueOf(r.delta);
            historyModel.addRow(new Object[]{
                r.round, r.playerCards, r.playerHandName, r.cpuHandName,
                r.community, r.resultado, delta, r.fichas
            });
        }
    }

    private void clearHistory() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Esto borrará todo el historial guardado. ¿Continuar?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            engine.getDatabase().clearRounds();
            loadHistory();
            onLog("Historial borrado.");
        }
    }

    private void exportCsv() {
        List<RoundRecord> rows = engine.getDatabase().getRounds();
        if (rows == null || rows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay datos para exportar.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("poker_historial.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Path dest = chooser.getSelectedFile().toPath();
                engine.getDatabase().exportCSV(dest);
                JOptionPane.showMessageDialog(this, "Exportado a: " + dest);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage());
            }
        }
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ─── Clase interna CardRow ────────────────────────────────────────────────

    private static class CardRow extends JPanel {
        private final CardView[] slots;

        CardRow(int n) {
            super(new FlowLayout(FlowLayout.CENTER, 6, 4));
            setOpaque(false);
            slots = new CardView[n];
            for (int i = 0; i < n; i++) {
                slots[i] = new CardView();
                add(slots[i]);
            }
        }

        void show(List<Card> cards, boolean hidden) {
            for (int i = 0; i < slots.length; i++) {
                if (cards != null && i < cards.size()) {
                    slots[i].setCard(cards.get(i), hidden);
                } else {
                    slots[i].setCard(null, false);
                }
            }
        }
    }
}
