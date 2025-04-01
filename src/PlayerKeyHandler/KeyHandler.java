package PlayerKeyHandler;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

import Main.GamePanel;
import static Utilz.Constants.Directions.*;

public class KeyHandler implements KeyListener {
    public boolean shiftPressed, upPressed, downPressed, leftPressed, rightPressed;
    public boolean isMoving = true;


    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyPressed(KeyEvent e) {

        int code = e.getExtendedKeyCode();

        switch(code) {
            case KeyEvent.VK_W -> upPressed = true;
            case KeyEvent.VK_S -> downPressed = true;
            case KeyEvent.VK_A -> leftPressed = true;
            case KeyEvent.VK_D -> rightPressed = true;
            case KeyEvent.VK_SHIFT -> shiftPressed = true;
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

    }
}