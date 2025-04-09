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
import java.io.InputStream;
import java.util.ArrayList; // Import needed

public class Player extends Entity {

    // --- Image Arrays ---
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
    public BufferedImage heart_full, heart_blank, heart_half;

    // --- References ---
    GamePanel gamePanel;
    KeyHandler keyH;
    MouseHandler mouseH;

    // --- Animation ---
    public int spriteIndex = 0;         // For idle/walk
    public int spriteCounter = 0;       // For idle/walk
    public int attackSpriteIndex = 0;   // Separate index for attack animation
    public int attackSpriteCounter = 0; // Separate counter for attack animation
    public final int WALK_ANIMATION_SPEED = 6;  // Lower is faster
    public final int ATTACK_ANIMATION_SPEED = 5; // Lower is faster

    // --- Positioning & Size ---
    public final int characterWidth = 120; // Drawing size
    public final int characterHeight = 120;// Drawing size
    public final int screenX;              // Player screen position X
    public final int screenY;              // Player screen position Y

    // --- Aggro ---
    public Ellipse2D.Double aggroRange;
    int radius = 200;
    int diameter = radius * 2;

    // --- Combat & State ---
    private ArrayList<Entity> hitEntitiesThisSwing; // Track entities hit in current attack

    // --- Font ---
    public Font playerHpFont;


    // --- Lifesteal ---
    public double lifeSteal;

    public Player(GamePanel gamePanel, KeyHandler keyH, MouseHandler mouseH) {
        this.gamePanel = gamePanel;
        this.keyH = keyH;
        this.mouseH = mouseH;

        // Screen position calculation
        screenX = gamePanel.screenWidth / 2 - (gamePanel.getTileSize() / 2);
        screenY = gamePanel.screenHeight / 2 - (gamePanel.getTileSize() / 2);

        // Define collision area (relative to top-left 0,0)
        solidArea = new Rectangle(45, 75, 30, 20);
        solidAreaX = solidArea.x; // Store default relative X
        solidAreaY = solidArea.y; // Store default relative Y

        // Define attack area (size only, position is set dynamically)
        // Dimensions relative to player's solidArea/facing direction
        attackArea = new Rectangle(0, 0, 40, 40); // Increased size slightly


        // Init aggro circle
        aggroRange = new Ellipse2D.Double(); // Position set in updateAggroCircle

        setDefaultValues();
        getPlayerImage(); // Load sprites
        loadHeartImages(); // Load heart sprites
        loadFont(); // Load Font

        hitEntitiesThisSwing = new ArrayList<>(); // Initialize hit tracking list
    }

    public void setDefaultValues() {
        // Default player Spawn point
        worldX = gamePanel.getTileSize() * 151 - (gamePanel.getTileSize() / 2); //430
        worldY = gamePanel.getTileSize() * 123 - (gamePanel.getTileSize() / 2); //730

        speed = 4;
        direction = "idle";
        prevDirection = "down"; // Start facing down

        // Player Status
        maxLife = 500; // e.g., 5 hearts
        life = maxLife; // Start with full life
        attackPower = 10;
        lifeSteal = attackPower * 0.5;

        attacking = false;
        collisionOn = false;
        // gamePanel.playMusic(0); // Consider managing music in GamePanel state changes
        alive = true;
    }

    private void loadHeartImages() {
        try {
            OBJ_HEART heart = new OBJ_HEART(gamePanel); // Assuming this loads images
            heart_full = heart.image1;
            heart_half = heart.image2;
            heart_blank = heart.image3;
        } catch (Exception e) {
            System.err.println("Error loading heart images: " + e);
            // Handle error, maybe use placeholder colors/shapes
        }

    }

