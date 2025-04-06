package Entity;

import Main.GamePanel;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import java.awt.geom.Ellipse2D;

import static Utilz.Constants.Directions.*;

public class Orc extends Entity {
    GamePanel gp;
    Random rndDirection = new Random();
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
    public String diagonalMovement;

    // Animation control
    private int actionCooldown = 0; // Frames until next direction change

    //Monster Aggro Range
    public Ellipse2D.Double aggroRange;
    int radius = 200; // example radius
    int diameter = radius * 2;
    int centering = 22;


    public Orc(GamePanel gp) {
        this.gp = gp;
        aggroRange = new Ellipse2D.Double();

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
        int maxLife = 10;
        int life = 10;

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

        // Draw aggro circle (for debugging or visualization)
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        aggroRange.setFrame(((screenX - radius + (double)gp.getTileSize()/2) + (double)characterWidth/2)-centering, ((screenY - radius + (double)gp.getTileSize()/2) + (double)characterWidth/2.0)-centering, diameter, diameter);

        g2.setColor(new Color(255, 0, 0));
        g2.drawOval(((screenX - radius + gp.getTileSize()/2) + characterWidth/2)-centering, ((screenY - radius + gp.getTileSize()/2) + characterHeight/2)-centering, diameter, diameter);

//      g2.drawRect(drawX + solidAreaX, drawY + solidAreaY, solidArea.width , solidArea.height );

    }

    public void setAction() {
        actionCooldown++;
        if(actionCooldown == 120) {
            int i = rndDirection.nextInt(101), j;

            if(i <= 25) {
                direction = "up";
                j = rndDirection.nextInt(101);
                if(j <= 50){
                    diagonalMovement = "left";
                }else if(j > 50 && j <= 100){
                    diagonalMovement = "right";
                }
            }else if(i > 25 && i <= 50) {
                direction = "down";
                j = rndDirection.nextInt(101);
                if(j <= 50){
                    diagonalMovement = "left";
                }else if(j > 50 && j <= 100){
                    diagonalMovement = "right";
                }
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
            if(aggroRange.contains(gp.player.solidAreaX, gp.player.solidArea.y)){
                if(this.worldX < gp.player.worldX){
                    this.worldX += speed;
                }
                if(this.worldX > gp.player.worldX){
                    this.worldX -= speed;
                }
                if(this.worldY < gp.player.worldY){
                    this.worldY += speed;
                }
                if(this.worldY > gp.player.worldY){
                    this.worldY -= speed;
                }System.out.println("Orc : Player in range");
            }else{
                switch (direction) {
                    case "up" -> worldY -= speed;
                    case "down" -> worldY += speed;
                    case "left" -> worldX -= speed;
                    case "right" -> worldX += speed;
                }
            }

        }else {

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