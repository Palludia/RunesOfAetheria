package Entity;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Entity {

    public int worldX, worldY;
    public int speed;

    public BufferedImage[] walkDown = new BufferedImage[6];
    public BufferedImage[] walkLeft = new BufferedImage[8];
    public BufferedImage[] walkRight = new BufferedImage[8];
    public BufferedImage[] walkUp = new BufferedImage[8];
    public BufferedImage[] idleDown = new BufferedImage[1];
    public BufferedImage[] idleUp = new BufferedImage[1];
    public BufferedImage[] idleLeft = new BufferedImage[1];
    public BufferedImage[] idleRight = new BufferedImage[1];
    public BufferedImage[] AttackFront = new BufferedImage[5];
    public BufferedImage[] AttackLeft = new BufferedImage[5];
    public BufferedImage[] AttackRight = new BufferedImage[5];
    public BufferedImage[] AttackUp = new BufferedImage[5];
    public String direction;
    public String prevDirection;

    //Player Collision Box
    public Rectangle solidArea;
    public int solidAreaX, solidAreaY;
    public boolean collisionOn = false;
}
