package Entity;

import Main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
// import java.io.File; // Only needed if loading font via File path, prefer resource stream
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Represents an Orc enemy in the game.
 * Handles its movement, AI (random wandering, aggro, attacking),
 * animation, drawing, and health management. Uses an improved
 * collision response mechanism to navigate obstacles when blocked.
 */
public class Orc extends Entity {

    // --- References & Utilities ---
    GamePanel gp;
    Random rndDirection = new Random();

    // --- Animation & Drawing ---
    BufferedImage[] walkDown = new BufferedImage[6];
    BufferedImage[] walkUp = new BufferedImage[6];
    BufferedImage[] walkLeft = new BufferedImage[6];
    BufferedImage[] walkRight = new BufferedImage[6];
    BufferedImage[] attackDown = new BufferedImage[7]; // Attack animations
    BufferedImage[] attackUp = new BufferedImage[7];
    BufferedImage[] attackLeft = new BufferedImage[7];
    BufferedImage[] attackRight = new BufferedImage[7];


    private int characterWidth = 90;  // Drawing width
    private int characterHeight = 90; // Drawing height
    public int spriteIndex = 0;        // Current frame index for walk/idle
    public int spriteCounter = 0;      // Timer for walk/idle animation speed
    private int attackSpriteIndex = 0;    // Current frame index for attack
    private int attackSpriteCounter = 0;  // Timer for attack animation speed

    private static final int WALK_ANIMATION_DELAY = 8; // Frames between walk/idle frame changes
    private static final int ATTACK_ANIMATION_DELAY = 6; // Frames between attack frame changes
    private Font orcHpFont;            // Pre-loaded font for HP display

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
    private String attackDirection = "down"; // Direction Orc faces *during* an attack
    private boolean hitPlayerThisSwing = false; // Ensure only one hit per attack animation

    /**
     * Constructor for the Orc entity.
     *
     * @param gp The main GamePanel instance.
     */
    public Orc(GamePanel gp) {
        this.gp = gp;

        // Define the collision area relative to the Orc's top-left corner (0,0)
        solidArea = new Rectangle(35, 65, 20, 10); // Adjust x, y, width, height
        solidAreaX = solidArea.x; // Store the default relative X offset
        solidAreaY = solidArea.y; // Store the default relative Y offset

        // Define attack area size (position is calculated dynamically)
        attackArea = new Rectangle(0, 0, 40, 40); // Example size, adjust as needed

        loadFont();         // Load custom font for HP display
        setDefaultValues(); // Set initial position, speed, health, etc.
        getOrcImage();      // Load animation sprites
    }

    /**
     * Sets the default starting values for the Orc when created or reset.
     */
    public void setDefaultValues() {
        // Set Orc Spawn point (adjust coordinates as needed)
        worldX = gp.getTileSize() * 216 - (gp.getTileSize() / 2);
        worldY = gp.getTileSize() * 172 - (gp.getTileSize() / 2);

        speed = 1;           // Orc movement speed
        direction = "down";  // Initial facing direction

        // Orc Status
        maxLife = 50;        // Maximum health points
        life = maxLife;      // Start with full health
        alive = true;        // Start alive
        collision = true;    // Start as a solid entity that blocks movement
        aggro = false;       // Start non-aggressive
        attacking = false;   // Start not attacking
        attackPower = 20;   // Attack Power

        // Reset timers and animation state
        actionCooldown = 0;
        attackCooldown = 0;
        attackRange = gp.getTileSize();
        spriteIndex = 0;
        spriteCounter = 0;
        attackSpriteIndex = 0;
        attackSpriteCounter = 0;
    }

