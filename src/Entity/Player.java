package Entity;

import Main.GamePanel;
import PlayerKeyHandler.KeyHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static Utilz.Constants.Directions.*;

public class Player extends Entity{
    GamePanel gamePanel;
    KeyHandler keyHandler;
    private int playerDir = -1;
    private boolean isMoving = false;
    public int spriteIndex = 0;
    public int spriteCounter = 20;
    final int characterWidth = 64;
    final int characterHeight = 64;

    public Player(GamePanel gamePanel, KeyHandler keyHandler) {
        this.gamePanel = gamePanel;
        this.keyHandler = keyHandler;

        setDefaultValues();
        getPlayerImage();
        direction = "down";
    }

    public void setDefaultValues() {
        xPos = 640;
        yPos = 384;
        speed = 4;
        direction = "down";
    }

    public void setPlayerDir(int playerDir) {
        this.playerDir = playerDir;
    }

    public void isMoving(boolean isMoving) {
        this.isMoving = isMoving;
    }

    public void updatePos() {
        // Check if any movement keys are held
        if (!keyHandler.keysHeld.isEmpty()) {
            if (keyHandler.keysHeld.contains(KeyEvent.VK_W)) { // Up
                yPos -= speed;
                direction = "up";
            }
            if (keyHandler.keysHeld.contains(KeyEvent.VK_A)) { // Left
                xPos -= speed;
                direction = "left";
            }
            if (keyHandler.keysHeld.contains(KeyEvent.VK_S)) { // Down
                yPos += speed;
                direction = "down";
            }
            if (keyHandler.keysHeld.contains(KeyEvent.VK_D)) { // Right
                xPos += speed;
                direction = "right";
            }
        }
    }
    public void draw(Graphics2D g2) {
        BufferedImage[] image = null;

        switch (direction) {
            case "up" -> image = walkUp;
            case "down" -> image = walkDown;
            case "left" -> image = walkLeft;
            case "right" -> image = walkRight;
            default -> image = walkDown;
        }

        if(image.length == walkDown.length) {
            if(isMoving){
                spriteCounter++;

                if(spriteCounter > 20) {
                    spriteIndex++;
                    spriteCounter = 0;
                }

                if(spriteIndex >= image.length){
                    spriteIndex = 0;
                }
                g2.drawImage(image[spriteIndex],xPos,yPos,characterWidth,characterHeight,null);
            }else{
                g2.drawImage(image[0],xPos,yPos,characterWidth,characterHeight,null);
            }
        }else if(image.length == 8) {
            if(isMoving) {
                spriteCounter++;

                if(spriteCounter > 20) {
                    spriteIndex++;
                    spriteCounter = 0;
                }

                if(spriteIndex >= image.length){
                    spriteIndex = 0;
                }
                g2.drawImage(image[spriteIndex],xPos,yPos,characterWidth,characterHeight,null);
            }else {
                g2.drawImage(image[0],xPos,yPos,characterWidth,characterHeight,null);
            }
        }
    }

    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }

    public void getPlayerImage() {
        try{

            for(int i = 0; i<6; i++) {
                walkDown[i] = ImageIO.read(getClass().getResourceAsStream("/player/Walk(Down)/"+ (i + 1) + ".png"));

                if(walkDown[i] == null) {
                    System.out.println("IMAGE IS NOT FOUND");
                }

            }

            walkLeft[0] = ImageIO.read(getClass().getResourceAsStream("/player/Walk(Left)/1(Idle).png"));
            walkUp[0] = ImageIO.read(getClass().getResourceAsStream("/player/Walk(Up)/1(Idle).png"));
            for(int i = 1; i<8; i++) {
                walkLeft[i] = ImageIO.read(getClass().getResourceAsStream("/player/Walk(Left)/"+ (i + 1) + ".png"));
                walkUp[i] = ImageIO.read(getClass().getResourceAsStream("/player/Walk(Up)/"+ (i + 1) + ".png"));

                if(walkLeft[i] == null || walkUp[i] == null) {
                    System.out.println("IMAGE NOT FOUND");
                }
            }

            for(int i = 0; i<8; i++){
                walkRight[i] = ImageIO.read(getClass().getResourceAsStream("/player/Walk(Right)/"+ (i + 1) + ".png"));

                if(walkRight[i] == null) {
                    System.out.println("IMAGE NOT FOUND");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
