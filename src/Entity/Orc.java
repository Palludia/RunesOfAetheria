package Entity;

import Main.GamePanel;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import static Utilz.Constants.Directions.*;

public class Orc extends Entity {
    GamePanel gp;
    BufferedImage[] walkDown = new BufferedImage[6];
    BufferedImage[] walkUp = new BufferedImage[6];
    BufferedImage[] walkLeft = new BufferedImage[6];
    BufferedImage[] walkRight = new BufferedImage[6];
    BufferedImage[] currentAnimation = walkDown;
    private int characterWidth = 120;
    private int characterHeight = 120;
    public int spriteIndex = 0;
    public int spriteCounter = 0;
    int drawX, drawY;
    public boolean collision = true;

    // Animation control
    private     int actionCooldown = 0; // Frames until next direction change


    private int maxLife;
    private int life;

    public Orc(GamePanel gp) {
        this.gp = gp;
        solidArea = new Rectangle(45, 75, 30, 20);
        solidAreaX = solidArea.x;
        solidAreaY = solidArea.y;
        setDefaultValues();
        getOrcImage();
    }

    public void setDefaultValues() {
        // Default Orc Spawn point
        worldX = gp.getTileSize() * 401 - (gp.getTileSize() / 2);
        worldY = gp.getTileSize() * 738 - (gp.getTileSize() / 2);

        speed = 2; // Slightly slower speed for more control
        direction = "down"; // Start with a default direction
        prevDirection = "down";

        // Orc Status
        maxLife = 10;
        life = 10;

    }

    public void draw(Graphics2D g2) {
        // Calculate screen position dynamically relative to player
        drawX = worldX - gp.player.worldX + gp.player.screenX;
        drawY = worldY - gp.player.worldY + gp.player.screenY;

        currentAnimation = switch (direction) {
            case "up" -> walkUp;
            case "left" -> walkLeft;
            case "down" -> walkDown;
            case "right" -> walkRight;
            default -> null;
        };

        spriteCounter++;

        if(spriteCounter > 6) {
            spriteIndex++;
            spriteCounter = 0;
        }

        if(spriteIndex >= currentAnimation.length){
            spriteIndex = 0;
        }
        // Debug hitbox (optional)
        g2.drawImage(currentAnimation[spriteIndex],drawX,drawY,characterWidth,characterHeight,null);
        g2.setColor(Color.RED);
        g2.drawRect(drawX + solidAreaX, drawY + solidAreaY, solidArea.width , solidArea.height );

    }

    public void setAction() {
        actionCooldown++;
        if(actionCooldown == 120) {
            Random random = new Random();
            int i = random.nextInt(100) + 1;
            if(i <= 25) {
                direction = "up";
            }else if(i > 25 && i <= 50) {
                direction = "down";
            }else if(i > 50 && i <= 75) {
                direction = "left";
            }else if(i > 75 && i <= 100) {
                direction = "right";
            }
            actionCooldown = 0;
        }
    }

    public void updatePos() {
        setAction();
        collisionOn = false;
        gp.check.checkCollision(this);
        gp.check.checkForPlayer(this);

        if(!collisionOn) {
            switch (direction) {
                case "up" -> worldY -= speed;
                case "down" -> worldY += speed;
                case "left" -> worldX -= speed;
                case "right" -> worldX += speed;
            }
            setAction();
        }
    }

    public void getOrcImage() {
        try {
            for(int i = 0; i < 6; i++) {
                walkDown[i] = ImageIO.read(getClass().getResourceAsStream("/Entities/Enemies/Orcs/Walk(Down)/" + (i + 1) + ".png"));
                walkLeft[i] = ImageIO.read(getClass().getResourceAsStream("/Entities/Enemies/Orcs/Walk(Left)/" + (i + 1) + ".png"));
                walkRight[i] = ImageIO.read(getClass().getResourceAsStream("/Entities/Enemies/Orcs/Walk(Right)/" + (i + 1) + ".png"));
                walkUp[i] = ImageIO.read(getClass().getResourceAsStream("/Entities/Enemies/Orcs/Walk(Up)/" + (i + 1) + ".png"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}