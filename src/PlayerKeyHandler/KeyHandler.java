package PlayerKeyHandler;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

import Main.GamePanel;
import static Utilz.Constants.Directions.*;

public class KeyHandler implements KeyListener {
    private GamePanel gamePanel;
    public Set<Integer> keysHeld = new HashSet<Integer>(); // Tracks all currently held keys

    public KeyHandler(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keysHeld.add(e.getKeyCode());
        switch(e.getKeyCode()) {
            case KeyEvent.VK_W:
                gamePanel.setDir(UP);
                gamePanel.isMoving(true);
                break;
            case KeyEvent.VK_A:
                gamePanel.setDir(LEFT);
                gamePanel.isMoving(true);
                break;
            case KeyEvent.VK_S:
                gamePanel.setDir(DOWN);
                gamePanel.isMoving(true);
                break;
            case KeyEvent.VK_D:
                gamePanel.setDir(RIGHT);
                gamePanel.isMoving(true);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

        keysHeld.remove(e.getKeyCode());
        if(keysHeld.isEmpty()) {
            gamePanel.isMoving(false);
            return;
        }
        switch(e.getKeyCode()) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_A:
            case KeyEvent.VK_S:
            case KeyEvent.VK_D:
                if(keysHeld.contains(KeyEvent.VK_W)) {
                    gamePanel.setDir(UP);
                }else if(keysHeld.contains(KeyEvent.VK_A)) {
                    gamePanel.setDir(LEFT);
                }else if(keysHeld.contains(KeyEvent.VK_S)) {
                    gamePanel.setDir(DOWN);
                }else if(keysHeld.contains(KeyEvent.VK_D)) {
                    gamePanel.setDir(RIGHT);
                }
                break;
        }
    }

    private void updateMovement() {
        // Reset all movement flags
        boolean upPressed = keysHeld.contains(KeyEvent.VK_W);
        boolean downPressed = keysHeld.contains(KeyEvent.VK_S);
        boolean leftPressed = keysHeld.contains(KeyEvent.VK_A);
        boolean rightPressed = keysHeld.contains(KeyEvent.VK_D);

        // Notify the game panel about movement
        if (upPressed || downPressed || leftPressed || rightPressed) {
            gamePanel.isMoving(true);
        } else {
            gamePanel.isMoving(false);
        }

        if (upPressed) {
            gamePanel.setDir(UP);
        } else if (downPressed) {
            gamePanel.setDir(DOWN);
        } else if (leftPressed) {
            gamePanel.setDir(LEFT);
        } else if (rightPressed) {
            gamePanel.setDir(RIGHT);
        }
    }
}