package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main menu
 * @author Andrew
 */
public class MenuWindow extends JFrame {

    private GridBagConstraints gc;

    /**
     * Constructor
     */
    public MenuWindow() {
        setLayout(new GridBagLayout());
        gc = new GridBagConstraints();
        JButton singleplayer = new JButton("Single Player");
        JButton multiplayer = new JButton("Multi player");
        singleplayer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getContentPane().removeAll();
                constructSingleplayerForm();
                pack();
                revalidate();
                repaint();
            }
        });
        multiplayer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getContentPane().removeAll();
                constructMultiplayerForm();
                pack();
                revalidate();
                repaint();
            }
        });

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setPreferredSize(new Dimension(screen.width /3, screen.height /3));
        add(singleplayer);
        add(multiplayer);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    /**
     * Single player form
     */
    public void constructSingleplayerForm() {
        gc.gridx = 0;
        gc.gridy = 0;
        add(new JLabel("Rows: "), gc);

        gc.gridx = 1;
        gc.gridy = 0;
        JTextField rows = new JTextField();
        rows.setPreferredSize(new Dimension(200, 30));
        add(rows, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        add(new JLabel("Cols: "), gc);

        gc.gridx = 1;
        gc.gridy = 1;
        JTextField cols = new JTextField();
        cols.setPreferredSize(new Dimension(200, 30));
        add(cols, gc);

        gc.gridx = 1;
        gc.gridy = 2;
        JButton submit = new JButton("Play");
        submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EventQueue.invokeLater(() -> new GameWindow(Integer.parseInt(rows.getText()), Integer.parseInt(cols.getText())));
            }
        });
        add(submit, gc);
    }

    /**
     * Multiplayer form
     */
    public void constructMultiplayerForm() {
        gc.gridx = 0;
        gc.gridy = 0;
        add(new JLabel("Ip: "), gc);

        gc.gridx = 1;
        gc.gridy = 0;
        JTextField ip = new JTextField();
        ip.setPreferredSize(new Dimension(200, 30));
        add(ip, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        add(new JLabel("Port: "), gc);

        gc.gridx = 1;
        gc.gridy = 1;
        JTextField port = new JTextField();
        port.setPreferredSize(new Dimension(200, 30));
        add(port, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        add(new JLabel("Display Name: "), gc);

        gc.gridx = 1;
        gc.gridy = 2;
        JTextField name = new JTextField();
        name.setPreferredSize(new Dimension(200, 30));
        add(name, gc);

        gc.gridx = 1;
        gc.gridy = 3;
        JButton submit = new JButton("Play");
        submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EventQueue.invokeLater(() -> new GameWindow(ip.getText(), Integer.parseInt(port.getText()), name.getText()));
            }
        });
        add(submit, gc);
    }
}
