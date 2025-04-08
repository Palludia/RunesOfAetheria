package Entity;

import Main.GamePanel;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
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
    private int characterWidth = 90;
    private int characterHeight = 90;
    public int spriteIndex = 0;
    public int spriteCounter = 0;
    int drawX, drawY;
    public boolean collision = true;
    public String diagonalMovement;
    public boolean aggro = false;
    private static final int ANIMATION_DELAY = 6;
    private Font orcHpFont;

    // Animation control
    private int actionCooldown = 0; // Frames until next direction change



    public Orc(GamePanel gp) {
        this.gp = gp;

        solidArea = new Rectangle(35, 65, 20, 10);
        solidAreaX = solidArea.x;
        solidAreaY = solidArea.y;

        loadFont();
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
        // Calculate screen position (remains the same)
        drawX = worldX - gp.player.worldX + gp.player.screenX;
        drawY = worldY - gp.player.worldY + gp.player.screenY;

        // Determine animation set based on direction
        currentAnimation = switch (direction) {
            case "up" -> walkUp;
            case "left" -> walkLeft;
            case "down" -> walkDown;
            case "right" -> walkRight;
            default -> walkDown; // Use a default like walkDown to avoid null
        };

        // ===vvv=== Animation Update Logic ===vvv===
        // Check if the game is *NOT* paused before updating animation frames
        if (gp.gameState != gp.PAUSE_STATE) { // <<<=== THE FIX
            spriteCounter++;
            if (spriteCounter > ANIMATION_DELAY) { // Use the constant
                spriteIndex++;
                spriteCounter = 0;
            }

            // Reset sprite index if it exceeds the length of the current animation
            // Added null check for safety
            if (currentAnimation != null && spriteIndex >= currentAnimation.length) {
                spriteIndex = 0;
            } else if (currentAnimation == null) {
                // If animation is null somehow, reset index and maybe log error
                spriteIndex = 0;
                // System.err.println("Orc animation is null for direction: " + direction);
            }
        }
        // ===^^^=== End of Animation Update Logic ===^^^===


        // --- Drawing the Orc ---
        // Make sure animation and index are valid before drawing
        if (currentAnimation != null && spriteIndex >= 0 && spriteIndex < currentAnimation.length) {
            g2.drawImage(currentAnimation[spriteIndex], drawX, drawY, characterWidth, characterHeight, null);
        } else {
            // Optional: Draw placeholder if animation invalid
            g2.setColor(Color.MAGENTA);
            g2.fillRect(drawX, drawY, characterWidth, characterHeight);
            if (spriteIndex < 0 || (currentAnimation != null && spriteIndex >= currentAnimation.length)) {
                spriteIndex = 0; // Attempt to reset index if invalid
            }
        }

        // --- Drawing Debug Hitbox ---
        g2.setColor(Color.RED);
        g2.drawRect(drawX + solidAreaX, drawY + solidAreaY, solidArea.width, solidArea.height);


        // --- Drawing HP Text ---
        // Font loading is now done in constructor/loadFont() method

        // Set the pre-loaded font
        if (orcHpFont != null) {
            g2.setFont(orcHpFont);
        } else {
            // Fallback just in case font loading failed silently
            g2.setFont(new Font("Arial", Font.BOLD, 16));
        }

        g2.setColor(Color.WHITE);
        String hpText = String.format("HP %d / %d", life, maxLife);
        // Adjust text position to be above the Orc, centered perhaps
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(hpText);
        int textX = drawX + (characterWidth / 2) - (textWidth / 2); // Center horizontally above Orc
        int textY = drawY - 5; // Position slightly above the Orc's drawing Y
        g2.drawString(hpText, textX, textY);
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

    private void loadFont() {
        try {
            File customFontFile = new File("res/fonts/medieval-font.ttf"); // Ensure this path is correct
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, customFontFile);
            // Derive the font with the desired style and size ONCE
            orcHpFont = customFont.deriveFont(Font.BOLD, 16F); // Adjusted size slightly
        } catch (FontFormatException | IOException e) {
            System.err.println("Error loading custom font for Orc HP: " + e.getMessage());
            // If the custom font fails to load, use a default font
            orcHpFont = new Font("Arial", Font.BOLD, 16); // Default fallback font
        }
    }

    // Inside Orc.java

    public void updatePos() {
        // Determine intended direction *before* checking collision if not aggro
        // If aggro, the direction will be determined during movement logic
        if(gp.gameState == gp.PLAY_STATE) {
            if (!aggro) {
                setAction(); // Let setAction handle timed direction changes for random movement
            }

            collisionOn = false;
            gp.check.checkCollision(this); // Check for tile collisions in the current direction
            gp.check.checkForPlayer(this); // Check for player collision in the current direction
            // Optional: Add gp.check.checkForMonster(this); if Orcs should block each other

            if (!collisionOn) {
                // --- Movement logic ---
                if (aggro && canSeePlayer()) {
                    // Improved Aggro Pathing (tries primary axis first, then secondary if blocked)
                    int targetX = gp.player.worldX;
                    int targetY = gp.player.worldY;
                    int dx = targetX - worldX;
                    int dy = targetY - worldY;
                    String primaryDir = direction; // Keep track of intended direction for animation
                    boolean moved = false;

                    // Determine preferred axes
                    if (Math.abs(dx) > Math.abs(dy)) { // Try horizontal movement first
                        primaryDir = (dx > 0) ? "right" : "left";
                        if (!willCollide(primaryDir)) {
                            worldX += Integer.signum(dx) * speed;
                            moved = true;
                        } else { // Horizontal blocked, try vertical
                            primaryDir = (dy > 0) ? "down" : "up";
                            // Need to check dy != 0 before trying vertical
                            if (dy != 0 && !willCollide(primaryDir)) {
                                worldY += Integer.signum(dy) * speed;
                                moved = true;
                            }
                        }
                    } else { // Try vertical movement first
                        primaryDir = (dy > 0) ? "down" : "up";
                        if (!willCollide(primaryDir)) {
                            worldY += Integer.signum(dy) * speed;
                            moved = true;
                        } else { // Vertical blocked, try horizontal
                            primaryDir = (dx > 0) ? "right" : "left";
                            // Need to check dx != 0 before trying horizontal
                            if (dx != 0 && !willCollide(primaryDir)) {
                                worldX += Integer.signum(dx) * speed;
                                moved = true;
                            }
                        }
                    }

                    // If moved, update the actual direction for animation purposes
                    if (moved) {
                        direction = primaryDir;
                    } else {
                        // If couldn't move either way, maybe pick a random direction?
                        // Or just stay put for this frame. For now, let's just ensure
                        // the animation direction reflects the last *attempted* primary direction.
                        direction = primaryDir;
                    }
                    System.out.println("Orc : Player in range, trying to move " + direction);


                } else {
                    // Original Random movement logic
                    switch (direction) {
                        case "up" -> worldY -= speed;
                        case "down" -> worldY += speed;
                        case "left" -> worldX -= speed;
                        case "right" -> worldX += speed;
                    }
                }
            } else {
                // COLLISION DETECTED!
                // Immediately pick a new random direction that's different from the current one.
                String previousDirection = direction;
                int attempts = 0; // Prevent infinite loop in rare cases
                do {
                    int i = rndDirection.nextInt(100); // 0-99

                    if (i < 25) {
                        direction = "up";
                    } else if (i < 50) {
                        direction = "down";
                    } else if (i < 75) {
                        direction = "left";
                    } else {
                        direction = "right";
                    }
                    attempts++;
                } while (direction.equals(previousDirection) && attempts < 10); // Try up to 10 times to get a different direction

                // Reset the cooldown so it doesn't immediately change direction again randomly
                actionCooldown = 0;

                // Do NOT attempt to move this frame after detecting collision.
                // The next update cycle will try the new direction.
            }
        }

    }


    // Helper method inside Orc class to check potential collision for a specific direction