    /**
     * Main update logic for the Orc. Called once per frame from GamePanel.
     * Handles AI decision-making, collision detection, movement, attacking,
     * and animation timing.
     */
    public void update() {
        // Ignore updates if game not playing or Orc is dead
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
            int dy = gp.player.worldY - worldY;
            double distance = Math.hypot(dx, dy); // Calculate distance to player

            // Condition to start an attack:
            boolean canAttack = distance < attackRange        // Player in range?
                    && attackCooldown <= 0        // Attack ready?
                    && !attacking                 // Not already attacking?
                    && canSeePlayer();            // Orc can see player?

            if (canAttack) {
                startAttack(); // Initiate the attack sequence
                // After starting attack, skip movement for this frame
                return; // Exit update early
            } else {
                // Aggro but cannot attack (out of range, cooldown, LOS blocked):
                // Determine direction to move towards player.
                if (Math.abs(dx) > Math.abs(dy)) {
                    direction = (dx > 0) ? "right" : "left";
                } else if (dy != 0) { // Avoid changing if dy=0 and dx=0
                    direction = (dy > 0) ? "down" : "up";
                }
                // Optional: Check line of sight again to potentially lose aggro
                // if (!canSeePlayer()) { aggro = false; }
            }
        }

        // --- 4. Collision Check for Movement ---
        // Check if the intended direction (from AI) is blocked by tiles or player.
        collisionOn = false; // Reset collision flag before checking
        gp.check.checkCollision(this);   // Check against solid tiles
        if (!collisionOn) { // Optimization: only check player if tiles are clear
            gp.check.checkForPlayer(this); // Check against player (for movement blocking)
        }