    /**
     * Main update method for the Player. Called from GamePanel's game loop.
     */
    public void update() {
        // 1. Handle Attack State
        if (attacking) {
            handleAttack();
            // Optional: Prevent movement *during* the attack animation
            // updateAggroCircle(); // Still update aggro circle position
            // return; // Uncomment this line to stop movement processing while attacking
        }

        // 2. Check for Attack Initiation (only if not already attacking)
        // Assumes mouseH.isAttacking becomes true on press, false on release
        if (mouseH.isAttacking && !attacking) {
            startAttack();
            // Optional: Prevent movement in the *same frame* attack starts
            // updateAggroCircle();
            // return;
        }

        // 3. Handle Movement Input (only if not attacking, or allowed during attack)
        boolean isMoving = keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed;
        if (isMoving) {
            // Determine direction based on input
            if (keyH.upPressed) direction = "up";
            else if (keyH.downPressed) direction = "down";
            else if (keyH.leftPressed) direction = "left";
            else direction = "right";

            // Update facing direction only when actively moving
            prevDirection = direction;

            // Check collisions BEFORE moving
            collisionOn = false;
            gamePanel.check.checkCollision(this); // Check tile collisions
            gamePanel.check.checkForMonster(this); // Check movement collision with monsters

            // Move player if no collision detected
            if (!collisionOn) {
                movePlayer();
            }

            // Handle walking animation
            handleMovementAnimation();

        } else {
            // Handle idle state
            direction = "idle";
            // Reset walking animation index when stopping (optional, depends on idle sprite)
            // spriteIndex = 0; // If idle is always frame 0 of the last direction
        }

        // 4. Update Aggro Circle Position and Check Targets
        updateAggroCircle();
        checkAggro();

        // 5. Update Player Status (e.g., regeneration, cooldowns - not implemented here)
        // ...
    }

    /**
     * Starts the attack sequence.
     */
    private void startAttack() {
        attacking = true;
        attackSpriteIndex = 0;      // Reset attack animation frame
        attackSpriteCounter = 0;    // Reset attack animation timer
        hitEntitiesThisSwing.clear(); // Clear list of entities hit by the previous swing

        // Calculate the initial hitbox position IMMEDIATELY
        updateAttackHitboxPosition(); // <<<====== ADD THIS LINE

        // Play attack sound effect?
        // gamePanel.playSoundEffect( /* attack sound index */ );
        System.out.println("Attack Started - Facing: " + prevDirection + " Hitbox at: " + attackArea.x + "," + attackArea.y); // Debug
    }

    /**
     * Manages the attack animation, hitbox positioning, and hit detection during an attack.
     */
    private void handleAttack() {
        attackSpriteCounter++;

        if (attackSpriteCounter > ATTACK_ANIMATION_SPEED) {
            attackSpriteIndex++;
            attackSpriteCounter = 0;

            // Define Attack Hitbox Position for the current frame/direction
            updateAttackHitboxPosition();

            // Check for Hits during specific frames of the animation
            // Adjust frame numbers (e.g., 1 to 3) based on when the swing connects in your sprites
            int hitFrameStart = 1;
            int hitFrameEnd = 3;
            if (attackSpriteIndex >= hitFrameStart && attackSpriteIndex <= hitFrameEnd) {
                // System.out.println("Attack Check - Frame: " + attackSpriteIndex); // Debug
                performAttackCheck();
            }

            // End Attack Animation
            BufferedImage[] attackAnim = getAttackAnimationArray();
            if (attackSpriteIndex >= attackAnim.length) {
                attackSpriteIndex = 0;
                attacking = false; // Attack animation finished
                hitEntitiesThisSwing.clear(); // Clear hit list for safety
                System.out.println("Attack Finished"); // Debug
            }
        }
    }

