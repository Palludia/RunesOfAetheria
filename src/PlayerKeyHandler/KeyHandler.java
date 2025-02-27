package PlayerKeyHandler;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

import Main.GamePanel;
import static Utilz.Constants.Directions.*;


public class KeyHandler implements KeyListener {
    private GamePanel gamePanel;
    private Set<Integer> keysHeld = new HashSet<Integer>();

    public KeyHandler(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }


    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        keysHeld.add(code);

        switch (code) {
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
        int code = e.getKeyCode();
        keysHeld.remove(code);
        if(keysHeld.isEmpty()) {
            gamePanel.isMoving(false);
            return;
        }
        if (keysHeld.contains(KeyEvent.VK_W)) {
            gamePanel.setDir(UP);
        } else if (keysHeld.contains(KeyEvent.VK_A)) {
            gamePanel.setDir(LEFT);
        } else if (keysHeld.contains(KeyEvent.VK_S)) {
            gamePanel.setDir(DOWN);
        } else if (keysHeld.contains(KeyEvent.VK_D)) {
            gamePanel.setDir(RIGHT);
        }
    }
}
