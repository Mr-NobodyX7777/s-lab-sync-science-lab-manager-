//I'll warn you i got so bored and tired by this point that my brain decided that the best thing to do about the ui is...
// ... to let AI make the program's UI ...... I don't have enough braincells in my head to do it myself. So i let AI do it for me.
//            \did you get that? :)/     
//Let's hope AI doesn't make the UI look like a complete mess. I mean, it probably will, but let's hope it doesn't. Mr.NobodyX7777 ;)
package com.labmanager.ui;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

/** Liquid-glass flavored UI helpers: frosted cards, glowing pills, animated gradient mesh backgrounds. */
public final class UITheme {

    public static final Color PRIMARY       = new Color(0x24, 0x1F, 0x4D);
    public static final Color PRIMARY_DARK  = new Color(0x17, 0x14, 0x33);
    public static final Color ACCENT        = new Color(0x35, 0xE0, 0xC8);
    public static final Color ACCENT_DARK   = new Color(0x1E, 0xA8, 0x94);
    public static final Color DANGER        = new Color(0xFF, 0x5C, 0x6E);
    public static final Color WARNING       = new Color(0xFF, 0xB4, 0x4D);
    public static final Color SUCCESS       = new Color(0x4C, 0xD9, 0x89);

    private static final Color LIGHT_BG         = new Color(0xCE, 0xD6, 0xE6);
    private static final Color LIGHT_CARD_BG    = new Color(0xFF, 0xFF, 0xFF, 250);
    private static final Color LIGHT_TEXT_DARK  = new Color(0x1D, 0x20, 0x2B);
    private static final Color LIGHT_TEXT_MUTED = new Color(0x51, 0x5A, 0x70);
    private static final Color LIGHT_BORDER     = new Color(0x93, 0x9E, 0xBA, 160);

    private static final Color DARK_BG          = new Color(0x0A, 0x0C, 0x15);
    private static final Color DARK_CARD_BG     = new Color(0x23, 0x25, 0x39, 195);
    private static final Color DARK_TEXT_DARK   = new Color(0xEE, 0xF0, 0xF8);
    private static final Color DARK_TEXT_MUTED  = new Color(0x9B, 0xA1, 0xBC);
    private static final Color DARK_BORDER      = new Color(0xFF, 0xFF, 0xFF, 38);

    private static final Color LIGHT_NEUTRAL_BTN = new Color(0xE3, 0xE7, 0xF0, 240);
    private static final Color DARK_NEUTRAL_BTN  = new Color(0xFF, 0xFF, 0xFF, 26);

    public static boolean darkMode = false;
    public static Color BG         = LIGHT_BG;
    public static Color CARD_BG    = LIGHT_CARD_BG;
    public static Color TEXT_DARK  = LIGHT_TEXT_DARK;
    public static Color TEXT_MUTED = LIGHT_TEXT_MUTED;
    public static Color BORDER     = LIGHT_BORDER;
    public static Color NEUTRAL_BTN_BG = LIGHT_NEUTRAL_BTN;

    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_SUB     = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_LABEL   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BUTTON  = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SIDEBAR = new Font("Segoe UI", Font.PLAIN, 15);

    private UITheme() {}

    private static Image logoImage;
    private static boolean logoLoaded = false;