    /**
     * Calculates and sets the world coordinates of the attackArea rectangle
     * based on the player's current position, solidArea, and facing direction.
     */
    private void updateAttackHitboxPosition() {
        // Calculate offset based on player's solid area and direction
        int attackOffsetX = 0;
        int attackOffsetY = 0;

        // Position the attack area relative to the player's solid body
        switch (prevDirection) { // Use prevDirection: where the player was facing when attacking
            case "up":
                // Center hitbox horizontally above player's solidArea
                attackArea.x = worldX + solidArea.x + (solidArea.width / 2) - (attackArea.width / 2);
                attackArea.y = worldY + solidArea.y - attackArea.height; // Position directly above
                break;
            case "down":
                // Center hitbox horizontally below player's solidArea
                attackArea.x = worldX + solidArea.x + (solidArea.width / 2) - (attackArea.width / 2);
                attackArea.y = worldY + solidArea.y + solidArea.height; // Position directly below
                break;
            case "left":
                // Center hitbox vertically left of player's solidArea
                attackArea.x = worldX + solidArea.x - attackArea.width; // Position directly left
                attackArea.y = worldY + solidArea.y + (solidArea.height / 2) - (attackArea.height / 2);
                break;
            case "right":
                // Center hitbox vertically right of player's solidArea
                attackArea.x = worldX + solidArea.x + solidArea.width; // Position directly right
                attackArea.y = worldY + solidArea.y + (solidArea.height / 2) - (attackArea.height / 2);
                break;
        }
        // System.out.println("Attack Hitbox World: " + attackArea.x + ", " + attackArea.y); // Debug
    }

    /**
     * Checks if the current attackArea intersects with any enemies.
     */
    private void performAttackCheck() {
        // Iterate through monsters (use the actual list from GamePanel)
        for (Entity monster : gamePanel.orcs) { // Assuming gamePanel.orcs is the list of enemies (use Entity type)
            if (monster != null && monster != this && !hitEntitiesThisSwing.contains(monster)) { // Check if monster exists, isn't self, and hasn't been hit this swing

                // Get monster's world bounds for collision check
                // Important: Use the monster's current world position and its *relative* solidArea offsets
                Rectangle monsterWorldBounds = new Rectangle(
                        monster.worldX + monster.solidArea.x,
                        monster.worldY + monster.solidArea.y,
                        monster.solidArea.width,
                        monster.solidArea.height
                );

                // Check intersection between player's attackArea and monster's solidArea
                if (attackArea.intersects(monsterWorldBounds)) {
                    System.out.println("HIT! " + monster.getClass().getSimpleName()); // Log hit

//                     Apply damage to the monster (assuming a takeDamage method exists)
                    if (monster instanceof Orc) { // Example: Cast if needed for specific methods
                        ((Orc) monster).takeDamage(attackPower);
                        if(life + lifeSteal < maxLife) {
                            life += lifeSteal;
                        }

                    }
                    // else if (monster instanceof Slime) { ... } // Handle other monster types

                    hitEntitiesThisSwing.add(monster); // Add to list to prevent multiple hits per swing
                }
            }
        }
        // You could also check for destructible objects here
        // for (DestructibleObject obj : gamePanel.destructibles) { ... }
    }

    /**
     * Moves the player based on the current direction and speed.
     * Handles diagonal speed correction.
     */
    private void movePlayer() {
        if(!gamePanel.isMoving) {
            return;
        }
        double currentSpeed = speed;
        boolean diagonal = (keyH.upPressed || keyH.downPressed) && (keyH.leftPressed || keyH.rightPressed);
        if (diagonal) {
            // Adjust speed for diagonal movement to prevent moving faster
            currentSpeed = speed / Math.sqrt(2);
        }

        // Apply movement based on direction
        switch (direction) {
            case "up": worldY -= currentSpeed; break;
            case "down": worldY += currentSpeed; break;
            case "left": worldX -= currentSpeed; break;
            case "right": worldX += currentSpeed; break;
        }
    }

    /**
     * Handles the sprite animation for walking states.
     */
    private void handleMovementAnimation() {
        spriteCounter++;
        if (spriteCounter > WALK_ANIMATION_SPEED) {
            spriteIndex++;
            spriteCounter = 0;
            BufferedImage[] currentImageArray = getImageArrayForState(); // Get current animation set (walk or idle)
            // Loop the animation
            if (spriteIndex >= currentImageArray.length) {
                spriteIndex = 0;
            }
        }
    }

