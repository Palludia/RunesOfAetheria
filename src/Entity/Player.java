package Entity;

import Main.GamePanel;
import PlayerKeyHandler.KeyHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static Utilz.Constants.Directions.*;

public class Player extends Entity{
    GamePanel gamePanel;
    KeyHandler keyHandler;
    private int playerDir = -1;
    private boolean isMoving = true;
    public int spriteIndex = 0;
    public int spriteCounter = 0;
    final int characterWidth = 64;
    final int characterHeight = 64;
    public final int screenX;
    public final int screenY;

    public Player(GamePanel gamePanel, KeyHandler keyHandler) {
        this.gamePanel = gamePanel;
        this.keyHandler = keyHandler;

        screenX = gamePanel.screenWidth / 2 - (gamePanel.getTileSize()/2);
        screenY = gamePanel.screenHeight / 2 - (gamePanel.getTileSize()/2);

        setDefaultValues();
        getPlayerImage();
        direction = "down";
    }

    public void setDefaultValues() {
        worldX = gamePanel.getTileSize() * 78;
        worldY = gamePanel.getTileSize() * 42;
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
        if(isMoving) {
            switch(playerDir) {
                case LEFT :
                case RIGHT :
                        worldX += playerDir == LEFT ? -speed : speed;
                        direction = playerDir == LEFT ? "left" : "right";
                    break;
                case UP :
                case DOWN :
                        worldY += playerDir == UP ? -speed : speed;
                        direction = playerDir == UP ? "up" : "down";
                    break;
            }
        }
    }
    public void draw(Graphics2D g2) {
        BufferedImage[] image;

        switch (direction) {
            case "up" -> image = walkUp;
            case "left" -> image = walkLeft;
            case "down" -> image = walkDown;
            case "right" -> image = walkRight;
            default -> image = idleDown;
        }

        if(isMoving){
            spriteCounter++;

            if(spriteCounter > 6) {
                spriteIndex++;
                spriteCounter = 0;
            }

            if(spriteIndex >= image.length){
                spriteIndex = 0;
            }
            g2.drawImage(image[spriteIndex],screenX,screenY,characterWidth,characterHeight,null);
        }else{
            g2.drawImage(image[0],screenX,screenY,characterWidth,characterHeight,null);
        }
    }

    public void getPlayerImage() {
        try{

            for(int i = 0; i<6; i++) {
                walkDown[i] = ImageIO.read(getClass().getResourceAsStream("/player/Walk(Down)/"+ (i + 1) + ".png"));
            }

            walkLeft[0] = ImageIO.read(getClass().getResourceAsStream("/player/Walk(Left)/1(Idle).png"));
            walkUp[0] = ImageIO.read(getClass().getResourceAsStream("/player/Walk(Up)/1(Idle).png"));
            for(int i = 1; i<8; i++) {
                walkLeft[i] = ImageIO.read(getClass().getResourceAsStream("/player/Walk(Left)/"+ (i + 1) + ".png"));
                walkUp[i] = ImageIO.read(getClass().getResourceAsStream("/player/Walk(Up)/"+ (i + 1) + ".png"));
            }

            for(int i = 0; i<8; i++){
                walkRight[i] = ImageIO.read(getClass().getResourceAsStream("/player/Walk(Right)/"+ (i + 1) + ".png"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
