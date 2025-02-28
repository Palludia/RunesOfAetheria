package Main;

import Entity.Player;
import PlayerKeyHandler.KeyHandler;
import Tile.TileManager;

import javax.swing.*;
import java.awt.*;

import static Utilz.Constants.Directions.*;

public class GamePanel extends JPanel {
    final int originalTileSize = 16; // 32 x 32 tile
    final int scale = 1;
    final int tileSize = originalTileSize * scale; // 64 x 64 tile that would be displayed
    final int maxScreenCol = 80;
    final int maxScreenRow = 48;
    final int screenWidth = tileSize * maxScreenCol; // 1280
    final int screenHeight = tileSize * maxScreenRow; // 768

    KeyHandler keyHandler = new KeyHandler(this);
    Player player = new Player(this,keyHandler);
    TileManager tileManager = new TileManager(this);


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

        tileManager.draw(g2);

        player.draw(g2);

        g2.dispose();
    }

    public void updatePos() {
        player.updatePos();
    }

    public int getTileSize() {
        return tileSize;
    }

    public int getMaxScreenCol() {
        return maxScreenCol;
    }

    public int getMaxScreenRow() {
        return maxScreenRow;
    }
}