    /**
     * Sets the aggro status of nearby monsters based on the aggroRange.
     */
    private void checkAggro(){
        for(Entity m : gamePanel.orcs){ // Use Entity type for broader compatibility
            if (m != null && m instanceof Orc) { // Check type if calling type-specific methods like setAggro
                Orc orc = (Orc) m; // Cast to Orc
                // Calculate monster center point
                double monsterCenterX = orc.worldX + orc.solidArea.x + orc.solidArea.width / 2.0;
                double monsterCenterY = orc.worldY + orc.solidArea.y + orc.solidArea.height / 2.0;
                // Set aggro based on whether the monster's center is inside the player's aggro range
                orc.setAggro(aggroRange.contains(monsterCenterX, monsterCenterY));
            }
        }
    }

    /**
     * Updates the world position of the aggro circle based on the player's center.
     */
    public void updateAggroCircle() {
        // Center based on the middle of the *solid area* in the world
        double centerX = worldX + solidArea.x + solidArea.width / 2.0;
        double centerY = worldY + solidArea.y + solidArea.height / 2.0;
        aggroRange.setFrame(centerX - radius, centerY - radius, diameter, diameter);
    }

    /**
     * Gets the correct animation array based on the current player state (attacking, walking, idle).
     * @return The BufferedImage array for the current state.
     */
    private BufferedImage[] getImageArrayForState() {
        if (attacking) {
            return getAttackAnimationArray();
        } else { // Idle or Walking
            return switch (direction) {
                case "up" -> walkUp;
                case "left" -> walkLeft;
                case "down" -> walkDown;
                case "right" -> walkRight;
                case "idle" -> switch(prevDirection){ // Use prevDirection for idle facing
                    case "right" -> idleRight.length > 0 ? idleRight : walkRight; // Fallback to walk if idle missing
                    case "left" -> idleLeft.length > 0 ? idleLeft : walkLeft;
                    case "up" -> idleUp.length > 0 ? idleUp : walkUp;
                    case "down" -> idleDown.length > 0 ? idleDown : walkDown;
                    default -> idleDown.length > 0 ? idleDown : walkDown; // Default idle state
                };
                default -> idleDown.length > 0 ? idleDown : walkDown; // Should not happen
            };
        }
    }

    /**
     * Gets the specific attack animation array based on the player's facing direction.
     * @return The BufferedImage array for the attack.
     */
    private BufferedImage[] getAttackAnimationArray() {
        return switch (prevDirection) { // Attack direction based on last movement
            case "left" -> AttackLeft;
            case "down" -> AttackFront;
            case "right" -> AttackRight;
            case "up" -> AttackUp;
            default -> AttackFront; // Default attack animation
        };
    }

    /**
     * Draws the player, health bar, and debug visuals.
     * @param g2 The Graphics2D context.
     */
    @Override
    public void draw(Graphics2D g2) {

        if(!alive) {
            return;
        }

        BufferedImage[] currentImageArray = getImageArrayForState();
        int currentSpriteIndex = attacking ? attackSpriteIndex : spriteIndex;

        // Ensure spriteIndex is valid for the selected array
        if (currentSpriteIndex >= currentImageArray.length) {
            currentSpriteIndex = 0; // Fallback to first frame if index is out of bounds
        }

        // --- Draw Player Sprite ---
        if(currentImageArray.length > 0 && currentImageArray[currentSpriteIndex] != null) {
            g2.drawImage(currentImageArray[currentSpriteIndex], screenX, screenY, characterWidth, characterHeight, null);
        } else {
            // Draw placeholder if image is missing
            g2.setColor(Color.MAGENTA);
            g2.fillRect(screenX, screenY, characterWidth, characterHeight);
            g2.setColor(Color.BLACK);
            g2.drawString("IMG?", screenX + 10, screenY + 20);
        }


        // --- Draw Debug Visuals (Uncomment as needed) ---
        drawDebugInfo(g2);

        // --- Draw Player Health Bar (or Hearts) ---
        // drawPlayerHeart(g2); // Or use the HP text below
        drawPlayerHP(g2);
        drawHPBar(g2,screenX,screenY);
    }

