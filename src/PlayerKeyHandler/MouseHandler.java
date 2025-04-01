package PlayerKeyHandler;

import Entity.Player;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MouseHandler implements MouseListener {
    public Player player;
    public boolean isAttacking = false;


    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(!isAttacking) {
            isAttacking = true;
            System.out.println("I AM ATTACKING");
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(isAttacking) {
            isAttacking = false;
            System.out.println("I AM NOT ATTACKING");
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    public void setPlayer(Player player){
        this.player = player;
    }
}
