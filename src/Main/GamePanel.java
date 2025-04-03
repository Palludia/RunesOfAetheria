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

    Sound sound = new Sound();
    KeyHandler keyH = new KeyHandler(this);
    MouseHandler mouseH = new MouseHandler(this);
    public Player player = new Player(this,keyH, mouseH);
    TileManager tileManager = new TileManager(this);
    public CollisionChecker check = new CollisionChecker(this);
    Menu menu;

    //WORLD MAP SETTINGS
    public int maxWorldCol;
    public int maxWorldRow;

    public final int TITLE_STATE = 0;
    public final int PLAY_STATE = 1;
    public int gameState = TITLE_STATE;
    public int commandNum = 0;



    public GamePanel() {
        menu = new Menu(this);
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

        if(gameState == TITLE_STATE) {
            menu.drawTitleScreen(g2);
        }else if(gameState == PLAY_STATE) {
           menu.drawPlayScreen(g2);
        }

//        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
//        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
//        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.dispose();
    }

    public void updatePos() {
        player.updatePos();
    }

    public int getTileSize() {
        return tileSize;
    }

    public void playMusic(int i) {
        sound.setFile(i);
        sound.play();
        sound.loop();
    }

    public void stopMusic() {
        sound.stop();
    }

    public void playSE(int i) {
        sound.setFile(i);
        sound.play();
    }

    public void setGameState(int gameState) {
        this.gameState = gameState;
        stopMusic();
        if(this.gameState == 0) {
            playMusic(0);
        }else if(this.gameState == 1){
            playMusic(1);
        }
    }
}
