package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameChatPanel extends JPanel {

    private JTextArea chatPane;
    private JTextField chatField;
    private SendMessageListener sendMessageListener;

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

    public void addToChat(String msg) {
        chatPane.append(msg);
    }

    public void setSendMessageListener(SendMessageListener sendMessageListener) {
        this.sendMessageListener = sendMessageListener;
    }
}
