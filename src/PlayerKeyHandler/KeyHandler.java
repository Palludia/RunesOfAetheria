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
        int code = e.getKeyCode();
        keysHeld.add(code); // Add the pressed key to the set of held keys
        updateMovement(); // Update movement based on all currently held keys
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        keysHeld.remove(code); // Remove the released key from the set of held keys
        updateMovement(); // Update movement based on remaining held keys
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