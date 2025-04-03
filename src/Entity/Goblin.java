package Entity;

import Main.GamePanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Goblin extends Entity_Enemy{
    GamePanel gp;
    BufferedImage[] idleAnimation = new BufferedImage[4];

    public Goblin(GamePanel gp){
        this.gp = gp;
        solidArea = new Rectangle(45,75,30,20);
        solidAreaX = solidArea.x;
        solidAreaY = solidArea.y;
    }

}