// (Useful for aggro pathing)
    private boolean willCollide(String checkDirection) {
        Rectangle futureBox = new Rectangle(
                worldX + solidArea.x,
                worldY + solidArea.y,
                solidArea.width,
                solidArea.height
        );

        switch (checkDirection) {
            case "up":    futureBox.y -= speed; break;
            case "down":  futureBox.y += speed; break;
            case "left":  futureBox.x -= speed; break;
            case "right": futureBox.x += speed; break;
        }

        // Check against tiles
        for (Rectangle box : gp.tileManager.collisionBoxes) {
            if (futureBox.intersects(box)) {
                return true;
            }
        }

        // Check against player (if needed for pathing decision)
        Rectangle playerBox = new Rectangle(
                gp.player.worldX + gp.player.solidArea.x,
                gp.player.worldY + gp.player.solidArea.y,
                gp.player.solidArea.width,
                gp.player.solidArea.height
        );
        if (futureBox.intersects(playerBox)) {
            return true;
        }

        // Optional: Check against other Orcs here if they should block pathing
        // for(Entity otherOrc : gp.orcs) { ... check intersection ... }


        return false; // No collision detected for this direction
    }

    public void setAggro(boolean aggro){
        this.aggro = aggro;
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

    private boolean canSeePlayer() {
        int tileSize = gp.getTileSize();

        int orcCenterX = worldX + solidArea.x + solidArea.width / 2;
        int orcCenterY = worldY + solidArea.y + solidArea.height / 2;

        int playerCenterX = gp.player.worldX + gp.player.solidArea.x + gp.player.solidArea.width / 2;
        int playerCenterY = gp.player.worldY + gp.player.solidArea.y + gp.player.solidArea.height / 2;

        double dx = playerCenterX - orcCenterX;
        double dy = playerCenterY - orcCenterY;
        double distance = Math.hypot(dx, dy);

        dx /= distance;
        dy /= distance;

        double currentX = orcCenterX;
        double currentY = orcCenterY;

        for (int i = 0; i < distance; i++) {
            int col = (int) (currentX / tileSize);
            int row = (int) (currentY / tileSize);


            if (gp.check.isTileBlocked(col, row)) {
                return false;
            }

            currentX += dx;
            currentY += dy;
        }

        return true;
    }

}