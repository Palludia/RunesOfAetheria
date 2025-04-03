package Entity;

import Main.GamePanel;
import Objects.OBJ_HEART;
import PlayerKeyHandler.KeyHandler;
import PlayerKeyHandler.MouseHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Player extends Entity {
    GamePanel gamePanel;
    KeyHandler keyH;
    MouseHandler mouseH;
    public int spriteIndex = 0;
    public int spriteCounter = 0;
    public final int characterWidth = 120;
    public final int characterHeight = 120;
    public final int screenX;
    public final int screenY;
    public BufferedImage[] image = idleDown;
    public BufferedImage heart_full, heart_blank, heart_half;

    public Player(GamePanel gamePanel, KeyHandler keyH, MouseHandler mouseH) {
        this.gamePanel = gamePanel;
        this.keyH = keyH;
        this.mouseH = mouseH;

        //Player Camera Center Settings
        screenX = gamePanel.screenWidth / 2 - (gamePanel.getTileSize() / 2);
        screenY = gamePanel.screenHeight / 2 - (gamePanel.getTileSize() / 2);

        //Player Solid Area for Collisions
        solidArea = new Rectangle(45, 75, 30, 20);

        solidAreaX = solidArea.x;
        solidAreaY = solidArea.y;

        setDefaultValues();
        getPlayerImage();
        OBJ_HEART heart = new OBJ_HEART(gamePanel);
        heart_full = heart.image1;
        heart_half = heart.image2;
        heart_blank = heart.image3;
    }

    public void setDefaultValues() {
        //Default player Spawn point
        worldX = gamePanel.getTileSize() * 399 - (gamePanel.getTileSize() / 2);
        worldY = gamePanel.getTileSize() * 728 - (gamePanel.getTileSize() / 2);

        speed = 4;
        direction = "idle";
        prevDirection = "down";

        // Player Status
        maxLife = 10;
        life = 5;
        gamePanel.playMusic(0);
    }

    public void updatePos() {
        if (keyH.upPressed || keyH.downPressed ||
                keyH.leftPressed || keyH.rightPressed) {

            if (keyH.upPressed) {
                direction = "up";
                prevDirection = "up";

            } else if (keyH.downPressed) {
                direction = "down";
                prevDirection = "down";

            } else if (keyH.leftPressed) {
                direction = "left";
                prevDirection = "left";

            } else {
                direction = "right";
                prevDirection = "right";

            }

            //CHECK FOR COLLISIONS
            collisionOn = false;
            gamePanel.check.checkCollision(this);

            //CHECK FOR OBJECTS
            //int objIndex = gamePanel.check.checkObject(this, true);
            //pickUpObject(objIndex);

            //IF COLLISION IS "ON" PLAYER CAN'T MOVE
            if (!collisionOn) {
                double diagonalSpeed = speed / Math.sqrt(2);
                switch (direction) {
                    case "up":
                    case "down":
                        worldY += keyH.upPressed ? -speed : speed;
                        //Implementation of Diagonal Movements When Going UP
                        if (keyH.rightPressed || keyH.leftPressed) {
                            direction = keyH.rightPressed ? "right" : "left";
                            prevDirection = keyH.rightPressed ? "right" : "left";
                            worldX += keyH.rightPressed ? Math.ceil(diagonalSpeed) : -diagonalSpeed;
                        }
                        break;
                    case "left":
                    case "right":
                        worldX += keyH.leftPressed ? -speed : speed;
                        break;
                }
            }
            //speed = keyH.shiftPressed ? isBoosted ? 4 : 3 : 2;
        } else {
            direction = "idle";
        }
    }

    public void draw(Graphics2D g2) {

        image = switch (direction) {
            case "up" -> walkUp;
            case "left" -> walkLeft;
            case "down" -> walkDown;
            case "right" -> walkRight;
            case "idle" -> switch(prevDirection){
                case "right" -> idleRight;
                case "left" -> idleLeft;
                case "up" -> idleUp;
                case "down" -> idleDown;
                default -> image;
            };
            default -> null;
        };

        if(mouseH.isAttacking) {
            image = switch (prevDirection) {
                case "left" -> AttackLeft;
                case "down" -> AttackFront;
                case "right" -> AttackRight;
                case "up" -> AttackUp;
                default -> idleDown;
            };
        }
            spriteCounter++;

            if(spriteCounter > 6) {
                spriteIndex++;
                spriteCounter = 0;
            }

            if(spriteIndex >= image.length){
                spriteIndex = 0;
            }
            g2.drawImage(image[spriteIndex],screenX,screenY,characterWidth,characterHeight,null);
//            g2.setColor(Color.RED);
//            g2.drawRect(screenX + solidAreaX, screenY + solidAreaY, solidArea.width , solidArea.height );

    }

    public void drawPlayerHeart(Graphics2D g2) {
        int x = gamePanel.getTileSize() / 2;
        int y = gamePanel.getTileSize() / 2;

        // 1. First draw all blank hearts (background)
        for (int i = 0; i < gamePanel.player.maxLife / 2; i++) {
            g2.drawImage(heart_blank, x, y, null);
            x += gamePanel.getTileSize();
        }

        // 2. Reset position for filled hearts
        x = gamePanel.getTileSize() / 2;

        // 3. Draw filled hearts (improved version)
        int fullHearts = gamePanel.player.life / 2;
        int halfHearts = gamePanel.player.life % 2;

        // Draw full hearts
        for (int i = 0; i < fullHearts; i++) {
            g2.drawImage(heart_full, x, y, null);
            x += gamePanel.getTileSize();
        }

        // Draw half heart if needed
        if (halfHearts > 0) {
            g2.drawImage(heart_half, x, y, null);
        }
    }

    public void getPlayerImage() {
        try {

            for (int i = 0; i < 6; i++) {
                walkDown[i] = ImageIO.read(getClass().getResourceAsStream("/player/Walk(Down)/" + (i + 1) + ".png"));
            }

            idleDown[0] = ImageIO.read(getClass().getResourceAsStream("/player/Walk(Down)/1.png"));
            idleUp[0] = ImageIO.read(getClass().getResourceAsStream("/player/Walk(Up)/1.png"));
            idleLeft[0] = ImageIO.read(getClass().getResourceAsStream("/player/Walk(Left)/1.png"));
            idleRight[0] = ImageIO.read(getClass().getResourceAsStream("/player/Walk(Right)/1.png"));

            for (int i = 0; i < 8; i++) {
                walkLeft[i] = ImageIO.read(getClass().getResourceAsStream("/player/Walk(Left)/" + (i + 1) + ".png"));
                walkUp[i] = ImageIO.read(getClass().getResourceAsStream("/player/Walk(Up)/" + (i + 1) + ".png"));
                walkRight[i] = ImageIO.read(getClass().getResourceAsStream("/player/Walk(Right)/" + (i + 1) + ".png"));
            }
            for (int i = 0; i < 5; i++) {
                AttackFront[i] = ImageIO.read(getClass().getResourceAsStream("/player/Attack(Down)/" + (i + 1) + ".png"));
                AttackLeft[i] = ImageIO.read(getClass().getResourceAsStream("/player/Attack(Left)/" + (i + 1) + ".png"));
                AttackRight[i] = ImageIO.read(getClass().getResourceAsStream("/player/Attack(Right)/" + (i + 1) + ".png"));
                AttackUp[i] = ImageIO.read(getClass().getResourceAsStream("/player/Attack(Up)/" + (i + 1) + ".png"));
            }

        } catch (IOException _) {

        }
    }

    public String getDirection(){
        return direction;
    }
}