    @Override
    protected void takeDamage(int attackPower) {
        if(!alive) {
            return;
        }
        life -= attackPower;
        gamePanel.playSE(3);
        if(life <= 0) {
            life = 0;
            die();
        }
    }

    @Override
    protected void die() {
        if(!alive) {
            return;
        }
        alive = false;
        collisionOn = false;
    }

    /** Draws debug rectangles and circles */
    private void drawDebugInfo(Graphics2D g2){
        // --- Draw Attack Hitbox (Debug) ---
        if (attacking) {
            // Attack area X/Y are already in world coordinates
            // Convert world coordinates of attackArea to screen coordinates for drawing
            int attackScreenX = attackArea.x - worldX + screenX;
            int attackScreenY = attackArea.y - worldY + screenY;

            g2.setColor(new Color(255, 255, 0, 150)); // Yellow, semi-transparent
            g2.fillRect(attackScreenX, attackScreenY, attackArea.width, attackArea.height); // Use fillRect for visibility
            g2.setColor(Color.YELLOW);
            g2.drawRect(attackScreenX, attackScreenY, attackArea.width, attackArea.height); // Outline
        }

        // --- Draw Solid Area (Debug) ---
//        g2.setColor(new Color(255, 0, 0, 100)); // Red, semi-transparent
//        int solidScreenX = screenX + solidAreaX; // Use stored relative offset
//        int solidScreenY = screenY + solidAreaY; // Use stored relative offset
//        g2.fillRect(solidScreenX, solidScreenY, solidArea.width, solidArea.height);

        // --- Draw Aggro Circle (Debug) ---
//        double aggroScreenX = aggroRange.getX() - worldX + screenX;
//        double aggroScreenY = aggroRange.getY() - worldY + screenY;
//        g2.setColor(new Color(0, 0, 255, 50)); // Blue, very transparent
//        g2.drawOval((int) aggroScreenX, (int) aggroScreenY, (int) aggroRange.getWidth(), (int) aggroRange.getHeight());
    }

    // --- UI Drawing Methods (Hearts, HP) ---
//    public void drawPlayerHeart(Graphics2D g2) {
//        // Keep your existing heart drawing logic here
//        int x = gamePanel.getTileSize() / 100;
//        int y = gamePanel.getTileSize() / 100;
//        int heartSize = gamePanel.getTileSize(); // Assuming heart images are tile sized
//
//        // Draw blank hearts up to maxLife
//        for (int i = 0; i < maxLife / 100; i++) {
//            g2.drawImage(heart_blank, x + (i * heartSize), y, heartSize, heartSize, null);
//        }
//        // Draw filled hearts based on current life
//        int fullHearts = life / 100;
//        int halfHearts = life % 100;
//        for (int i = 0; i < fullHearts; i++) {
//            g2.drawImage(heart_full, x + (i * heartSize), y, heartSize, heartSize, null);
//        }
//        // Draw half heart if needed
//        if (halfHearts > 0) {
//            g2.drawImage(heart_half, x + (fullHearts * heartSize), y, heartSize, heartSize, null);
//        }
//    }

