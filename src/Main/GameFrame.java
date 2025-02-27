package Main;

import javax.swing.*;

public class GameFrame extends JFrame {
    public GameFrame(GamePanel gamePanel){
        setTitle("Runes of Aetheria");
        setResizable(false);
        add(gamePanel);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
}
