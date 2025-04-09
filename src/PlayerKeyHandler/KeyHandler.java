package PlayerKeyHandler;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Main.GamePanel;

public class KeyHandler implements KeyListener {
    public boolean shiftPressed = false, upPressed, downPressed, leftPressed, rightPressed;
    public boolean pPressed;
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
                    gp.sound.stopClip(0);
                    gp.resetAndGoToState(1);
                    gp.setMoving(true);
                    gp.newGameSelected = true;
                }else if(gp.commandNum == 1 && gp.newGameSelected) {
                    gp.sound.stopClip(0);
                    gp.setGameState(gp.LOADGAME_STATE);
                    gp.setMoving(true);
                }
                else if(gp.commandNum == 2) {
                    System.exit(0);
                }
            }
        }

        else if (gp.gameState == gp.NEWGAME_STATE && gp.player.alive || gp.gameState == gp.LOADGAME_STATE) {
            // Movement keys
            if (code == KeyEvent.VK_W) { upPressed = true; }
            if (code == KeyEvent.VK_S) { downPressed = true; }
            if (code == KeyEvent.VK_A) { leftPressed = true; }
            if (code == KeyEvent.VK_D) { rightPressed = true; }
            if (code == KeyEvent.VK_SHIFT && !shiftPressed) {
                gp.player.speed *= 1.5;
                shiftPressed = true;
            }

            // Pause Key
            if (code == KeyEvent.VK_P) {
                gp.setMoving(false);
                gp.setGameState(gp.PAUSE_STATE);
            }

            if(code == KeyEvent.VK_ESCAPE) {
                gp.setMoving(false);
                gp.setGameState(gp.ESCAPE_STATE);
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
                gp.setGameState(gp.LOADGAME_STATE); // Switch back to Play State
                gp.sound.resumeClip(1);
                gp.setMoving(true);
            }
            // Add other pause menu controls here if needed (e.g., Enter to select Quit)
        }

        else if(gp.gameState == gp.GAMEOVER_STATE) {
            if(code == KeyEvent.VK_ENTER) {
                gp.playMusic(0);
                gp.resetAndGoToState(0);
                gp.newGameSelected = false;
            }
        }

        else if(gp.gameState == gp.ESCAPE_STATE) {
            if(code == KeyEvent.VK_Y) {
                gp.sound.stopAll();
                gp.setGameState(gp.TITLE_STATE);
                gp.sound.loopClip(0);
            }else if(code == KeyEvent.VK_N) {
                gp.setGameState(gp.LOADGAME_STATE);
                gp.sound.resumeClip(1);
                gp.setMoving(true);
            }
        }


    }

    @Override
    public void keyReleased(KeyEvent e) {

        int code = e.getExtendedKeyCode();

        if (code == KeyEvent.VK_W) { upPressed = false; }
        if (code == KeyEvent.VK_S) { downPressed = false; }
        if (code == KeyEvent.VK_A) { leftPressed = false; }
        if (code == KeyEvent.VK_D) { rightPressed = false; }
        if (code == KeyEvent.VK_SHIFT) {
            gp.player.speed = 4;
            shiftPressed = false;
        }

    }
}