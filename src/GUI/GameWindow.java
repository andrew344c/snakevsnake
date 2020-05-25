package GUI;

import Networking.ChatEvent;
import Networking.ChatListener;

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
    private GameChatPanel gameChatPanel;
    private GridBagConstraints gc;


    public void initialize() {
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        gc = new GridBagConstraints();
    }

    public GameWindow(String ip, int port, String name) {
        initialize();

        score = new JLabel("Score: 1");
        gamePanel = new GamePanel(ip, port);
        gamePanel.setScoreUpdateListener(new ScoreUpdateListener() {
            @Override
            public void scoreUpdateEventOccurred(ScoreUpdateEvent event) {
                score.setText("Score: " + event.getNewScore());
            }
        });
        gamePanel.setChatListener(new ChatListener() {
            @Override
            public void chatEventOccurred(ChatEvent event) {
                String msg = "<" + event.getSender() + "> " + event.getMsg() + "\n";
                gameChatPanel.addToChat(msg);
            }
        });
        gameChatPanel = new GameChatPanel(gamePanel.getPreferredSize());
        gameChatPanel.setSendMessageListener(new SendMessageListener() {
            @Override
            public void sendMessageEventOccurred(SendMessageEvent event) {
                gamePanel.send(new ChatEvent(this, name, event.getSentMsg()));
            }
        });

        constructScore();
        constructGamePanel();
        // Chat Pane
        gc.gridx = 1;
        gc.gridy = 1;
        add(gameChatPanel, gc);

        pack();
        setVisible(true);
    }

    public GameWindow(int rows, int cols) {
        initialize();

        gamePanel = new GamePanel(rows, cols);
        score = new JLabel("Score: 1");
        gamePanel.setScoreUpdateListener(new ScoreUpdateListener() {
            @Override
            public void scoreUpdateEventOccurred(ScoreUpdateEvent event) {
                score.setText("Score: " + event.getNewScore());
            }
        });

        constructScore();
        constructGamePanel();
        pack();
        setVisible(true);
    }

    public void constructScore() {
        gc.anchor = GridBagConstraints.LINE_START;
        gc.gridx = 0;
        gc.gridy = 0;
        add(score, gc);
    }

    public void constructGamePanel() {
        gc.gridx = 0;
        gc.gridy = 1;
        add(gamePanel, gc);
    }
}
