package Main;

import Entity.Player;
import PlayerKeyHandler.KeyHandler;
import PlayerKeyHandler.MouseHandler;
import Tile.TileManager;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    final int originalTileSize = 16;
    final int scale = 3;
    final int tileSize = originalTileSize * scale;
    public final int screenWidth = 1280; // 1280 Default
    public final int screenHeight = 768; // 768 Default

    KeyHandler keyH = new KeyHandler();
    MouseHandler mouseH = new MouseHandler();
    public Player player = new Player(this,keyH, mouseH);
    TileManager tileManager = new TileManager(this);
    public CollisionChecker check = new CollisionChecker(this);

    //WORLD MAP SETTINGS
    public int maxWorldCol;
    public int maxWorldRow;


    public GamePanel() {
        setPreferredSize(new Dimension(screenWidth,screenHeight));
        setBackground(Color.black);
        setDoubleBuffered(true);
        setFocusable(true);
        addKeyListener(keyH);
        addMouseListener(mouseH);
        mouseH.setPlayer(player);
        requestFocusInWindow();
        this.maxWorldCol = tileManager.getMapWidth();
        this.maxWorldRow = tileManager.getMapHeight();
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;

//        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
//        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
//        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

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
