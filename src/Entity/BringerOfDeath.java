package Entity;

import Main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BringerOfDeath extends Entity{
    // --- References & Utilities ---
    GamePanel gp;
    Random rndDirection = new Random();

    public BufferedImage[] walkLeft = new BufferedImage[8];
    public BufferedImage[] walkRight = new BufferedImage[8];
    public BufferedImage[] attackLeft = new BufferedImage[10];
    public BufferedImage[] attackRight = new BufferedImage[10];

    private int characterWidth = 280 * 3;  // Drawing width
    private int characterHeight = 85 * 3; // Drawing height
    public int spriteIndex = 0;        // Current frame index for walk/idle
    public int spriteCounter = 0;      // Timer for walk/idle animation speed
    private int attackSpriteIndex = 0;    // Current frame index for attack
    private int attackSpriteCounter = 0;  // Timer for attack animation speed

    private static final int WALK_ANIMATION_DELAY = 8; // Frames between walk/idle frame changes
    private static final int ATTACK_ANIMATION_DELAY = 6; // Frames between attack frame changes
    private Font BODHpFont;            // Pre-loaded font for HP display


    // --- State & AI ---
    public boolean collision = true; // Causes movement collision?
    public boolean aggro = false;    // Is aggressive towards player?
    public boolean attacking = false; // Is currently in attack animation?

    private int actionCooldown = 0; // Timer for random movement direction changes
    private final int ACTION_INTERVAL = 120; // Frames between random direction changes

    // --- Combat ---
    public int attackRange; // Distance to trigger attack (pixels)
    private int attackCooldown = 0;       // Timer after an attack before next one
    private final int ATTACK_COOLDOWN_TIME = 90; // Frames between attacks (~1.5s @ 60fps)
    private String attackDirection = "left"; // Direction Bringer faces *during* an attack
    private boolean hitPlayerThisSwing = false; // Ensure only one hit per attack animation

    /**
     * Constructor for the Bringer of Death entity.
     *
     * @param gp The main GamePanel instance.
     */
    public BringerOfDeath(GamePanel gp) {
        this.gp = gp;

        // Define the collision area relative to the entity's top-left corner (0,0)
        solidArea = new Rectangle(characterWidth/3 +15, characterHeight -20, 65, 30); // Adjust x, y, width, height
        solidAreaX = solidArea.x; // Store the default relative X offset
        solidAreaY = solidArea.y; // Store the default relative Y offset

        // Define attack area size (position is calculated dynamically)
        attackArea = new Rectangle(0, 0, 40, 40); // Example size, adjust as needed

        loadFont();         // Load custom font for HP display
        setDefaultValues(); // Set initial position, speed, health, etc.
        getBODImage();      // Load animation sprites
    }

    public void setDefaultValues() {
        worldX = gp.getTileSize() * 180 - (gp.getTileSize() / 2);
        worldY = gp.getTileSize() * 104 - (gp.getTileSize() / 2);

        speed = 2;           // Movement speed
        direction = "left";  // Initial facing direction

        // Status
        maxLife = 1500;      // Maximum health points
        life = maxLife;      // Start with full health
        alive = true;        // Start alive
        collision = true;    // Start as a solid entity that blocks movement
        aggro = false;       // Start non-aggressive
        attacking = false;   // Start not attacking
        attackPower = 164;   // Attack Power

        // Reset timers and animation state
        actionCooldown = 0;
        attackCooldown = 0;
        attackRange = gp.getTileSize() * characterWidth /2;
        spriteIndex = 0;
        spriteCounter = 0;
        attackSpriteIndex = 0;
        attackSpriteCounter = 0;
    }

    public void update() {
        // Ignore updates if game not playing or entity is dead
        if (gp.gameState != gp.NEWGAME_STATE && gp.gameState != gp.LOADGAME_STATE || !alive) {
            return;
        }

        // --- 1. Update Timers ---
        if (attackCooldown > 0) {
            attackCooldown--; // Decrement attack cooldown timer
        }
        if (!aggro) { // Only tick random action timer if not aggro
            actionCooldown++;
        }

        // --- 2. Handle Active Attack Sequence ---
        // If currently attacking, manage the attack animation and hit checks.
        // Prevent other actions during the attack.
        if (attacking) {
            handleAttackSequence(); // Manage animation and hit detection
            // Don't perform regular AI movement/collision checks while attacking
            return; // Exit update early
        }

        // --- 3. AI: Decide Intended Direction & Check Attack Trigger ---
        // If not currently attacking, decide what to do next.
        if (!aggro) {
            // Non-Aggro: Use setAction to potentially change direction randomly.
            setAction();
        } else {
            // Aggro: Face player, check if should attack or move closer.
            int dx = gp.player.worldX - worldX;

            // Determine direction based only on X-axis (left or right)
            direction = (dx > 0) ? "right" : "left";

            double distance = Math.abs(dx); // Using horizontal distance only

            // Condition to start an attack:
            boolean canAttack = distance < attackRange        // Player in range?
                    && attackCooldown <= 0        // Attack ready?
                    && !attacking                 // Not already attacking?
                    && canSeePlayer();            // Can see player?

            if (canAttack) {
                startAttack(); // Initiate the attack sequence
                // After starting attack, skip movement for this frame
                return; // Exit update early
            }
        }

        // --- 4. Collision Check for Movement ---
        // Check if the intended direction (from AI) is blocked by tiles or player.
        collisionOn = false; // Reset collision flag before checking
        gp.check.checkCollision(this);   // Check against solid tiles
        gp.check.checkForPlayer(this);

        // --- 5. Movement & Improved Collision Response ---
        // Execute movement only if the path was clear initially.
        // If blocked, try to move in opposite direction.
        if (!collisionOn) {
            // Path is clear: Move normally in the intended direction.
            moveNormally();
        } else {
            // COLLISION DETECTED: Try to navigate around the obstacle.
            handleMovementCollision();
        }

        // --- 6. Animation Update ---
        // Update walk/idle animation frame based on final direction and timer.
        // Attack animation is handled separately in handleAttackSequence.
        updateWalkIdleAnimation();
    }

    private void startAttack() {
        // Double-check conditions just in case
        if (!alive || attacking || attackCooldown > 0) {
            return;
        }

        attacking = true;           // Set the attacking state flag
        attackSpriteIndex = 0;      // Start animation from frame 0
        attackSpriteCounter = 0;    // Reset animation timer
        attackCooldown = ATTACK_COOLDOWN_TIME; // Start the cooldown for the *next* attack
        hitPlayerThisSwing = false; // Reset hit flag for this specific attack swing

        // Determine attack direction - Face the player at the moment attack starts
        int dx = gp.player.worldX - worldX;
        attackDirection = (dx > 0) ? "left" : "right";

        // Make the entity visually face the direction they are attacking
        direction = attackDirection;

        gp.playSE(4); // Play attack sound effect
    }

    private void handleAttackSequence() {
        // Update attack animation timer
        attackSpriteCounter++;
        if (attackSpriteCounter > ATTACK_ANIMATION_DELAY) {

            attackSpriteIndex++; // Advance to the next frame
            attackSpriteCounter = 0; // Reset timer

            // --- Hit Detection during specific frames ---
            // Adjust these frame indices based on your attack animation sprites
            int hitFrameStart = 2; // Example: Hitbox becomes active on frame index 2
            int hitFrameEnd = 4;   // Example: Hitbox stays active until frame index 4
            if (attackSpriteIndex >= hitFrameStart && attackSpriteIndex <= hitFrameEnd) {
                performAttackHitCheck(); // Check if the attack hits the player
            }

            // --- Determine which attack animation array to use ---
            BufferedImage[] currentAttackAnim = "left".equals(attackDirection) ? attackLeft : attackRight;

            // --- Check if Animation Has Finished ---
            if (currentAttackAnim != null && attackSpriteIndex >= currentAttackAnim.length) {
                // Animation cycle is complete
                attacking = false;      // End the attacking state
                attackSpriteIndex = 0;  // Reset index for the next attack
            }
        }
        // Also update the general animation counters to keep them ticking (for draw method)
        // This ensures the correct index is available for drawing even if logic is here
        spriteCounter = attackSpriteCounter;
        spriteIndex = attackSpriteIndex;
    }

    private void performAttackHitCheck() {
        // Only allow one successful hit per attack swing/animation cycle
        if (hitPlayerThisSwing || gp.player == null) {
            return;
        }

        // Create a temporary rectangle for the attack area check
        Rectangle attackArea = new Rectangle();
        // Use the size defined in the entity's fields (set in constructor)
        attackArea.width = this.attackArea.width;
        attackArea.height = this.attackArea.height;

        // Calculate the offset of the attack area relative to the entity's top-left (0,0)
        // based on the direction the attack was initiated in (attackDirection)
        int attackOffsetX = 0;
        int attackOffsetY = 0;

        // Only handle left/right directions
        if ("left".equals(attackDirection)) {
            // Center vertically, left of solidArea
            attackOffsetX = solidArea.x - attackArea.width;
            attackOffsetY = solidArea.y + (solidArea.height / 2) - (attackArea.height / 2);
        } else if("right".equals(attackDirection)){ // right
            // Center vertically, right of solidArea
            attackOffsetX = solidArea.x + solidArea.width;
            attackOffsetY = solidArea.y + (solidArea.height / 2) - (attackArea.height / 2);
        }

        // Set the world coordinates for the calculated attack area
        attackArea.x = worldX + attackOffsetX;
        attackArea.y = worldY + attackOffsetY;

        // Get the Player's solidArea in world coordinates for intersection check
        Rectangle playerWorldSolidArea = new Rectangle(
                gp.player.worldX + gp.player.solidArea.x,
                gp.player.worldY + gp.player.solidArea.y,
                gp.player.solidArea.width,
                gp.player.solidArea.height
        );

        // --- Check for Intersection ---
        if (attackArea.intersects(playerWorldSolidArea)) {
            // Apply damage to player
            gp.player.takeDamage(attackPower); // Use the defined attackPower

            // Mark that a hit occurred this swing to prevent multiple damage instances
            hitPlayerThisSwing = true;
        }
    }

    private void moveNormally() {
        // Only move horizontally based on direction
        if ("left".equals(direction)) {
            worldX -= speed;
        } else if ("right".equals(direction)) {
            worldX += speed;
        }
    }

    private void handleMovementCollision() {
        // For BringerOfDeath that only moves left/right, simply reverse direction
        direction = "left".equals(direction) ? "right" : "left";
        actionCooldown = 0; // Reset random movement timer
        // Do not move this frame
    }

    /**
     * Sets a random direction at intervals defined by ACTION_INTERVAL.
     * Only active when not aggressive (`aggro == false`).
     */
    public void setAction() {
        // Check if it's time to change direction
        if (actionCooldown >= ACTION_INTERVAL) {
            int i = rndDirection.nextInt(2); // Random number 0 or 1
            direction = (i == 0) ? "left" : "right"; // 0 = left, 1 = right
            actionCooldown = 0; // Reset timer
        }
    }

    private void updateWalkIdleAnimation() {
        // Only update walk/idle animation if NOT attacking
        if (attacking) {
            return;
        }

        spriteCounter++;
        if (spriteCounter > WALK_ANIMATION_DELAY) { // Use appropriate delay
            spriteIndex++;
            spriteCounter = 0; // Reset counter

            // Determine the current walk/idle animation array based on direction
            BufferedImage[] anim = "left".equals(direction) ? walkLeft : walkRight;

            // Check for null and loop the index
            if (anim != null && anim.length > 0) {
                if (spriteIndex >= anim.length) {
                    spriteIndex = 0; // Loop back to the first frame
                }
            } else {
                spriteIndex = 0; // Reset if animation array is invalid
            }
        }
    }

    public void draw(Graphics2D g2) {
        // Don't draw if dead (add check for death animation later if needed)
        if (!alive) {
            return;
        }

        // Calculate entity's position on the screen relative to the player
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        int visibleLeft = gp.player.worldX - gp.player.screenX;
        int visibleRight = visibleLeft + gp.screenWidth;  // Fixed using screen width
        int visibleTop = gp.player.worldY - gp.player.screenY;
        int visibleBottom = visibleTop + gp.screenHeight;  // Fixed using screen height

        // Add draw buffer for large entities
        int drawBuffer = 500;  // Increased buffer for huge bosses

        boolean isVisible =
                (worldX + characterWidth + drawBuffer > visibleLeft) &&
                        (worldX - drawBuffer < visibleRight) &&
                        (worldY + characterHeight + drawBuffer > visibleTop) &&
                        (worldY - drawBuffer < visibleBottom);

        // Check if entity is visible on screen
        if (isVisible)
        {
            // --- Select Correct Animation Set and Index ---
            BufferedImage[] currentAnimationSet;
            int currentSpriteIndex;

            if (attacking) {
                // Use attack animation set based on the stored attackDirection
                currentAnimationSet = "left".equals(attackDirection) ? attackLeft : attackRight;
                // Use the dedicated attack sprite index
                currentSpriteIndex = attackSpriteIndex;
            } else {
                // Use walk/idle animation set based on the current facing direction
                currentAnimationSet = "left".equals(direction) ? walkLeft : walkRight;
                // Use the walk/idle sprite index
                currentSpriteIndex = spriteIndex;
            }

            // --- Drawing the Sprite ---
            BufferedImage imageToDraw = null;
            // Ensure animation array and index are valid before accessing image
            if (currentAnimationSet != null && currentAnimationSet.length > 0 &&
                    currentSpriteIndex >= 0 && currentSpriteIndex < currentAnimationSet.length) {
                imageToDraw = currentAnimationSet[currentSpriteIndex];
            }

            if (imageToDraw != null) {
                // Draw the determined sprite frame at the screen position
                g2.drawImage(imageToDraw, screenX, screenY, characterWidth, characterHeight, null);
            } else {
                // Draw a magenta placeholder box if image is missing or animation invalid
                g2.setColor(Color.MAGENTA);
                g2.fillRect(screenX, screenY, characterWidth, characterHeight);
                g2.setColor(Color.BLACK);
                g2.drawString("BOD?", screenX + 10, screenY + 20);
                // Attempt to reset the relevant index if it was invalid
                if (attacking) {
                    if (currentSpriteIndex < 0 || (currentAnimationSet != null && currentSpriteIndex >= currentAnimationSet.length)) {
                        attackSpriteIndex = 0;
                    }
                } else {
                    if (currentSpriteIndex < 0 || (currentAnimationSet != null && currentSpriteIndex >= currentAnimationSet.length)) {
                        spriteIndex = 0;
                    }
                }
            }

            // --- Drawing HP Bar ---
            g2.setColor(Color.RED);
            g2.drawRect(screenX + solidAreaX, screenY + solidAreaY, solidArea.width , solidArea.height );
            drawHPBar(g2, screenX, screenY);
        }
    }

    /**
     * Helper method to draw the HP bar above its head.
     * @param g2 Graphics context.
     * @param screenX Screen X position.
     * @param screenY Screen Y position.
     */
    private void drawHPBar(Graphics2D g2, int screenX, int screenY) {
        // Set font (use preloaded or fallback)
        if (BODHpFont != null) {
            g2.setFont(BODHpFont);
        } else {
            g2.setFont(new Font("Arial", Font.BOLD, 16)); // Fallback
        }

        // Calculate HP bar dimensions and position
        // Ensure division by zero doesn't happen if maxLife is 0 (shouldn't be)
        double hpScale = (maxLife > 0) ? ((double) life / maxLife) : 0;
        // Prevent negative width if life somehow exceeds maxLife (or during damage visual?)
        hpScale = Math.max(0, Math.min(1, hpScale));
        int hpBarFullWidth = characterWidth; // Bar width matches character width
        int hpBarCurrentWidth = (int) (hpBarFullWidth * hpScale) ;
        int hpBarHeight = 5; // Height of the HP bar
        // Position above entity's head
        int hpBarY = screenY - hpBarHeight - 2; // Add small gap
        int hpBarX = screenX;

        // Draw the background (empty part) of the HP bar
        g2.setColor(Color.DARK_GRAY); // Or black
        g2.fillRect(hpBarX, hpBarY, hpBarFullWidth, hpBarHeight);
        // Draw the foreground (current health) of the HP bar
        g2.setColor(Color.RED); // Or green
        g2.fillRect(hpBarX, hpBarY, hpBarCurrentWidth, hpBarHeight);
    }

    public void getBODImage() {
        try {
            // Define the base path for images within your resources folder
            String basePath = "/Entities/Enemies/BringerOfDeath/Sprites/"; // Make sure this path is correct!

            // Load Walk Animations (8 frames)
            for (int i = 0; i < 8; i++) {
                walkLeft[i] = loadImage(basePath + "Walk/Left/" + "Left_" + (i+1) + ".png");
                walkRight[i] = loadImage(basePath + "Walk/Right/" + "Right_" + (i+1) + ".png");
            }

            // Load Attack Animations (10 frames)
            for (int i = 0; i < 10; i++) {
                attackLeft[i] = loadImage(basePath + "Attack/Left/" + "Left_" + (i+1) + ".png");
                attackRight[i] = loadImage(basePath + "Attack/Right/" + "Right_" + (i+1) + ".png");
            }

        } catch (IOException | NullPointerException e) { // Catch loading errors
            System.err.println("ERROR loading Bringer of Death images: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed debugging info
        }
    }

    private BufferedImage loadImage(String path) throws IOException, NullPointerException {
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            throw new NullPointerException("Resource not found: " + path);
        }
        BufferedImage img;
        try {
            img = ImageIO.read(stream);
        } finally {
            // Ensure stream is closed even if ImageIO.read throws an error
            try {
                stream.close();
            } catch (IOException e) {
                System.err.println("Error closing image stream for path: " + path);
                // Log or ignore this secondary error, main error (if any) will be thrown
            }
        }
        if (img == null) {
            throw new IOException("ImageIO.read returned null for path (unsupported format?): " + path);
        }
        return img;
    }

    private void loadFont() {
        InputStream fontStream = null; // Declare outside try for finally block
        try {
            // Load font file as a resource stream
            String fontPath = "/fonts/medieval-font.ttf"; // Ensure this path is correct
            fontStream = getClass().getResourceAsStream(fontPath);
            if (fontStream == null) {
                throw new IOException("Font resource not found: " + fontPath);
            }
            // Create font from stream and derive desired size/style
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            BODHpFont = customFont.deriveFont(Font.BOLD, 16F); // Set size and style

        } catch (FontFormatException | IOException e) {
            System.err.println("Error loading custom font for HP: " + e.getMessage() + ". Using default.");
            // Use a standard system font as a fallback
            BODHpFont = new Font("Arial", Font.BOLD, 16);
            e.printStackTrace(); // Print error details
        } finally {
            // Ensure the stream is closed
            if (fontStream != null) {
                try {
                    fontStream.close();
                } catch (IOException e) {
                    System.err.println("Error closing font stream.");
                }
            }
        }
    }

    @Override
    protected void takeDamage(int attackPower) {
        if (!alive) {
            return; // Cannot damage a dead entity
        }
        life -= attackPower;
        gp.playSE(5);

        if (life <= 0) {
            life = 0; // Prevent negative health
            die();    // Trigger the death sequence
        }
    }

    @Override
    protected void die() {
        if (!alive) {
            return; // Ensure die() only runs once
        }

        alive = false;      // Mark as dead for updates and drawing
        collision = false;  // Make corpse non-collidable for movement checks
        gp.playSE(6);
    }

    /**
     * Sets the aggression state.
     *
     * @param aggro true to make aggressive, false otherwise.
     */
    public void setAggro(boolean aggro) {
        if (this.aggro != aggro) { // Only change if state actually changes
            this.aggro = aggro;
            if (!aggro) {
                // Reset action cooldown when losing aggro so it doesn't immediately
                // change direction randomly based on a potentially old timer.
                actionCooldown = 0;
            }
        }
    }

    private boolean canSeePlayer() {
        // Basic checks
        if (gp.player == null) return false;
        int tileSize = gp.getTileSize();
        if (tileSize <= 0) return false; // Avoid division by zero

        // Use center points of solid areas for more accuracy
        int BODCenterX = worldX + solidArea.x + solidArea.width / 2;
        int BODCenterY = worldY + solidArea.y + solidArea.height / 2;
        int playerCenterX = gp.player.worldX + gp.player.solidArea.x + gp.player.solidArea.width / 2;
        int playerCenterY = gp.player.worldY + gp.player.solidArea.y + gp.player.solidArea.height / 2;

        // Vector and distance
        double dx = playerCenterX - BODCenterX;
        double dy = playerCenterY - BODCenterY;
        double distance = Math.hypot(dx, dy);

        // If very close, consider visible
        if (distance < tileSize / 2.0) { // Closer than half a tile? Assume visible.
            return true;
        }

        // Normalize direction vector (unit vector)
        dx /= distance;
        dy /= distance;

        // Step along the line checking tiles
        double currentX = BODCenterX;
        double currentY = BODCenterY;
        // Check full distance
        int steps = (int) Math.round(distance);

        for (int i = 0; i < steps; i++) {
            // Move one unit step along the line
            currentX += dx;
            currentY += dy;

            // Get tile coordinates at the current point
            int col = (int) (currentX / tileSize);
            int row = (int) (currentY / tileSize);

            // Check if the tile at these coordinates blocks line of sight
            try {
                if (gp.check.isTileBlocked(col, row)) {
                    return false; // Path is blocked
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                // Point is outside the map boundaries, consider it blocked
                return false;
            }
        }

        // If loop completes, no blocking tiles were found
        return true;
    }

    public void reset() {
        setDefaultValues();
    }

    @Override
    public boolean getCollision(){return collision;}
}