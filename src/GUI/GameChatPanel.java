package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Chat Panel for Game
 */
public class GameChatPanel extends JPanel {

    private JTextArea chatPane;
    private JTextField chatField;
    private SendMessageListener sendMessageListener;

    /**
     * Constructor
     * @param gamePanelDimension Dimension of game panel
     */
    public GameChatPanel(Dimension gamePanelDimension) {
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        chatPane = new JTextArea();
        chatPane.setEditable(false);
        chatPane.setBackground(Color.LIGHT_GRAY);
        chatPane.setPreferredSize(new Dimension(500, gamePanelDimension.height - 30));
        JScrollPane chatPaneWrapper = new JScrollPane(chatPane);
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessageListener.sendMessageEventOccurred(new SendMessageEvent(this, chatField.getText()));
                chatField.setText("");
            }
        });
        chatField = new JTextField();
        chatField.setPreferredSize(new Dimension(500 - sendButton.getPreferredSize().width, 30));


        // Chat Pane
        gc.gridx = 0;
        gc.gridy = 0;
        add(chatPaneWrapper, gc);

        // Chat Field
        gc.anchor = GridBagConstraints.LINE_START;
        gc.gridx = 0;
        gc.gridy = 1;
        add(chatField, gc);

        // Send Button
        gc.anchor = GridBagConstraints.LINE_START;
        gc.gridx = 1;
        gc.gridy = 1;
        add(sendButton, gc);
    }

    /**
     * Adds to chat
     * @param msg to be added
     */
    public void addToChat(String msg) {
        chatPane.append(msg);
    }

    /**
     * Set listener
     * @param sendMessageListener for direct communication to ClientService
     */
    public void setSendMessageListener(SendMessageListener sendMessageListener) {
        this.sendMessageListener = sendMessageListener;
    }
}
