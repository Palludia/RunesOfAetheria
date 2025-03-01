package Main;

import Entity.Player;
import PlayerKeyHandler.KeyHandler;
import Tile.TileManager;

import javax.swing.*;
import java.awt.*;

import static Utilz.Constants.Directions.*;

public class GamePanel extends JPanel {
    final int originalTileSize = 16;
    final int scale = 1;
    final int tileSize = originalTileSize * scale;
    public final int screenWidth = 1280; // 1280
    public final int screenHeight = 768; // 768

    KeyHandler keyHandler = new KeyHandler(this);
    public Player player = new Player(this,keyHandler);
    TileManager tileManager = new TileManager(this);

    //WORLD MAP SETTINGS
    public int maxWorldCol;
    public int maxWorldRow;
    public final int worldWidth = tileSize * maxWorldCol;
    public final int worldHeight = tileSize * maxWorldRow;


    public GamePanel() {
        setPreferredSize(new Dimension(screenWidth,screenHeight));
        setBackground(Color.black);
        setDoubleBuffered(true);
        setFocusable(true);
        addKeyListener(keyHandler);
        requestFocusInWindow();
        this.maxWorldCol = tileManager.getMapWidth();
        this.maxWorldRow = tileManager.getMapHeight();
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

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

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

}
