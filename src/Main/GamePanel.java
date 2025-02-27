package Main;

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
    private int playerDir = -1;
    private boolean isMoving = false;

    // Set Player's default position
    int playerX = 640;
    int playerY = 384;
    int playerSpeed = 4;


    public GamePanel() {
        setPreferredSize(new Dimension(screenWidth,screenHeight));
        setBackground(Color.black);
        setDoubleBuffered(true);
        setFocusable(true);
        addKeyListener(new KeyHandler(this));
        requestFocusInWindow();
    }

    public void setDir(int playerDir) {
        this.playerDir = playerDir;
    }

    public void isMoving(boolean isMoving){
        this.isMoving = isMoving;
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;

        g2.setColor(Color.white);

        g2.fillRect(playerX,playerY,tileSize,tileSize);

        g2.dispose();
    }

    public void updatePos() {
        if(isMoving) {
            switch (playerDir) {
                case UP -> playerY -= playerSpeed;
                case LEFT -> playerX -= playerSpeed;
                case DOWN -> playerY += playerSpeed;
                case RIGHT -> playerX += playerSpeed;
            }
        }
    }
}
