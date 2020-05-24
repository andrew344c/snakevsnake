package GUI;

import javax.swing.*;
import java.awt.*;

/**
 * Window for game
 *
 * @author Andrew
 */
public class GameWindow extends JFrame {

    private JLabel score;
    private JLabel secondScore;
    private GamePanel gamePanel;

    public GameWindow(String ip, int port) {
        gamePanel = new GamePanel(ip, port);
        score = new JLabel("Score: 1");
        construct();
    }

    public GameWindow(int rows, int cols) {
        gamePanel = new GamePanel(rows, cols);
        score = new JLabel("Score: 1");
        construct();
    }

    public void construct() {
        gamePanel.setScoreUpdateListener(new ScoreUpdateListener() {
            @Override
            public void scoreUpdateEventOccurred(ScoreUpdateEvent event) {
                score.setText("Score: " + event.getNewScore());
            }
        });
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();

        // Score
        gc.anchor = GridBagConstraints.LINE_START;
        gc.gridx = 0;
        gc.gridy = 0;
        add(score, gc);

        // Second Score (High Score (Single-player) or Target Score (Multi-player))
        //gc.anchor = GridBagConstraints.LINE_START;
        //gc.gridx = 0;
        //gc.gridy = 0;
        //add(secondScore, gc);

        // Game
        gc.gridx = 0;
        gc.gridy = 1;
        add(gamePanel, gc);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new GameWindow(25, 25));
    }
}