    private void drawHPBar(Graphics2D g2, int screenX, int screenY) {
        // Don't draw HP bar if Orc has full health (optional clutter reduction)
        // if (life == maxLife) return;

        // Set font (use preloaded or fallback)
        if (playerHpFont != null) {
            g2.setFont(playerHpFont);
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
            playerHpFont = customFont.deriveFont(Font.BOLD, 16F); // Set size and style
            // System.out.println("Custom font loaded successfully."); // Debug

        } catch (FontFormatException | IOException e) {
            System.err.println("Error loading custom font for Orc HP: " + e.getMessage() + ". Using default.");
            // Use a standard system font as a fallback
            playerHpFont = new Font("Arial", Font.BOLD, 16);
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

    public void drawPlayerHP(Graphics2D g2) {
        // Keep your font loading logic, add error handling
        try {
            File customFontFile = new File("res/fonts/medieval-font.ttf"); // Ensure path is correct relative to project root
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, customFontFile).deriveFont(Font.BOLD, 20F);
            g2.setFont(customFont);
        } catch (FontFormatException | IOException e) {
            System.err.println("Error loading custom font: " + e.getMessage() + ". Using default font.");
            g2.setFont(new Font("Arial", Font.BOLD, 20)); // Fallback font
        }

        g2.setColor(Color.WHITE);
        String hpText = String.format("HP %.2f / %.2f", life, maxLife);
        // Position relative to screen, perhaps near hearts or corner
        int textX = gamePanel.getTileSize() / 2; // Align with hearts start
        int textY = gamePanel.getTileSize(); // Position below hearts (adjust as needed)
        // Add a shadow for readability
        g2.setColor(Color.BLACK);
        g2.drawString(hpText, textX+1, textY+1);
        g2.setColor(Color.WHITE);
        g2.drawString(hpText, textX, textY);
    }

    // --- Image Loading ---
    public void getPlayerImage() {
        System.out.println("Loading player images..."); // Debug
        try {
            idleDown[0] = loadImage("/player/Walk(Down)/1.png");
            idleUp[0] = loadImage("/player/Walk(Up)/1.png");
            idleLeft[0] = loadImage("/player/Walk(Left)/1.png");
            idleRight[0] = loadImage("/player/Walk(Right)/1.png");

            for (int i = 0; i < 6; i++) walkDown[i] = loadImage("/player/Walk(Down)/" + (i + 1) + ".png");
            for (int i = 0; i < 8; i++) walkLeft[i] = loadImage("/player/Walk(Left)/" + (i + 1) + ".png");
            for (int i = 0; i < 8; i++) walkUp[i] = loadImage("/player/Walk(Up)/" + (i + 1) + ".png");
            for (int i = 0; i < 8; i++) walkRight[i] = loadImage("/player/Walk(Right)/" + (i + 1) + ".png");
            for (int i = 0; i < 5; i++) AttackFront[i] = loadImage("/player/Attack(Down)/" + (i + 1) + ".png");
            for (int i = 0; i < 5; i++) AttackLeft[i] = loadImage("/player/Attack(Left)/" + (i + 1) + ".png");
            for (int i = 0; i < 5; i++) AttackRight[i] = loadImage("/player/Attack(Right)/" + (i + 1) + ".png");
            for (int i = 0; i < 5; i++) AttackUp[i] = loadImage("/player/Attack(Up)/" + (i + 1) + ".png");
            System.out.println("Player images loaded successfully."); // Debug

        } catch (IOException e) {
            System.err.println("FATAL ERROR loading player images: " + e.getMessage());
            e.printStackTrace();
            // Handle this critical error - maybe exit or use placeholders
            // System.exit(1);
        } catch (NullPointerException e) {
            System.err.println("FATAL ERROR: Could not find player image resource path. Check paths starting with '/' are correct relative to the resources folder.");
            e.printStackTrace();
            // System.exit(1);
        }
    }

    // Helper method for loading images to reduce repetition and add error checking
    private BufferedImage loadImage(String path) throws IOException, NullPointerException {
        // System.out.println("Loading: " + path); // Debug path issues
        java.io.InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            throw new NullPointerException("Resource not found: " + path);
        }
        BufferedImage img = ImageIO.read(stream);
        if (img == null) {
            throw new IOException("ImageIO.read returned null for path: " + path);
        }
        // System.out.println("Loaded: " + path); // Debug path issues
        return img;
    }

    // --- Getters ---
    public String getCurrentDirection(){
        // Returns the current movement direction (or idle)
        return direction;
    }

    public String getFacingDirection(){
        // Returns the direction the player is facing (last moved direction)
        return prevDirection;
    }
}