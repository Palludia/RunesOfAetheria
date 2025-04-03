package Entity;

import Main.Game;
import Main.GamePanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Entity_Enemy {

    public GamePanel gp;
    public int worldX, worldY;
    public int speed;
    public String direction;
    public String prevDirection;
    public Rectangle solidArea;
    public int solidAreaX, solidAreaY;
    public boolean collisionOn = false;

}
