package GUI;

import javax.swing.*;
import java.awt.*;
import java.util.Scanner;

public class MainWindow extends JFrame {
    public MainWindow() {
        setTitle("Snake Game Demo");

        add(new GamePanel(25, 25));
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(MainWindow::new);
    }
}
