package Entity;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Entity {

    public int worldX, worldY;
    public double speed;


    public String direction;
    public String prevDirection;

    //Player Collision Box
    public Rectangle solidArea;
    public Rectangle attackArea = new Rectangle(0,0,0,0);
    public int solidAreaX, solidAreaY;
    public boolean collisionOn = false;

    //Character Status
    public double maxLife;
    public double life;
    public boolean alive;
    public boolean attacking;
    public int attackPower;


    public void draw(Graphics2D g2) {
        // Default: do nothing
    }

    public boolean collidesWith(Entity other) {
        Rectangle thisBox = new Rectangle(
                worldX + solidArea.x,
                worldY + solidArea.y,
                solidArea.width,
                solidArea.height
        );

        Rectangle otherBox = new Rectangle(
                other.worldX + other.solidArea.x,
                other.worldY + other.solidArea.y,
                other.solidArea.width,
                other.solidArea.height
        );

        return thisBox.intersects(otherBox);
    }


    protected abstract void takeDamage(int attackPower);
    protected abstract void die();
}
