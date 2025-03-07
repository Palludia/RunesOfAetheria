package PlayerKeyHandler;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

import Main.GamePanel;
import static Utilz.Constants.Directions.*;

public class KeyHandler implements KeyListener {
    private final GamePanel gamePanel;
    private final Set<Integer> keysHeld = new HashSet<>();

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
        updateMovement();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keysHeld.remove(e.getKeyCode());
        updateMovement();
    }

    private void updateMovement() {
        if (keysHeld.isEmpty()) {
            gamePanel.isMoving(false);
            return;
        }

        boolean up = keysHeld.contains(KeyEvent.VK_W);
        boolean left = keysHeld.contains(KeyEvent.VK_A);
        boolean down = keysHeld.contains(KeyEvent.VK_S);
        boolean right = keysHeld.contains(KeyEvent.VK_D);

        if (up && left) {
            gamePanel.setDir(UP_LEFT);
        } else if (up && right) {
            gamePanel.setDir(UP_RIGHT);
        } else if (down && left) {
            gamePanel.setDir(DOWN_LEFT);
        } else if (down && right) {
            gamePanel.setDir(DOWN_RIGHT);
        } else if (up) {
            gamePanel.setDir(UP);
        } else if (left) {
            gamePanel.setDir(LEFT);
        } else if (down) {
            gamePanel.setDir(DOWN);
        } else if (right) {
            gamePanel.setDir(RIGHT);
        }

        gamePanel.isMoving(true);
    }
}
