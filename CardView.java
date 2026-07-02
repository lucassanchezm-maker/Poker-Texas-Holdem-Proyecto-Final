package com.pokergame.App;

import javax.swing.*;
import java.awt.*;

public class CardView extends JPanel {
    private Card card;
    private boolean hidden;

    private static final int W = 60, H = 86;
    private static final Color RED   = new Color(0xc0392b);
    private static final Color BLACK = new Color(0x111111);

    public CardView() {
        setPreferredSize(new Dimension(W, H));
        setOpaque(false);
    }

    public void setCard(Card card, boolean hidden) {
        this.card   = card;
        this.hidden = hidden;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (card == null && !hidden) {
            g.setColor(new Color(0, 0, 0, 60));
            g.fillRoundRect(0, 0, W, H, 8, 8);
            g.setColor(new Color(255, 255, 255, 40));
            g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    0, new float[]{4, 3}, 0));
            g.drawRoundRect(1, 1, W - 2, H - 2, 8, 8);
            g.dispose();
            return;
        }

        if (hidden) {
            g.setColor(new Color(0x1a2870));
            g.fillRoundRect(0, 0, W, H, 8, 8);
            g.setColor(new Color(255, 255, 255, 35));
            g.drawRoundRect(4, 4, W - 8, H - 8, 6, 6);
            g.dispose();
            return;
        }

        g.setColor(Color.WHITE);
        g.fillRoundRect(0, 0, W, H, 8, 8);
        g.setColor(new Color(0xbbbbbb));
        g.drawRoundRect(0, 0, W - 1, H - 1, 8, 8);

        Color cl = card.isRed() ? RED : BLACK;
        g.setColor(cl);

        Font cornerFont = new Font("Serif", Font.BOLD, 13);
        g.setFont(cornerFont);
        g.drawString(card.rank, 6, 16);
        g.drawString(card.suit, 6, 30);

        Font centerFont = new Font("Serif", Font.PLAIN, 26);
        g.setFont(centerFont);
        FontMetrics fm = g.getFontMetrics();
        int sw = fm.stringWidth(card.suit);
        g.drawString(card.suit, (W - sw) / 2, H / 2 + fm.getAscent() / 2 - 4);

        g.setFont(cornerFont);
        FontMetrics fm2 = g.getFontMetrics();
        int rw  = fm2.stringWidth(card.rank);
        int sw2 = fm2.stringWidth(card.suit);
        int x = W - 6 - Math.max(rw, sw2);
        g.drawString(card.rank, x, H - 22);
        g.drawString(card.suit, x, H - 8);

        g.dispose();
    }
}
