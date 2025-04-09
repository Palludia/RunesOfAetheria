package Main;

import Entity.Entity;
import java.awt.*;
import java.util.ArrayList;

public class CollisionChecker {
    GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    public void checkCollision(Entity entity) {
        entity.collisionOn = false; // Reset collision flag

        int futureX = entity.worldX;
        int futureY = entity.worldY;

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

    public void checkForMonster(Entity entity) {
        ArrayList<Entity> entityList = new ArrayList<>();
        entityList.add(gp.BOD);
        entityList.addAll(gp.orcs);

        for(int i = 0; i < entityList.size(); i++) {
            if(entityList.get(i) != null) {
                entity.solidArea.x = entity.worldX + entity.solidArea.x;
                entity.solidArea.y = entity.worldY + entity.solidArea.y;

                entityList.get(i).solidArea.x = entityList.get(i).worldX + entityList.get(i).solidArea.x;
                entityList.get(i).solidArea.y = entityList.get(i).worldY + entityList.get(i).solidArea.y;

                switch(entity.direction) {
                    case "up":
                        entity.solidArea.y -= entity.speed;
                        if(entity.solidArea.intersects(entityList.get(i).solidArea)) {
                            if(entityList.get(i).getCollision()) {
                                entity.collisionOn = true;
                            }
                        }break;
                    case "down":
                        entity.solidArea.y += entity.speed;
                        if(entity.solidArea.intersects(entityList.get(i).solidArea)) {
                            if(entityList.get(i).getCollision()) {
                                entity.collisionOn = true;
                            }
                        }break;
                    case "left":
                        entity.solidArea.x -= entity.speed;
                        if(entity.solidArea.intersects(entityList.get(i).solidArea)) {
                            if(entityList.get(i).getCollision()) {
                                entity.collisionOn = true;
                            }
                        }break;
                    case "right":
                        entity.solidArea.x += entity.speed;
                        if(entity.solidArea.intersects(entityList.get(i).solidArea)) {
                            if(entityList.get(i).getCollision()) {
                                entity.collisionOn = true;
                            }
                        }break;
                }
                entity.solidArea.x = entity.solidAreaX;
                entity.solidArea.y = entity.solidAreaY;

                entityList.get(i).solidArea.x = entityList.get(i).solidAreaX;
                entityList.get(i).solidArea.y = entityList.get(i).solidAreaY;
            }
        }
    }

    public void checkForPlayer(Entity entity) {
        ArrayList<Entity> entityList = new ArrayList<>();
        entityList.add(gp.BOD);
        entityList.addAll(gp.orcs);

        for (Entity value : entityList) {
            if (value != null) {
                entity.solidArea.x = entity.worldX + entity.solidArea.x;
                entity.solidArea.y = entity.worldY + entity.solidArea.y;

                gp.player.solidArea.x = gp.player.worldX + gp.player.solidArea.x;
                gp.player.solidArea.y = gp.player.worldY + gp.player.solidArea.y;

                switch (entity.direction) {
                    case "up":
                        entity.solidArea.y -= entity.speed;
                        if (entity.solidArea.intersects(gp.player.solidArea)) {
                            entity.collisionOn = true;
                        }
                        break;
                    case "down":
                        entity.solidArea.y += entity.speed;
                        if (entity.solidArea.intersects(gp.player.solidArea)) {
                            entity.collisionOn = true;
                        }
                        break;
                    case "left":
                        entity.solidArea.x -= entity.speed;
                        if (entity.solidArea.intersects(gp.player.solidArea)) {
                            entity.collisionOn = true;
                        }
                        break;
                    case "right":
                        entity.solidArea.x += entity.speed;
                        if (entity.solidArea.intersects(gp.player.solidArea)) {
                            entity.collisionOn = true;
                        }
                        break;
                }
                entity.solidArea.x = entity.solidAreaX;
                entity.solidArea.y = entity.solidAreaY;

                gp.player.solidArea.x = gp.player.solidAreaX;
                gp.player.solidArea.y = gp.player.solidAreaY;
            }
        }
    }

    public boolean isTileBlocked(int col, int row) {
        int tileSize = gp.getTileSize();
        Rectangle tileBox = new Rectangle(col * tileSize, row * tileSize, tileSize, tileSize);

        for (Rectangle box : gp.tileManager.collisionBoxes) {
            if (tileBox.intersects(box)) {
                return true;
            }
        }

        return false;
    }

}
