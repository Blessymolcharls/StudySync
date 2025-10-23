package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Pomodoro Timer - Study Focus Management Tool
 * 
 * This class implements the Pomodoro Technique for effective time management
 * in study sessions. The Pomodoro Technique is a time management method that
 * uses alternating work and break periods to maintain focus and prevent burnout.
 * 
 * Timer Intervals:
 * 1. Focus Session (Pomodoro)
 *    - Duration: 25 minutes
 *    - Purpose: Concentrated study period
 *    - No interruptions allowed
 * 
 * 2. Break Types
 *    - Short Break: 5 minutes (between Pomodoros)
 *    - Long Break: 15 minutes (after 4 Pomodoros)
 * 
 * Features:
 * 1. Visual Elements
 *    - Large digital timer display
 *    - Session type indicators
 *    - Control buttons for each mode
 * 
 * 2. Timer Controls
 *    - Start focus session
 *    - Initialize breaks
 *    - Stop current session
 *    - Resume functionality
 * 
 * 3. Notifications
 *    - Session completion alerts
 *    - Break reminders
 *    - Visual and audio cues
 * 
 * Usage Benefits:
 * - Improved focus and concentration
 * - Reduced mental fatigue
 * - Better time management
 * - Enhanced study effectiveness
 * 
 * @see Timer
 * @see ActionListener
 */
public class PomodoroTimer extends JPanel {
    // Timer components
    private Timer timer;                  // Swing timer for countdown
    private int remainingSeconds;         // Current time remaining
    private JLabel timerLabel;            // Visual display of time
    private JButton startFocusBtn,        // Control buttons for different sessions
            startShortBreakBtn, 
            startLongBreakBtn, 
            stopBtn;

    // Session durations (in seconds)
    private final int FOCUS_DURATION = 25 * 60;   // Standard 25-minute focus session
    private final int SHORT_BREAK = 5 * 60;       // 5-minute short break
    private final int LONG_BREAK = 15 * 60;       // 15-minute long break

    public PomodoroTimer() {
        setLayout(new BorderLayout());

        timerLabel = new JLabel("25:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 48));
        add(timerLabel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        startFocusBtn = new JButton("Start Focus");
        startShortBreakBtn = new JButton("Short Break");
        startLongBreakBtn = new JButton("Long Break");
        stopBtn = new JButton("Stop");

        buttonPanel.add(startFocusBtn);
        buttonPanel.add(startShortBreakBtn);
        buttonPanel.add(startLongBreakBtn);
        buttonPanel.add(stopBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        startFocusBtn.addActionListener(e -> startSession(FOCUS_DURATION, "Focus Session"));
        startShortBreakBtn.addActionListener(e -> startSession(SHORT_BREAK, "Short Break"));
        startLongBreakBtn.addActionListener(e -> startSession(LONG_BREAK, "Long Break"));
        stopBtn.addActionListener(e -> stopSession());
    }

    private void startSession(int durationInSeconds, String sessionName) {
        remainingSeconds = durationInSeconds;
        updateLabel();

        if (timer != null) {
            timer.stop();
        }

        timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                remainingSeconds--;
                updateLabel();
                if (remainingSeconds <= 0) {
                    timer.stop();
                    JOptionPane.showMessageDialog(PomodoroTimer.this, sessionName + " over!");
                }
            }
        });
        timer.start();
    }

    private void stopSession() {
        if (timer != null) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Timer stopped.");
        }
    }

    private void updateLabel() {
        int mins = remainingSeconds / 60;
        int secs = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", mins, secs));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Pomodoro Timer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new PomodoroTimer());
            frame.setSize(400, 250);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
