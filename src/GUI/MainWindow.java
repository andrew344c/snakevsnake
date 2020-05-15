package GUI;

import javax.swing.*;
import java.awt.*;
import java.util.Scanner;

public class MainWindow extends JFrame {
    public MainWindow(String ip, int port) {
        setTitle("Snake Game Demo");

        add(new GamePanel(ip, port));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new MainWindow(args[0], Integer.parseInt(args[1])));
    }
}
