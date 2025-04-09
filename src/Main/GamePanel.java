package Main;

import Entity.Orc;
import Entity.Player;
import PlayerKeyHandler.KeyHandler;
import PlayerKeyHandler.MouseHandler;
import Tile.TileManager;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GamePanel extends JPanel {
    final int originalTileSize = 16;
    final int scale = 3;
    final int tileSize = originalTileSize * scale;
    public final int screenWidth = 1280; // 1280 Default
    public final int screenHeight = 768; // 768 Default

    public Sound sound = new Sound();
    KeyHandler keyH = new KeyHandler(this);
    MouseHandler mouseH = new MouseHandler(this);

    public Player player = new Player(this,keyH, mouseH);
    public TileManager tileManager = new TileManager(this);
    public CollisionChecker check = new CollisionChecker(this);

    Menu menu;
    public boolean showDebugInfo = false;

    //WORLD MAP SETTINGS
    public int maxWorldCol;
    public int maxWorldRow;

    public final int TITLE_STATE = 0;
    public final int NEWGAME_STATE = 1;
    public final int PAUSE_STATE = 2;
    public final int GAMEOVER_STATE = 3;
    public final int ESCAPE_STATE = 4;
    public final int LOADGAME_STATE = 5;

    public int gameState;
    public int commandNum = 0;
    public int previousState;


    public boolean isMoving;
    public boolean newGameSelected = false;

    public ArrayList<Orc> orcs = new ArrayList<>();

    public GamePanel() {
        menu = new Menu(this);
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setBackground(Color.black);
        setDoubleBuffered(true);
        setFocusable(true);
        addKeyListener(keyH);
        addMouseListener(mouseH);
        mouseH.setPlayer(player);
        requestFocusInWindow();
        this.maxWorldCol = tileManager.getMapWidth();
        this.maxWorldRow = tileManager.getMapHeight();
        setGameState(TITLE_STATE);
        spawnOrcs(1);
        setMoving(true);
        previousState = -1;
    }

    public void spawnOrcs(int count) {
        for (int i = 0; i < count; i++) {
            orcs.add(new Orc(this));
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        if (gameState == TITLE_STATE) {
            menu.drawTitleScreen(g2);
        } else {
            menu.drawPlayScreen(g2);
            if(!player.alive && gameState != GAMEOVER_STATE) {
                setGameState(GAMEOVER_STATE);
            }
            if(gameState == GAMEOVER_STATE) {
                menu.drawGameOverScreen(g2);
            }
            if (gameState == PAUSE_STATE) {
                menu.drawPauseScreen(g2); // We'll add this method to the Menu class
            }
            if(gameState == ESCAPE_STATE) {
                menu.drawEscapeScreen(g2);
            }
        }
        g2.dispose();
    }

    public void updatePos() {
        player.update();
        if (gameState == PAUSE_STATE || gameState == ESCAPE_STATE) {
            return;
        }
        for (Orc orc : orcs) {
            if (orc != null) {
                orc.update();
            }
        }
    }

    public int getTileSize() {
        return tileSize;
    }

    public void playMusic(int state) {
        System.out.println("Requested music for state: " + state);

        switch (state) {
            case TITLE_STATE:
                // Stop any playing title music before (optional)
                sound.stopClip(0);
                // Loop the title screen music (clip at index 0)
                sound.loopClip(0);
                break;

            case NEWGAME_STATE:
                // Stop playing any previous music before switching
                sound.stopClip(1);
                // Loop the play state music (clip at index 1)
                sound.loopClip(1);
                break;

            case PAUSE_STATE:
                // Pause the play state music if needed
                sound.pauseClip(1);
                break;

            case GAMEOVER_STATE:
                // Stop all playing sounds for game over
                sound.stopAll();
                break;
            case ESCAPE_STATE:
                sound.pauseClip(1);
                break;
            case LOADGAME_STATE:
                // Stop playing any previous music before switching
                sound.stopClip(1);
                // Loop the play state music (clip at index 1)
                sound.loopClip(1);
                break;
        }
    }

    public void stopMusic() {
        sound.stopAll();
    }

    public void playSE(int i) {
        sound.playClip(i);
    }


    public void resetGame() {
        System.out.println("Resetting game...");

        sound.stopAll();
        player.setDefaultValues();

        for (Orc orc : orcs) {
            orc.reset();
        }

        // Delay to ensure audio cleanup (if necessary)
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void resetAndGoToState(int targetState) {
        resetGame();
        setGameState(targetState);
        if(targetState == TITLE_STATE) {
            sound.loopClip(0);
        }else if(targetState == NEWGAME_STATE || targetState == LOADGAME_STATE) {
            sound.loopClip(1);
        }

    }



    public void setMoving(boolean moving) {
        isMoving = moving;
    }

    public void setGameState(int gameState) {
        previousState = this.gameState;
        this.gameState = gameState;

        // Call playMusic to handle audio transitions
        playMusic(this.gameState);

        switch (gameState) {
            case TITLE_STATE:
                // If you want title music, you can add additional title-specific behavior here.
                break;
            case NEWGAME_STATE:
                // Additional behavior if needed for play state.
                break;
            case PAUSE_STATE:
                // Additional behavior if needed for pause.
                break;
            case GAMEOVER_STATE:
                // Additional behavior for game over can be added here.
                break;
        } // 🔍 You are missing this closing brace!
    } // ✅ Close method here

}
