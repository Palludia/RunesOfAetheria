package PlayerKeyHandler;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.security.Key;
import java.util.HashSet;
import java.util.Set;

import Main.Game;
import Main.GamePanel;
import static Utilz.Constants.Directions.*;

public class KeyHandler implements KeyListener {
    public boolean shiftPressed, upPressed, downPressed, leftPressed, rightPressed;
    public boolean isMoving = true;
    GamePanel gp;

    public KeyHandler(GamePanel gp) {
        this.gp = gp;
    }


    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyPressed(KeyEvent e) {

        int code = e.getExtendedKeyCode();

        if(gp.gameState == gp.TITLE_STATE) {
            if(code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                gp.commandNum--;
                if(gp.commandNum < 0) {
                    gp.commandNum = 2;
                }
            }
            if(code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                gp.commandNum++;
                if(gp.commandNum > 2) {
                    gp.commandNum = 0;
                }
            }
            if(code == KeyEvent.VK_ENTER) {
                if(gp.commandNum == 0) {
                    gp.setGameState(1); // PLAY STATE
                }else if(gp.commandNum == 2) {
                    System.exit(0);
                }
            }
        }

        else if (gp.gameState == gp.PLAY_STATE) {
            // Movement keys
            if (code == KeyEvent.VK_W) { upPressed = true; }
            if (code == KeyEvent.VK_S) { downPressed = true; }
            if (code == KeyEvent.VK_A) { leftPressed = true; }
            if (code == KeyEvent.VK_D) { rightPressed = true; }

            // Pause Key
            if (code == KeyEvent.VK_P) {
                gp.setGameState(gp.PAUSE_STATE); // Switch to Pause State
            }
            // Debug Key Example
            if (code == KeyEvent.VK_T) {
                gp.showDebugInfo = !gp.showDebugInfo; // Toggle debug info
            }
        }
        // --- PAUSE STATE ---
        else if (gp.gameState == gp.PAUSE_STATE) {
            // Unpause Key
            if (code == KeyEvent.VK_P) {
                gp.setGameState(gp.PLAY_STATE); // Switch back to Play State
            }
            // Add other pause menu controls here if needed (e.g., Enter to select Quit)
        }


    }

    @Override
    public void keyReleased(KeyEvent e) {

        int code = e.getExtendedKeyCode();

        switch(code) {
            case KeyEvent.VK_W -> upPressed = false;
            case KeyEvent.VK_S -> downPressed = false;
            case KeyEvent.VK_A -> leftPressed = false;
            case KeyEvent.VK_D -> rightPressed = false;
            case KeyEvent.VK_SHIFT -> shiftPressed = false;
        }

        isMoving = false;
    }
}