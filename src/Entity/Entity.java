package Entity;

import java.awt.image.BufferedImage;

public class Entity {

    public int worldX, worldY;
    public int speed;

    public BufferedImage[] walkDown = new BufferedImage[6];
    public BufferedImage[] walkLeft = new BufferedImage[8];
    public BufferedImage[] walkRight = new BufferedImage[8];
    public BufferedImage[] walkUp = new BufferedImage[8];
    public BufferedImage[] idleDown = new BufferedImage[1];
    public String direction;



}
