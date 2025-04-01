package Main;

import Entity.Entity;
import java.awt.*;

public class CollisionChecker {
    GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    public void checkCollision(Entity entity) {
        entity.collisionOn = false; // Reset collision flag

        // Create entity's next position bounding box
        Rectangle entityFutureBox = new Rectangle(
                entity.worldX + entity.solidArea.x,
                entity.worldY + entity.solidArea.y,
                entity.solidArea.width,
                entity.solidArea.height
        );

        // Adjust future position based on movement direction
        switch (entity.direction) {
            case "up":
                entityFutureBox.y -= entity.speed;
                break;
            case "down":
                entityFutureBox.y += entity.speed;
                break;
            case "left":
                entityFutureBox.x -= entity.speed;
                break;
            case "right":
                entityFutureBox.x += entity.speed;
                break;
        }

        // Check for collision with map's collision boxes
        for (Rectangle box : gp.tileManager.collisionBoxes) {
            if (entityFutureBox.intersects(box)) {
                entity.collisionOn = true;
                return; // Stop checking once a collision is detected
            }
        }
    }
}