    public static ImageIcon logoIcon(int size) {
        if (!logoLoaded) {
            logoLoaded = true;
            java.net.URL url = UITheme.class.getResource("/com/labmanager/logo.png");
            logoImage = url == null ? null : new ImageIcon(url).getImage();
        }
        if (logoImage == null) return null;
        return new ImageIcon(logoImage.getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }

    public static void applyGlobalDefaults() {
        UIManager.put("control", BG);
        UIManager.put("nimbusBase", PRIMARY);
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("Table.rowHeight", 32);
        UIManager.put("TableHeader.font", FONT_LABEL.deriveFont(Font.BOLD));
        UIManager.put("TextField.font", FONT_BODY);
        UIManager.put("Label.font", FONT_BODY);
    }

    public static void setDarkMode(boolean on) {
        darkMode = on;
        BG = on ? DARK_BG : LIGHT_BG;
        CARD_BG = on ? DARK_CARD_BG : LIGHT_CARD_BG;
        TEXT_DARK = on ? DARK_TEXT_DARK : LIGHT_TEXT_DARK;
        TEXT_MUTED = on ? DARK_TEXT_MUTED : LIGHT_TEXT_MUTED;
        BORDER = on ? DARK_BORDER : LIGHT_BORDER;
        NEUTRAL_BTN_BG = on ? DARK_NEUTRAL_BTN : LIGHT_NEUTRAL_BTN;
        UIManager.put("control", BG);
    }

    private static Color withAlpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    private static Color brighten(Color c, int amt) {
        return new Color(Math.min(255, c.getRed() + amt), Math.min(255, c.getGreen() + amt), Math.min(255, c.getBlue() + amt), c.getAlpha());
    }

    /** Full-bleed animated "liquid" gradient mesh, meant to sit behind everything else. */
    public static JPanel meshBackground() {
        return new MeshPanel();
    }

    private static final class MeshPanel extends JPanel {
        private float t = 0f;
        private final Timer timer;
        MeshPanel() {
            setOpaque(true);
            timer = new Timer(50, e -> { t += 0.006f; repaint(); });
            timer.start();
        }
        @Override public void removeNotify() {
            super.removeNotify();
            timer.stop();
        }
        @Override protected void paintComponent(Graphics g) {
            int w = Math.max(1, getWidth()), h = Math.max(1, getHeight());
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(BG);
            g2.fillRect(0, 0, w, h);
            Color[] blobs = darkMode
                    ? new Color[]{ new Color(0x5B, 0x3A, 0xE0, 130), new Color(0x18, 0xC9, 0xB0, 110), new Color(0xE0, 0x3A, 0x8C, 85), new Color(0x2A, 0x55, 0xE0, 110) }
                    : new Color[]{ new Color(0x4F, 0x8E, 0xF7, 140), new Color(0x18, 0xC9, 0xB0, 130), new Color(0x6C, 0x5C, 0xE0, 120), new Color(0x2A, 0xC7, 0xE0, 130) };
            float radius = Math.max(w, h) * 0.62f;
            for (int i = 0; i < blobs.length; i++) {
                double angle = t * (0.55 + i * 0.21) + i * 2.1;
                float cx = (float) (w * (0.5 + 0.4 * Math.cos(angle + i)));
                float cy = (float) (h * (0.5 + 0.4 * Math.sin(angle * 0.75 - i)));
                RadialGradientPaint rg = new RadialGradientPaint(new Point2D.Float(cx, cy), radius,
                        new float[]{0f, 1f}, new Color[]{blobs[i], withAlpha(blobs[i], 0)});
                g2.setPaint(rg);
                g2.fillRect(0, 0, w, h);
            }
            g2.dispose();
        }
    }

    /** Translucent frosted panel, rectangular when arc is 0 or rounded otherwise. Used for bars/docks. */
    public static JPanel glassPanel(int arc) {
        return new GlassPanel(arc);
    }

    private static final class GlassPanel extends JPanel {
        private final int arc;
        GlassPanel(int arc) { this.arc = arc; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            int w = getWidth(), h = getHeight();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = darkMode ? new Color(0x14, 0x16, 0x24, 165) : new Color(0xF3, 0xF5, 0xFA, 235);
            Shape shape = arc > 0 ? new RoundRectangle2D.Double(0, 0, w - 1, h - 1, arc, arc) : new Rectangle(0, 0, w, h);
            g2.setColor(fill);
            g2.fill(shape);
            g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, darkMode ? 22 : 95), 0, h * 0.55f, withAlpha(Color.WHITE, 0)));
            g2.fill(shape);
            if (arc > 0) {
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1.1f));
                g2.draw(shape);
            }
            g2.dispose();
        }
    }

    /** Frosted glass card with soft drop shadow and glossy top highlight. */
    public static JPanel card() {
        return new GlassCard();
    }

    private static final class GlassCard extends JPanel {
        private static final int SHADOW = 10;
        GlassCard() {
            setOpaque(false);
            setBorder(new EmptyBorder(18 + SHADOW, 18 + SHADOW, 20 + SHADOW, 18 + SHADOW));
        }
        @Override protected void paintComponent(Graphics g) {
            int w = getWidth(), h = getHeight();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int x0 = SHADOW, y0 = SHADOW, w0 = Math.max(1, w - SHADOW * 2), h0 = Math.max(1, h - SHADOW * 2 - 3);
            for (int i = SHADOW; i > 0; i--) {
                int alpha = (int) (22 * (1 - (i / (float) SHADOW))) + 3;
                int grow = SHADOW - i;
                g2.setColor(new Color(0, 0, 0, darkMode ? alpha + 10 : alpha));
                g2.fill(new RoundRectangle2D.Double(x0 - grow, y0 - grow + 5, w0 + grow * 2.0, h0 + grow * 2.0, 24, 24));
            }
            RoundRectangle2D.Double rect = new RoundRectangle2D.Double(x0, y0, w0, h0, 22, 22);
            g2.setPaint(new GradientPaint(0, y0, brighten(CARD_BG, darkMode ? 14 : 6), 0, y0 + h0, CARD_BG));
            g2.fill(rect);
            g2.setPaint(new GradientPaint(0, y0, new Color(255, 255, 255, darkMode ? 34 : 140), 0, y0 + h0 / 2.6f, withAlpha(Color.WHITE, 0)));
            g2.fill(new RoundRectangle2D.Double(x0, y0, w0, h0 / 2.6, 22, 22));
            g2.setColor(BORDER);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(rect);
            g2.dispose();
        }
    }

    /** Rounded, colored glass button used across the app. */
    public static JButton roundButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight() - 6;
                boolean pressed = getModel().isPressed();
                boolean hover = getModel().isRollover();
                Color base = pressed ? bg.darker() : bg;
                int arc = 12;
                g2.setColor(withAlpha(bg, hover ? 100 : 60));
                g2.fill(new RoundRectangle2D.Double(2, 5, w - 4, h, arc, arc));
                g2.setPaint(new GradientPaint(0, 0, hover ? brighten(base, 18) : base, 0, h, base.darker()));
                g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, arc, arc));
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 100), 0, h / 2f, withAlpha(Color.WHITE, 0)));
                g2.fill(new RoundRectangle2D.Double(1, 1, w - 3, h / 2.0, arc, arc));
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) { /* no border */ }
        };
        b.setFont(FONT_BUTTON);
        b.setForeground(fg);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(11, 22, 16, 22));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton primaryButton(String text) { return roundButton(text, ACCENT, Color.WHITE); }
    public static JButton dangerButton(String text) { return roundButton(text, DANGER, Color.WHITE); }
    public static JButton neutralButton(String text) { return roundButton(text, NEUTRAL_BTN_BG, TEXT_DARK); }

    public static JTextField field() {
        JTextField f = new JTextField();
        styleField(f);
        return f;
    }

    public static void styleField(JComponent f) {
        f.setFont(FONT_BODY);
        f.setOpaque(true);
        f.setBackground(darkMode ? new Color(255, 255, 255, 18) : new Color(255, 255, 255, 210));
        f.setForeground(TEXT_DARK);
        if (f instanceof JTextField) ((JTextField) f).setCaretColor(TEXT_DARK);
        applyFieldBorder(f, false);
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { applyFieldBorder(f, true); }
            @Override public void focusLost(FocusEvent e) { applyFieldBorder(f, false); }
        });
    }

    private static void applyFieldBorder(JComponent f, boolean focused) {
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(focused ? ACCENT : BORDER, focused ? 2 : 1, true),
                new EmptyBorder(8, 10, 8, 10)));
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    public static void styleTableHeader(JTableHeader header) {
        Color bg = darkMode ? new Color(0x1C, 0x1E, 0x30) : new Color(0x2E, 0x2A, 0x55);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                c.setOpaque(true);
                c.setBackground(bg);
                c.setForeground(Color.WHITE);
                c.setFont(FONT_LABEL.deriveFont(Font.BOLD));
                c.setBorder(new EmptyBorder(8, 12, 8, 12));
                return c;
            }
        });
        header.setBackground(bg);
        header.setForeground(Color.WHITE);
        header.setFont(FONT_LABEL.deriveFont(Font.BOLD));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));
        header.setOpaque(true);
    }

    public static void styleTable(JTable table) {
        table.setBackground(withAlpha(CARD_BG, 0));
        table.setForeground(TEXT_DARK);
        table.setGridColor(BORDER);
        table.setSelectionBackground(ACCENT_DARK);
        table.setSelectionForeground(Color.WHITE);
        table.setOpaque(false);
        final Color stripeA = darkMode ? new Color(255, 255, 255, 10) : new Color(0, 0, 0, 22);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? withAlpha(Color.WHITE, 0) : stripeA);
                    c.setForeground(TEXT_DARK);
                    ((JComponent) c).setOpaque(row % 2 != 0);
                }
                return c;
            }
        });
    }

    public static void styleScrollPane(JScrollPane sp) {
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUI(new ThemedScrollBarUI());
        sp.getVerticalScrollBar().setOpaque(false);
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        sp.getHorizontalScrollBar().setUI(new ThemedScrollBarUI());
        sp.getHorizontalScrollBar().setOpaque(false);
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
    }

    private static final class ThemedScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = BORDER;
            trackColor = withAlpha(Color.BLACK, 0);
        }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(withAlpha(ACCENT, darkMode ? 130 : 150));
            g2.fill(new RoundRectangle2D.Double(r.x + 2, r.y + 2, Math.max(0, r.width - 4), Math.max(0, r.height - 4), 8, 8));
            g2.dispose();
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) { /* transparent track */ }
        @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }
        private JButton zeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }
    }

    public static Color tint(Color c, double whiteFactor) {
        int r = (int) (c.getRed()   * (1 - whiteFactor) + 255 * whiteFactor);
        int g = (int) (c.getGreen() * (1 - whiteFactor) + 255 * whiteFactor);
        int b = (int) (c.getBlue()  * (1 - whiteFactor) + 255 * whiteFactor);
        return new Color(Math.min(255, Math.max(0, r)), Math.min(255, Math.max(0, g)), Math.min(255, Math.max(0, b)), darkMode ? 60 : 140);
    }

    /** Small round swatch button showing a color, used for the "pick a color for this experiment" control. */
    public static JButton colorSwatchButton(Color initial, int size) {
        JButton b = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor((Color) getClientProperty("swatchColor"));
                g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                g2.setColor(new Color(255, 255, 255, 120));
                g2.drawOval(2, 2, getWidth() - 4, getHeight() - 4);
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        b.putClientProperty("swatchColor", initial);
        b.setPreferredSize(new Dimension(size, size));
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder());
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static void setSwatchColor(JButton swatch, Color c) {
        swatch.putClientProperty("swatchColor", c);
        swatch.repaint();
    }

    public static Color getSwatchColor(JButton swatch) {
        return (Color) swatch.getClientProperty("swatchColor");
    }

    /** Preset palette so users can quickly tag experiments by subject/type without opening a full color picker. */
    public static final Color[] EXPERIMENT_PALETTE = {
            new Color(0xE6, 0x39, 0x46),
            new Color(0x45, 0x7B, 0x9D),
            new Color(0x2E, 0xC4, 0xB6),
            new Color(0x43, 0xAA, 0x8B),
            new Color(0xF4, 0xA2, 0x61),
            new Color(0xFF, 0xB7, 0x03),
            new Color(0x8E, 0x44, 0xAD),
            new Color(0x6D, 0x69, 0x75),
    };

    public static String toHex(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    public static Color fromHex(String hex, Color fallback) {
        if (hex == null || hex.isBlank()) return fallback;
        try { return Color.decode(hex); } catch (Exception e) { return fallback; }
    }

    /** Sidebar nav pill that glows when selected or hovered. */
    public static JButton sidebarButton(String text, boolean selected) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                boolean sel = isNavSelected(this);
                boolean hover = getModel().isRollover();
                int arc = 12;
                if (sel) {
                    g2.setColor(withAlpha(ACCENT, 70));
                    g2.fill(new RoundRectangle2D.Double(2, 0, w - 4, h, arc, arc));
                    g2.setPaint(new GradientPaint(0, 0, ACCENT, w, h, ACCENT_DARK));
                    g2.fill(new RoundRectangle2D.Double(6, 3, w - 12, h - 6, arc, arc));
                    g2.setColor(new Color(255, 255, 255, 55));
                    g2.fill(new RoundRectangle2D.Double(6, 3, w - 12, (h - 6) / 2.0, arc, arc));
                } else if (hover) {
                    g2.setColor(darkMode ? new Color(255, 255, 255, 20) : new Color(255, 255, 255, 110));
                    g2.fill(new RoundRectangle2D.Double(6, 3, w - 12, h - 6, arc, arc));
                }
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        b.setFont(FONT_SIDEBAR);
        b.putClientProperty("navSelected", selected);
        b.setForeground(selected ? Color.WHITE : TEXT_MUTED);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(new EmptyBorder(13, 22, 13, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.repaint(); }
            @Override public void mouseExited(MouseEvent e) { b.repaint(); }
        });
        return b;
    }

    public static void setNavSelected(JButton b, boolean selected) {
        b.putClientProperty("navSelected", selected);
        b.setForeground(selected ? Color.WHITE : TEXT_MUTED);
        b.repaint();
    }

    private static boolean isNavSelected(JComponent b) {
        Boolean sel = (Boolean) b.getClientProperty("navSelected");
        return sel != null && sel;
    }

    /** Window control button (minimize/maximize/close) drawn with a real icon glyph, not just a colored dot. */
    public static JButton windowControlButton(String type, boolean danger) {
        JButton b = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                boolean hover = getModel().isRollover();
                if (hover) {
                    g2.setColor(danger ? DANGER : (darkMode ? new Color(255, 255, 255, 30) : new Color(0, 0, 0, 18)));
                    g2.fill(new RoundRectangle2D.Double(2, 2, w - 4, h - 4, 10, 10));
                }
                g2.setColor(danger && hover ? Color.WHITE : TEXT_DARK);
                g2.setStroke(new BasicStroke(1.4f));
                int cx = w / 2, cy = h / 2;
                if (type.equals("min")) {
                    g2.drawLine(cx - 5, cy + 4, cx + 5, cy + 4);
                } else if (type.equals("max")) {
                    g2.draw(new RoundRectangle2D.Double(cx - 5, cy - 5, 10, 10, 2, 2));
                } else {
                    g2.drawLine(cx - 5, cy - 5, cx + 5, cy + 5);
                    g2.drawLine(cx - 5, cy + 5, cx + 5, cy - 5);
                }
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        b.setPreferredSize(new Dimension(38, 32));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
