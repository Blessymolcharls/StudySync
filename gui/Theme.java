package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Central UI theme for StudySync — colors, fonts and styling system.
 * 
 * This class provides a comprehensive theming system for the StudySync application,
 * ensuring visual consistency across all components and screens.
 */
public class Theme {
    // Brand Colors - New Theme
    public static final Color DEEP_TEAL = new Color(15, 98, 93);     // #0F625D
    public static final Color LIGHT_MINT = new Color(232, 240, 239); // #E8F0EF
    public static final Color GOLDEN_YELLOW = new Color(245, 184, 46); // #F5B82E
    
    // Button States
    public static final Color DEEP_TEAL_HOVER = new Color(20, 115, 110);
    public static final Color GOLDEN_YELLOW_HOVER = new Color(247, 195, 77);
    
    // Text Colors
    public static final Color TEXT_PRIMARY = new Color(15, 98, 93);    // Deep Teal
    public static final Color TEXT_SECONDARY = new Color(102, 102, 102); // Gray
    public static final Color TEXT_WHITE = new Color(255, 255, 255);
    
    // Border Colors
    public static final Color BORDER_COLOR = new Color(27, 102, 98);  // #1B6662
    public static final Color BORDER_NORMAL = new Color(203, 213, 225);  // Light gray
    public static final Color BORDER_FOCUS = DEEP_TEAL;
    public static final Color BORDER_HOVER = GOLDEN_YELLOW;
    
    // Background Colors
    public static final Color BG_PRIMARY = LIGHT_MINT;
    public static final Color BG_SECONDARY = new Color(233, 239, 239); // #E9EFEF
    public static final Color BG_INPUT = new Color(233, 239, 239); // #E9EFEF

    // Section Theme Colors
    public static final Color PDF_PRIMARY = new Color(235, 244, 246);   // Light Teal for PDF section
    public static final Color PDF_SECONDARY = DEEP_TEAL_HOVER;
    
    public static final Color TASK_PRIMARY = new Color(255, 251, 235);  // Pale Yellow for Task Manager
    public static final Color TASK_SECONDARY = GOLDEN_YELLOW;
    
    public static final Color SCHEDULE_PRIMARY = new Color(235, 246, 240); // Soft Green for Calendar
    public static final Color SCHEDULE_SECONDARY = new Color(46, 125, 50);
    
    public static final Color POMODORO_PRIMARY = new Color(255, 245, 240); // Light Peach for Pomodoro
    public static final Color POMODORO_SECONDARY = new Color(239, 83, 80);

    // Calendar Specific Colors
    public static final Color CALENDAR_HEADER = DEEP_TEAL;
    public static final Color CALENDAR_WEEKEND = new Color(250, 250, 250);
    public static final Color CALENDAR_TODAY = DEEP_TEAL_HOVER;
    public static final Color CALENDAR_SELECTED = GOLDEN_YELLOW;
    public static final Color CALENDAR_EVENT = GOLDEN_YELLOW_HOVER;
    public static final Color CALENDAR_EVENT_TEXT = TEXT_PRIMARY;

    // Typography
    public static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font UI_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font UI_HEADING = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font UI_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font UI_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    
    // Dimensions & Spacing
    public static final int CORNER_RADIUS = 12;
    public static final int INPUT_HEIGHT = 40;
    public static final int BUTTON_HEIGHT = 40;
    public static final Dimension BUTTON_SIZE = new Dimension(150, BUTTON_HEIGHT);
    public static final int SHADOW_OFFSET = 3;
    public static final int SHADOW_ALPHA = 30; // 30% opacity

    public static JButton createPrimaryButton(String text) {
        JButton button = createStyledButton(text, DEEP_TEAL, DEEP_TEAL_HOVER);
        button.setPreferredSize(BUTTON_SIZE);
        return button;
    }

    public static JButton createSecondaryButton(String text) {
        JButton button = createStyledButton(text, GOLDEN_YELLOW, GOLDEN_YELLOW_HOVER);
        button.setPreferredSize(BUTTON_SIZE);
        return button;
    }

    private static JButton createStyledButton(String text, Color baseColor, Color hoverColor) {
        JButton button = new JButton(text) {
            {
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                boolean isPressed = getModel().isPressed();
                boolean isMouseOver = getModel().isRollover();
                
                // Draw shadow first
                if (!isPressed) {
                    g2.setColor(new Color(0, 0, 0, SHADOW_ALPHA));
                    g2.fill(new RoundRectangle2D.Float(
                        SHADOW_OFFSET, SHADOW_OFFSET, 
                        getWidth() - SHADOW_OFFSET, 
                        getHeight() - SHADOW_OFFSET, 
                        CORNER_RADIUS, CORNER_RADIUS
                    ));
                }
                
                // Draw button background
                g2.setColor(isPressed ? baseColor.darker() : 
                          isMouseOver ? hoverColor : baseColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 
                    CORNER_RADIUS, CORNER_RADIUS));
                
                // Draw text
                g2.setColor(TEXT_WHITE);
                g2.setFont(UI_BOLD);
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), textX, isPressed ? textY + 1 : textY);
                
