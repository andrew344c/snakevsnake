package GUI;

import javax.swing.*;
import java.awt.*;

public class MainMenuPanel extends JPanel {
    private JLabel title;
    private JButton multiplayer;
    private JButton singleplayer;
    private JButton settings;

    public MainMenuPanel() {
        title = new JLabel("Game.Snake V Game.Snake");

        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.NONE;
    }
}
