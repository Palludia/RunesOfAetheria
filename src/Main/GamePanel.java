package Main;

import Entity.Player;
import PlayerKeyHandler.KeyHandler;

import javax.swing.*;
import java.awt.*;

import static Utilz.Constants.Directions.*;

public class GamePanel extends JPanel {
    final int originalTileSize = 32; // 32 x 32 tile
    final int scale = 2;
    final int tileSize = originalTileSize * scale; // 64 x 64 tile that would be displayed
    final int maxScreenCol = 20;
    final int maxScreenRow = 12;
    final int screenWidth = tileSize * maxScreenCol; // 1280
    final int screenHeight = tileSize * maxScreenRow; // 768


    // Set Player's default position
    KeyHandler keyHandler = new KeyHandler(this);
    Player player = new Player(this,keyHandler);


    public GamePanel() {
        setPreferredSize(new Dimension(screenWidth,screenHeight));
        setBackground(Color.black);
        setDoubleBuffered(true);
        setFocusable(true);
        addKeyListener(keyHandler);
        requestFocusInWindow();
    }

    public void setDir(int playerDir) {
        player.setPlayerDir(playerDir);
    }

    public void isMoving(boolean isMoving){
        player.isMoving(isMoving);
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;

        player.draw(g2);

        g2.dispose();
    }

    public void updatePos() {
        player.updatePos();
    }

    public int getTileSize() {
        return tileSize;
    }
}
