package GUI;

import Networking.ChatEvent;
import Networking.ChatListener;
import Networking.ServerConnectionEvent;
import Networking.ServerConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Window for game
 *
 * @author Andrew
 */
public class GameWindow extends JFrame {

    private JLabel score;
    private String scoreInner;
    private String goalScoreInner;
    private GamePanel gamePanel;
    private GameChatPanel gameChatPanel;
    private GridBagConstraints gc;
    private MouseAdapter mouseAdapter;


    /**
     * Initialize GUI
     */
    public void initialize() {
        setResizable(false);
        setLayout(new GridBagLayout());
        gc = new GridBagConstraints();
        mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ((JComponent)e.getSource()).requestFocusInWindow();
            }
        };
    }

    /**
     * Construct text window
     * @param text to use
     */
    public void constructTextWindow(String text) {
        JFrame window = new JFrame();
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Arial", Font.BOLD, 30));
        window.add(textLabel);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.pack();
        window.setVisible(true);
    }

    /**
     * Displays text window
     * @param text to use
     */
    public void displayTextWindow(String text) {
        EventQueue.invokeLater(() -> constructTextWindow(text));
    }


    /**
     * Constructor
     * @param ip server
     * @param port server
     * @param name display name
     */
    public GameWindow(String ip, int port, String name) {
        initialize();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("ayo");
                gamePanel.disconnect();
                dispose();
            }
        });

        scoreInner = "Score: 1";
        goalScoreInner = "\nGoal: -1";
        score = new JLabel(scoreInner + goalScoreInner);
        gamePanel = new GamePanel(ip, port, name);
        gamePanel.addMouseListener(mouseAdapter);
        gamePanel.setScoreUpdateListener(new ScoreUpdateListener() {
            @Override
            public void scoreUpdateEventOccurred(ScoreUpdateEvent event) {
                scoreInner = "Score: " + event.getNewScore();
                score.setText(scoreInner + goalScoreInner);
            }

            @Override
            public void updateGoalScore(ScoreUpdateEvent event) {
                goalScoreInner = "\nGoal: " + event.getNewScore();
                score.setText(scoreInner + goalScoreInner);
            }
        });
        gamePanel.setChatListener(new ChatListener() {
            @Override
            public void chatEventOccurred(ChatEvent event) {
                String msg = "<" + event.getSender() + "> " + event.getMsg() + "\n";
                gameChatPanel.addToChat(msg);
            }
        });
        gamePanel.setServerConnectionListener(new ServerConnectionListener() {
            @Override
            public void connectionEventOccurred(ServerConnectionEvent event) {
                if (event.getType() == ServerConnectionEvent.EXPECTED_DISCONNECT) {
                    displayTextWindow("Disconnected from server.");
                } else if (event.getType() == ServerConnectionEvent.UNEXPECTED_DISCONNECT) {
                    displayTextWindow("Your connection with the server was disrupted.");
                } else if (event.getType() == ServerConnectionEvent.UNSUCCESSFUL_CONNECTION_ATTEMPT) {
                    displayTextWindow("The server doesn't exist or there was an error with your network.");
                }
                dispose();
            }
        });
        gameChatPanel = new GameChatPanel(gamePanel.getPreferredSize());
        gameChatPanel.setSendMessageListener(new SendMessageListener() {
            @Override
            public void sendMessageEventOccurred(SendMessageEvent event) {
                gamePanel.sendChat(new ChatEvent(this, name, event.getSentMsg()));
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

    /**
     * Single player constructor
     * @param rows
     * @param cols
     */
    public GameWindow(int rows, int cols) {
        initialize();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        gamePanel = new GamePanel(rows, cols);
        gamePanel.addMouseListener(mouseAdapter);
        score = new JLabel("Score: 1");
        gamePanel.setScoreUpdateListener(new ScoreUpdateListener() {
            @Override
            public void scoreUpdateEventOccurred(ScoreUpdateEvent event) {
                score.setText("Score: " + event.getNewScore());
            }

            @Override
            public void updateGoalScore(ScoreUpdateEvent ignored) {}
        });

        constructScore();
        constructGamePanel();
        pack();
        setVisible(true);
    }

    /**
     * Constructs score label
     */
    public void constructScore() {
        gc.anchor = GridBagConstraints.LINE_START;
        gc.gridx = 0;
        gc.gridy = 0;
        add(score, gc);
    }

    /**
     * Constructs game panel
     */
    public void constructGamePanel() {
        gc.gridx = 0;
        gc.gridy = 1;
        add(gamePanel, gc);
    }
}