        // --- 5. Movement & Improved Collision Response ---
        // Execute movement only if the path was clear initially.
        // If blocked, try to move sideways. If still blocked, wait.
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

    } // End update()

    /**
     * Initiates the Orc's attack sequence: sets state, cooldown, and determines facing direction.
     */
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
        int dy = gp.player.worldY - worldY;
        if (Math.abs(dx) > Math.abs(dy)) {
            attackDirection = (dx > 0) ? "right" : "left";
        } else if (dy != 0) {
            attackDirection = (dy > 0) ? "down" : "up";
        } else {
            // If player is exactly centered, attack in current facing direction
            attackDirection = direction;
        }
        // Make the Orc visually face the direction they are attacking
        direction = attackDirection;

        // System.out.println("Orc starts attack facing " + attackDirection); // Debug
        gp.playSE(4);
        // TODO: Play attack sound effect? e.g., gp.playSoundEffect(...)
    }

    /**
     * Manages the active attack animation timing and triggers hit detection
     * during the appropriate frames. Called only when `attacking` is true.
     */
    private void handleAttackSequence() {
        // Update attack animation timer
        attackSpriteCounter++;
        if (attackSpriteCounter > ATTACK_ANIMATION_DELAY) {

            attackSpriteIndex++; // Advance to the next frame
            attackSpriteCounter = 0; // Reset timer

            // --- Hit Detection during specific frames ---
            // Adjust these frame indices based on your Orc's attack animation sprites
            int hitFrameStart = 2; // Example: Hitbox becomes active on frame index 2
            int hitFrameEnd = 4;   // Example: Hitbox stays active until frame index 4
            if (attackSpriteIndex >= hitFrameStart && attackSpriteIndex <= hitFrameEnd) {
                performAttackHitCheck(); // Check if the attack hits the player
            }

            // --- Determine which attack animation array to use ---
            BufferedImage[] currentAttackAnim = switch (attackDirection) {
                case "up":    yield attackUp; // Using yield for switch expression
                case "down":  yield attackDown;
                case "left":  yield attackLeft;
                case "right": yield attackRight;
                default:      yield attackDown; // Fallback
            };

            // --- Check if Animation Has Finished ---
            if (currentAttackAnim != null && attackSpriteIndex >= currentAttackAnim.length) {
                // Animation cycle is complete
                attacking = false;      // End the attacking state
                attackSpriteIndex = 0;  // Reset index for the next attack
                // System.out.println("Orc attack finished."); // Debug

            }
        }
        // Also update the general animation counters to keep them ticking (for draw method)
        // This ensures the correct index is available for drawing even if logic is here

        spriteCounter = attackSpriteCounter;
        spriteIndex = attackSpriteIndex;
    }

    /**
     * Calculates the Orc's attack hitbox position based on its current state
     * and checks if it intersects with the player's solid area. Applies damage if it hits.
     * Called only during specific frames defined in `handleAttackSequence`.
     */
    private void performAttackHitCheck() {
        // Only allow one successful hit per attack swing/animation cycle
        if (hitPlayerThisSwing || gp.player == null) {
            return;
        }

        // Create a temporary rectangle for the attack area check
        Rectangle orcAttackArea = new Rectangle();
        // Use the size defined in the Orc's fields (set in constructor)
        orcAttackArea.width = this.attackArea.width;
        orcAttackArea.height = this.attackArea.height;

        // Calculate the offset of the attack area relative to the Orc's top-left (0,0)
        // based on the direction the attack was initiated in (attackDirection)
        int attackOffsetX = 0;
        int attackOffsetY = 0;

        switch (attackDirection) { // Use the stored direction of the attack
            case "up":
                // Center above solidArea
                attackOffsetX = solidArea.x + (solidArea.width / 2) - (orcAttackArea.width / 2);
                attackOffsetY = solidArea.y - orcAttackArea.height;
                break;
            case "down":
                // Center below solidArea
                attackOffsetX = solidArea.x + (solidArea.width / 2) - (orcAttackArea.width / 2);
                attackOffsetY = solidArea.y + solidArea.height;
                break;
            case "left":
                // Center vertically, left of solidArea
                attackOffsetX = solidArea.x - orcAttackArea.width;
                attackOffsetY = solidArea.y + (solidArea.height / 2) - (orcAttackArea.height / 2);
                break;
            case "right":
                // Center vertically, right of solidArea
                attackOffsetX = solidArea.x + solidArea.width;
                attackOffsetY = solidArea.y + (solidArea.height / 2) - (orcAttackArea.height / 2);
                break;
        }
        // Set the world coordinates for the calculated attack area
        orcAttackArea.x = worldX + attackOffsetX;
        orcAttackArea.y = worldY + attackOffsetY;

        // Get the Player's solidArea in world coordinates for intersection check
        Rectangle playerWorldSolidArea = new Rectangle(
                gp.player.worldX + gp.player.solidArea.x,
                gp.player.worldY + gp.player.solidArea.y,
                gp.player.solidArea.width,
                gp.player.solidArea.height
        );

        // --- Check for Intersection ---
        if (orcAttackArea.intersects(playerWorldSolidArea)) {
            // System.out.println("Orc HIT Player!"); // Debug

            // Apply damage to player (Player class needs a takeDamage method)
            gp.player.takeDamage(attackPower); // Use the Orc's defined attackPower

            // Mark that a hit occurred this swing to prevent multiple damage instances
            hitPlayerThisSwing = true;

            // Optional: Apply knockback effect to the player
            // gp.player.applyKnockback(orcAttackArea.x, orcAttackArea.y, knockbackPower);
        }

    }

    /**
     * Moves the Orc one step based on its current `direction` field.
     * Assumes the path is already checked and clear.
     */
    private void moveNormally() {
        switch (direction) {
            case "up":
                worldY -= speed;
                break;
            case "down":
                worldY += speed;
                break;
            case "left":
                worldX -= speed;
                break;
            case "right":
                worldX += speed;
                break;
        }
    }

    /**
     * Handles collision response when the Orc's primary intended movement path is blocked.
     * Tries to move perpendicularly; if still blocked, picks a random direction and waits.
     */
    private void handleMovementCollision() {
        String[] perpendicular = getPerpendicularDirections(direction);

        // Shuffle check order to avoid bias (e.g., always trying left first)
        List<String> checkOrder = Arrays.asList(perpendicular);
        Collections.shuffle(checkOrder);

        boolean movedPerpendicular = false;
        // Check each perpendicular direction
        for (String checkDir : checkOrder) {
            // Use helper method to predict if one step in this direction is clear
            if (isStepClear(checkDir)) {
                // Clear path found! Change direction and move one step.
                direction = checkDir; // Update direction
                moveNormally();       // Take the clear step
                movedPerpendicular = true; // Mark that we successfully moved
                // System.out.println("Orc Collision: Moved perpendicularly " + direction); // Debug
                break; // Exit the loop after moving
            }
        }

        // If still blocked after checking both perpendicular directions...
        if (!movedPerpendicular) {
            // Fall back: Pick a new random direction and wait until next frame.
            // System.out.println("Orc Collision: Fully blocked, changing direction randomly."); // Debug
            handleBlockedResponse();
        }
    }

    /**
     * Predicts if moving one step in the given direction would result in a collision
     * with solid tiles or the player. Restores original state after checking.
     *
     * @param checkDirection The direction to check ("up", "down", "left", "right").
     * @return true if the step is clear, false otherwise.
     */
    private boolean isStepClear(String checkDirection) {
        boolean collisionDetected = false;
        boolean originalCollisionOnState = this.collisionOn; // Store flag state

        // Calculate potential next world position
        int futureX = worldX;
        int futureY = worldY;
        switch (checkDirection) {
            case "up": futureY -= speed; break;
            case "down": futureY += speed; break;
            case "left": futureX -= speed; break;
            case "right": futureX += speed; break;
        }

        // Temporarily set solidArea world position for the checker
        int originalRectX = this.solidArea.x; // Store relative X
        int originalRectY = this.solidArea.y; // Store relative Y
        this.solidArea.x = futureX + this.solidAreaX;
        this.solidArea.y = futureY + this.solidAreaY;

        // --- Perform Collision Checks ---
        this.collisionOn = false; // Reset flag specifically for this prediction

        // 1. Check against solid tiles
        gp.check.checkCollision(this);
        if (this.collisionOn) {
            collisionDetected = true;
        }

        // 2. Check against player (only if no tile collision)
        if (!collisionDetected) {
            this.collisionOn = false; // Reset between checks if needed
            gp.check.checkForPlayer(this);
            if (this.collisionOn) {
                collisionDetected = true;
            }
        }

        // --- Restore Original State ---
        this.solidArea.x = originalRectX; // Restore relative X
        this.solidArea.y = originalRectY; // Restore relative Y
        this.collisionOn = originalCollisionOnState; // Restore flag state

        // Return TRUE if path is CLEAR (no collision detected)
        return !collisionDetected;
    }

    /**
     * Returns the two directions perpendicular to the given direction.
     *
     * @param currentDir The current direction ("up", "down", "left", "right").
     * @return String array with the two perpendicular directions.
     */
    private String[] getPerpendicularDirections(String currentDir) {
        switch (currentDir) {
            case "up":
            case "down":
                return new String[]{"left", "right"};
            case "left":
            case "right":
                return new String[]{"up", "down"};
            default: // Should not happen with valid direction
                return new String[]{"left", "right"};
        }
    }

    /**
     * Handles the response when the Orc is blocked in its primary direction
     * AND both perpendicular directions. Picks a new random direction and waits.
     */
    private void handleBlockedResponse() {
        String previousDirection = direction;
        int attempts = 0;
        // Try up to 10 times to pick a *different* random direction
        do {
            int i = rndDirection.nextInt(100); // Random number 0-99
            if (i < 25) { direction = "up"; }
            else if (i < 50) { direction = "down"; }
            else if (i < 75) { direction = "left"; }
            else { direction = "right"; }
            attempts++;
        } while (direction.equals(previousDirection) && attempts < 10);

        actionCooldown = 0; // Reset random movement timer
        // Do not move this frame.
    }

    /**
     * Sets a random direction at intervals defined by ACTION_INTERVAL.
     * Only active when the Orc is not aggressive (`aggro == false`).
     */
    // If overriding a method from Entity (good practice)
    public void setAction() {
        // Increment cooldown timer only when not aggro (handled in update)
        // actionCooldown++; // Moved timer update to main update method

        // Check if it's time to change direction
        if (actionCooldown >= ACTION_INTERVAL) {
            int i = rndDirection.nextInt(100); // 0-99
            if (i < 25) { direction = "up"; }
            else if (i < 50) { direction = "down"; }
            else if (i < 75) { direction = "left"; }
            else { direction = "right"; }
            actionCooldown = 0; // Reset timer
        }
    }

    /**
     * Updates the sprite index for walk/idle animation based on ANIMATION_DELAY.
     * Ensures the index loops correctly for the current animation array.
     * This is NOT used when the Orc is attacking.
     */
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
            BufferedImage[] anim = switch (direction) {
                case "up":    yield walkUp;
                case "left":  yield walkLeft;
                case "down":  yield walkDown;
                case "right": yield walkRight;
                default:      yield walkDown; // Fallback
            };

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


    /**
     * Draws the Orc sprite, HP bar, and optional debug visuals.
     * Selects the correct animation (walk/idle or attack) based on state.
     *
     * @param g2 The Graphics2D context to draw on.
     */
    @Override
    public void draw(Graphics2D g2) {
        // Don't draw if dead (add check for death animation later if needed)
        if (!alive) {
            return;
        }

        // Calculate Orc's position on the screen relative to the player
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        // Optimization: Only draw if Orc is roughly within screen bounds
        if (worldX + gp.getTileSize() > gp.player.worldX - gp.player.screenX &&
                worldX - gp.getTileSize() < gp.player.worldX + gp.player.screenX &&
                worldY + gp.getTileSize() > gp.player.worldY - gp.player.screenY &&
                worldY - gp.getTileSize() < gp.player.worldY + gp.player.screenY)
        {
            // --- Select Correct Animation Set and Index ---
            BufferedImage[] currentAnimationSet;
            int currentSpriteIndex;

            if (attacking) {
                // Use attack animation set based on the stored attackDirection
                currentAnimationSet = switch (attackDirection) {
                    case "up":    yield attackUp;
                    case "down":  yield attackDown;
                    case "left":  yield attackLeft;
                    case "right": yield attackRight;
                    default:      yield attackDown; // Fallback
                };
                // Use the dedicated attack sprite index
                currentSpriteIndex = attackSpriteIndex;
            } else {
                // Use walk/idle animation set based on the current facing direction
                currentAnimationSet = switch (direction) {
                    case "up":    yield walkUp;
                    case "left":  yield walkLeft;
                    case "down":  yield walkDown;
                    case "right": yield walkRight;
                    default:      yield walkDown; // Fallback
                };
                // Use the walk/idle sprite index
                currentSpriteIndex = spriteIndex;
            }

            // --- Drawing the Orc Sprite ---
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
                g2.drawString("ORC?", screenX + 10, screenY + 20);
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

            // --- Drawing Debug Hitbox (Optional) ---
            // Uncomment to visualize the solid area
            // g2.setColor(new Color(255, 0, 0, 100)); // Semi-transparent red
            // g2.fillRect(screenX + solidAreaX, screenY + solidAreaY, solidArea.width, solidArea.height);

            // --- Drawing HP Bar ---
            drawHPBar(g2, screenX, screenY);

        } // End on-screen check
    } // End draw()


    /**
     * Helper method to draw the Orc's HP bar above its head.
     * @param g2 Graphics context.
     * @param screenX Orc's screen X position.
     * @param screenY Orc's screen Y position.
     */
    private void drawHPBar(Graphics2D g2, int screenX, int screenY) {
        // Don't draw HP bar if Orc has full health (optional clutter reduction)
        // if (life == maxLife) return;

        // Set font (use preloaded or fallback)
        if (orcHpFont != null) {
            g2.setFont(orcHpFont);
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
        // Position above Orc's head
        int hpBarY = screenY - hpBarHeight - 2; // Add small gap
        int hpBarX = screenX;

        // Draw the background (empty part) of the HP bar
        g2.setColor(Color.DARK_GRAY); // Or black
        g2.fillRect(hpBarX, hpBarY, hpBarFullWidth, hpBarHeight);
        // Draw the foreground (current health) of the HP bar
        g2.setColor(Color.RED); // Or green
        g2.fillRect(hpBarX, hpBarY, hpBarCurrentWidth, hpBarHeight);
        // Optional: Add a border around the bar
        // g2.setColor(Color.BLACK);
        // g2.drawRect(hpBarX, hpBarY, hpBarFullWidth, hpBarHeight);
    }


    /**
     * Loads Orc animation images from resource stream.
     * Handles potential errors during loading.
     */
    public void getOrcImage() {
        // System.out.println("Loading Orc images..."); // Debug message
        try {
            // Define the base path for Orc images within your resources folder
            String basePath = "/Entities/Enemies/Orcs/"; // Make sure this path is correct!

            // Load Walk Animations (assuming 6 frames)
            for (int i = 0; i < 6; i++) {
                walkDown[i] = loadImage(basePath + "Walk(Down)/" + (i + 1) + ".png");
                walkLeft[i] = loadImage(basePath + "Walk(Left)/" + (i + 1) + ".png");
                walkRight[i] = loadImage(basePath + "Walk(Right)/" + (i + 1) + ".png");
                walkUp[i] = loadImage(basePath + "Walk(Up)/" + (i + 1) + ".png");
            }

            // Load Attack Animations (assuming 7 frames)
            for (int i = 0; i < 7; i++) {
                attackDown[i] = loadImage(basePath + "Attack(Down)/" + (i + 1) + ".png");
                attackLeft[i] = loadImage(basePath + "Attack(Left)/" + (i + 1) + ".png");
                attackRight[i] = loadImage(basePath + "Attack(Right)/" + (i + 1) + ".png");
                attackUp[i] = loadImage(basePath + "Attack(Up)/" + (i + 1) + ".png");
            }

            // System.out.println("Orc images loaded successfully."); // Debug success message
        } catch (IOException | NullPointerException e) { // Catch loading errors
            System.err.println("ERROR loading Orc images: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed debugging info
        }
    }

    /**
     * Helper method to load a single image from the resource path.
     *
     * @param path The resource path (e.g., "/Entities/Enemies/Orcs/Walk(Down)/1.png").
     * @return The loaded BufferedImage.
     * @throws IOException If reading the image fails or stream closing fails.
     * @throws NullPointerException If the resource path is invalid or not found.
     */
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

    /**
     * Loads and prepares the custom font used for drawing Orc HP.
     * Uses a fallback font if loading fails.
     */
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
            orcHpFont = customFont.deriveFont(Font.BOLD, 16F); // Set size and style
            // System.out.println("Custom font loaded successfully."); // Debug

        } catch (FontFormatException | IOException e) {
            System.err.println("Error loading custom font for Orc HP: " + e.getMessage() + ". Using default.");
            // Use a standard system font as a fallback
            orcHpFont = new Font("Arial", Font.BOLD, 16);
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

    /**
     * Reduces Orc's health and handles death transition.
     *
     * @param attackPower The amount of damage to inflict.
     */
    @Override
    public void takeDamage(int attackPower) {
        if (!alive) {
            return; // Cannot damage a dead Orc
        }
        life -= attackPower;
        gp.playSE(5);
        // System.out.println("Orc took " + amount + " damage, life: " + life); // Debug
        // Add sound effect or visual indicator of damage here?
        if (life <= 0) {
            life = 0; // Prevent negative health
            die();    // Trigger the death sequence
        }
    }

    /**
     * Handles the Orc's death state transition.
     * Marks the Orc as not alive and not solid. Triggers death effects.
     */
    @Override
    protected void die() {
        if (!alive) {
            return; // Ensure die() only runs once
        }
        // System.out.println("Orc died!"); // Debug
        alive = false;      // Mark as dead for updates and drawing
        collision = false;  // Make corpse non-collidable for movement checks
        gp.playSE(6);
        // TODO: Play death sound effect: gp.playSoundEffect(...)
        // TODO: Trigger death particle effect: gp.particleManager.generate(...)
        // TODO: Start death animation timer/state if you have specific corpse sprites
    }

    /**
     * Sets the Orc's aggression state.
     *
     * @param aggro true to make the Orc aggressive, false otherwise.
     */
    public void setAggro(boolean aggro) {
        if (this.aggro != aggro) { // Only print/change if state actually changes
            // System.out.println("Orc aggro set to: " + aggro); // Debug
            this.aggro = aggro;
            if (!aggro) {
                // Reset action cooldown when losing aggro so it doesn't immediately
                // change direction randomly based on a potentially old timer.
                actionCooldown = 0;
            }
        }
    }

    /**
     * Checks if there is an unobstructed line of sight between the Orc and the Player.
     * Uses simple step-based raycasting against solid tiles.
     * Requires gp.check.isTileBlocked(col, row) to be implemented in CollisionChecker.
     *
     * @return true if line of sight is clear, false otherwise.
     */
    private boolean canSeePlayer() {
        // Basic checks
        if (gp.player == null) return false;
        int tileSize = gp.getTileSize();
        if (tileSize <= 0) return false; // Avoid division by zero

        // Use center points of solid areas for more accuracy
        int orcCenterX = worldX + solidArea.x + solidArea.width / 2;
        int orcCenterY = worldY + solidArea.y + solidArea.height / 2;
        int playerCenterX = gp.player.worldX + gp.player.solidArea.x + gp.player.solidArea.width / 2;
        int playerCenterY = gp.player.worldY + gp.player.solidArea.y + gp.player.solidArea.height / 2;

        // Vector and distance
        double dx = playerCenterX - orcCenterX;
        double dy = playerCenterY - orcCenterY;
        double distance = Math.hypot(dx, dy);

        // If very close, consider visible
        if (distance < tileSize / 2.0) { // Closer than half a tile? Assume visible.
            return true;
        }

        // Normalize direction vector (unit vector)
        dx /= distance;
        dy /= distance;

        // Step along the line checking tiles
        double currentX = orcCenterX;
        double currentY = orcCenterY;
        // Check one step less than the full distance to avoid checking the player's own tile? Or check full distance. Let's check full.
        int steps = (int) Math.round(distance);

        for (int i = 0; i < steps; i++) {
            // Move one unit step along the line
            currentX += dx;
            currentY += dy;

            // Get tile coordinates at the current point
            int col = (int) (currentX / tileSize);
            int row = (int) (currentY / tileSize);

            // Check if the tile at these coordinates blocks line of sight
            // !!! IMPORTANT: Implement isTileBlocked(col, row) in CollisionChecker !!!
            // It should return true if the tile at (col, row) is solid/opaque.
            try {
                if (gp.check.isTileBlocked(col, row)) {
                    // System.out.println("Orc LOS blocked at: " + col + "," + row); // Debug
                    return false; // Path is blocked
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                // Point is outside the map boundaries, consider it blocked
                // System.err.println("Orc LOS check out of bounds: " + col + "," + row); // Debug
                return false;
            }
        }

        // If loop completes, no blocking tiles were found
        return true;
    }

    public void reset() {
        setDefaultValues();
    }
} // End of Orc class