                g2.dispose();
            }
        };
        
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    // Text field styling with hover and focus effects
    public static void styleTextField(JTextField field) {
        field.setFont(UI_FONT);
        field.setPreferredSize(new Dimension(350, 40));
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(DEEP_TEAL);
        field.setSelectionColor(new Color(DEEP_TEAL.getRed(), DEEP_TEAL.getGreen(), DEEP_TEAL.getBlue(), 50));
        
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(RADIUS_MEDIUM, BORDER_COLOR),
            BorderFactory.createEmptyBorder(SPACING_SMALL, SPACING_MEDIUM, SPACING_SMALL, SPACING_MEDIUM)
        ));

        // Add hover and focus effects
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(RADIUS_MEDIUM, GOLDEN_YELLOW),
                    BorderFactory.createEmptyBorder(SPACING_SMALL, SPACING_MEDIUM, SPACING_SMALL, SPACING_MEDIUM)
                ));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(RADIUS_MEDIUM, DEEP_TEAL),
                    BorderFactory.createEmptyBorder(SPACING_SMALL, SPACING_MEDIUM, SPACING_SMALL, SPACING_MEDIUM)
                ));
            }
        });

        field.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!field.hasFocus()) {
                    field.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(RADIUS_MEDIUM, DEEP_TEAL_HOVER),
                        BorderFactory.createEmptyBorder(SPACING_SMALL, SPACING_MEDIUM, SPACING_SMALL, SPACING_MEDIUM)
                    ));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!field.hasFocus()) {
                    field.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(RADIUS_MEDIUM, DEEP_TEAL),
                        BorderFactory.createEmptyBorder(SPACING_SMALL, SPACING_MEDIUM, SPACING_SMALL, SPACING_MEDIUM)
                    ));
                }
            }
        });
    }



    // Rounded border for text fields
    public static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        public RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, width - 1, height - 1, radius, radius));
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius/2, radius/2, radius/2, radius/2);
        }
    }

    /**
     * Style a section panel with appropriate theme colors and effects
     * @param panel The panel to style
     * @param sectionType The type of section ("pdf", "task", "schedule", "pomodoro")
     */
    public static void styleSectionPanel(JPanel panel, String sectionType) {
        Color primaryColor;
        Color secondaryColor;
        
        switch (sectionType.toLowerCase()) {
            case "pdf":
                primaryColor = PDF_PRIMARY;
                secondaryColor = PDF_SECONDARY;
                break;
            case "task":
                primaryColor = TASK_PRIMARY;
                secondaryColor = TASK_SECONDARY;
                break;
            case "schedule":
                primaryColor = SCHEDULE_PRIMARY;
                secondaryColor = SCHEDULE_SECONDARY;
                break;
            case "pomodoro":
                primaryColor = POMODORO_PRIMARY;
                secondaryColor = POMODORO_SECONDARY;
                break;
            default:
                primaryColor = BG_PRIMARY;
                secondaryColor = TEXT_PRIMARY;
                break;
        }

        panel.setBackground(primaryColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(RADIUS_LARGE, secondaryColor),
            BorderFactory.createEmptyBorder(SPACING_MEDIUM, SPACING_MEDIUM, SPACING_MEDIUM, SPACING_MEDIUM)
        ));

        // Add subtle shadow effect
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                new ShadowBorder(),
                new RoundedBorder(RADIUS_LARGE, secondaryColor)
            ),
            BorderFactory.createEmptyBorder(SPACING_MEDIUM, SPACING_MEDIUM, SPACING_MEDIUM, SPACING_MEDIUM)
        ));
    }

    // Class for panel shadow effect
    public static class ShadowBorder extends EmptyBorder {
        public ShadowBorder() {
            super(0, 0, 4, 0);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(0, 0, 0, 30));
            g2d.fillRoundRect(x + 2, y + height - 3, width - 4, 3, 6, 6);
            g2d.dispose();
        }
    }

    /**
     * Style a combo box with the StudySync theme
     * Applies consistent fonts, colors, and rounded borders
     * 
     * @param box The JComboBox to style
     */
    /**
     * Style a combo box with the StudySync theme
     * @param box The combo box to style
     */
    public static void styleComboBox(JComboBox<?> box) {
        box.setFont(UI_FONT);
        box.setForeground(TEXT_PRIMARY);
        box.setBackground(BG_SECONDARY);
        box.setPreferredSize(new Dimension(350, 40));
        
            // Set initial border
        box.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(RADIUS_MEDIUM, BORDER_COLOR),
            BorderFactory.createEmptyBorder(SPACING_SMALL, SPACING_MEDIUM, SPACING_SMALL, SPACING_MEDIUM)
        ));

        // Style the dropdown button and list
        box.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        
                        // Draw arrow
                        int size = 8;
                        int x = (getWidth() - size) / 2;
                        int y = (getHeight() - size) / 2;
                        
                        g2.setColor(DEEP_TEAL);
                        int[] xPoints = {x, x + size, x + size/2};
                        int[] yPoints = {y, y, y + size};
                        g2.fillPolygon(xPoints, yPoints, 3);
                        
                        g2.dispose();
                    }
                };
                button.setBackground(BG_SECONDARY);
                button.setBorder(BorderFactory.createEmptyBorder(0, SPACING_SMALL, 0, SPACING_SMALL));
                button.setContentAreaFilled(false);
                return button;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                // Custom background painting
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_SECONDARY);
                g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, RADIUS_MEDIUM, RADIUS_MEDIUM);
                g2.dispose();
            }
        });

        // Style the dropdown list
        Object comp = box.getUI().getAccessibleChild(box, 0);
        if (comp instanceof JPopupMenu) {
            JPopupMenu popup = (JPopupMenu) comp;
            popup.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(),
                new RoundedBorder(RADIUS_SMALL, DEEP_TEAL)
            ));
            popup.setBackground(BG_SECONDARY);
        }

        // Add hover and focus effects
        box.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                box.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(RADIUS_MEDIUM, DEEP_TEAL_HOVER),
                    BorderFactory.createEmptyBorder(SPACING_SMALL, SPACING_MEDIUM, SPACING_SMALL, SPACING_MEDIUM)
                ));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                box.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(RADIUS_MEDIUM, DEEP_TEAL),
                    BorderFactory.createEmptyBorder(SPACING_SMALL, SPACING_MEDIUM, SPACING_SMALL, SPACING_MEDIUM)
                ));
            }
        });

        // Style the renderer
        box.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                JComponent comp = (JComponent) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
                
                if (isSelected) {
                    comp.setBackground(DEEP_TEAL_HOVER);
                    comp.setForeground(TEXT_WHITE);
                } else {
                    comp.setBackground(BG_SECONDARY);
                    comp.setForeground(TEXT_PRIMARY);
                }
                
                comp.setFont(UI_FONT);
                comp.setBorder(BorderFactory.createEmptyBorder(
                    SPACING_SMALL, SPACING_MEDIUM, SPACING_SMALL, SPACING_MEDIUM));
                
                return comp;
            }
        });
    }

    /**
     * Apply consistent styling to labels in the application.
     * Sets the font, color, and alignment for labels.
     * 
     * @param label The JLabel to style
     */
    public static void styleLabel(JLabel label) {
        label.setFont(UI_BOLD);
        label.setForeground(DEEP_TEAL);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
    }

    /**
     * Style a calendar component with StudySync theme
     * @param panel The panel representing the calendar
     */
    public static void styleCalendar(JPanel panel) {
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12, DEEP_TEAL),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    }

    /**
     * Style a calendar header
     * @param header The header component
     */
    public static void styleCalendarHeader(JComponent header) {
        header.setBackground(CALENDAR_HEADER);
        header.setForeground(TEXT_WHITE);
        header.setFont(UI_BOLD);
        header.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    /**
     * Style a calendar date cell
     * @param cell The cell component
     * @param isWeekend Whether the cell represents a weekend
     * @param isToday Whether the cell represents today
     * @param isSelected Whether the cell is selected
     */
    public static void styleCalendarCell(JComponent cell, boolean isWeekend, boolean isToday, boolean isSelected) {
        cell.setFont(UI_FONT);
        if (isSelected) {
            cell.setBackground(CALENDAR_SELECTED);
            cell.setForeground(TEXT_WHITE);
        } else if (isToday) {
            cell.setBackground(CALENDAR_TODAY);
            cell.setForeground(TEXT_WHITE);
        } else if (isWeekend) {
            cell.setBackground(CALENDAR_WEEKEND);
            cell.setForeground(TEXT_PRIMARY);
        } else {
            cell.setBackground(BG_SECONDARY);
            cell.setForeground(TEXT_PRIMARY);
        }
        cell.setBorder(BorderFactory.createLineBorder(BORDER_NORMAL, 1));
    }

    /**
     * Style a calendar event marker
     * @param event The event component
     */
    public static void styleCalendarEvent(JComponent event) {
        event.setBackground(CALENDAR_EVENT);
        event.setForeground(CALENDAR_EVENT_TEXT);
        event.setFont(UI_SMALL);
        event.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
    }

    /**
     * Style a section header (PDF, Tasks, Schedule, etc.)
     * @param header The header component
     * @param sectionColor The primary color for the section
     */
    public static void styleSectionHeader(JComponent header, Color sectionColor) {
        header.setBackground(sectionColor);
        header.setForeground(TEXT_PRIMARY);
        header.setFont(UI_TITLE);
        header.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, DEEP_TEAL),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
    }

    // Animation durations (in milliseconds)
    public static final int ANIMATION_SHORT = 150;
    public static final int ANIMATION_MEDIUM = 300;
    public static final int ANIMATION_LONG = 500;

    // Spacing constants
    public static final int SPACING_XSMALL = 4;
    public static final int SPACING_SMALL = 8;
    public static final int SPACING_MEDIUM = 16;
    public static final int SPACING_LARGE = 24;
    public static final int SPACING_XLARGE = 32;

    // Border radius constants
    public static final int RADIUS_SMALL = 4;
    public static final int RADIUS_MEDIUM = 8;
    public static final int RADIUS_LARGE = 12;
    public static final int RADIUS_XLARGE = 16;

    private Theme() {}
}
