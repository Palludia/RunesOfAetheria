package Entity;

import Main.GamePanel;
import Objects.OBJ_HEART;
import PlayerKeyHandler.KeyHandler;
import PlayerKeyHandler.MouseHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Player extends Entity {
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

    //Aggro Range
    public Ellipse2D.Double aggroRange;
    int radius = 200;
    int diameter = radius * 2;

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

        //Aggro Settings
        double centerX = worldX + solidArea.x + solidArea.width / 2.0;
        double centerY = worldY + solidArea.y + solidArea.height / 2.0;
        aggroRange = new Ellipse2D.Double(
            centerX - radius,
            centerY - radius,
            diameter,
            diameter
        );

        setDefaultValues();
        getPlayerImage();
        OBJ_HEART heart = new OBJ_HEART(gamePanel);
        heart_full = heart.image1;
        heart_half = heart.image2;
        heart_blank = heart.image3;
    }

    public void setDefaultValues() {
        //Default player Spawn point
        worldX = gamePanel.getTileSize() * 400 - (gamePanel.getTileSize() / 2);
        worldY = gamePanel.getTileSize() * 730 - (gamePanel.getTileSize() / 2);

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

            gamePanel.check.checkForMonster(this);

            //Check for Monster in the vicinity
            for(Orc m : gamePanel.orcs){
                m.setAggro(aggroRange.contains(m.worldX + m.solidArea.x + m.solidArea.width / 2.0, m.worldY + m.solidArea.y + m.solidArea.height / 2.0));
            }


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
            updateAggroCircle();
            g2.drawImage(image[spriteIndex],screenX,screenY,characterWidth,characterHeight,null);
//            g2.setColor(Color.RED);
//            g2.drawRect(screenX + solidAreaX, screenY + solidAreaY, solidArea.width , solidArea.height );
            double screenX = aggroRange.getX() - gamePanel.player.worldX + gamePanel.player.screenX;
            double screenY = aggroRange.getY() - gamePanel.player.worldY + gamePanel.player.screenY;

            g2.setColor(new Color(255, 0, 0)); // translucent red
            g2.drawOval((int) screenX, (int) screenY,
                    (int) aggroRange.getWidth(), (int) aggroRange.getHeight());

    }
    public void updateAggroCircle() {
        double centerX = worldX + solidArea.x + solidArea.width / 2.0;
        double centerY = worldY + solidArea.y + solidArea.height / 2.0;

        // Set the ellipse to be centered around the player's center point
        aggroRange.setFrame(centerX - radius, centerY - radius, diameter, diameter);
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

    public void drawPlayerHP(Graphics2D g2) {
        try {
            File customFontFile = new File("res/fonts/medieval-font.ttf");
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, customFontFile);

            // Derive the font with the desired style and size
            Font derivedFont = customFont.deriveFont(Font.BOLD, 20F);

            // Apply the custom font
            g2.setFont(derivedFont);
        } catch (FontFormatException | IOException e) {
            System.out.println("Error loading custom font: " + e.getMessage());
            // If the custom font fails to load, use a default font
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 20F));
        }

        g2.setColor(Color.WHITE);
        g2.drawString(String.format("HP %d / %d",life,maxLife), screenX + 20, screenY);
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
