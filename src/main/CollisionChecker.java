package src.main;

import src.entity.Entity;

public class CollisionChecker {

    GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    // check the collison when the direction is up/down/left/right
    // get the world coordonate of the player from top,down,left,right
    // get the col and row of the screen where the player is
    // check the collision between the player the th 2 blocks close to it relatively
    // to the direction
    public void checkTile(Entity entity) {
        int entityLeftWorldX = entity.worldX + entity.solidArea.x;
        int entityRightWorldX = entity.worldX + entity.solidArea.x + entity.solidArea.width;
        int entityTopWorldY = entity.worldY + entity.solidArea.y;
        int entityBottomWorldY = entity.worldY + entity.solidArea.y + entity.solidArea.height;

        int tileSize = gp.tileSize;

        int leftCol, rightCol, topRow, bottomRow;
        int tileNum1, tileNum2;

        switch (entity.direction) {
            case "up" -> {
                topRow = (entityTopWorldY - entity.speed) / tileSize;
                leftCol = entityLeftWorldX / tileSize;
                rightCol = entityRightWorldX / tileSize;
                tileNum1 = gp.tileM.mapTileNum[leftCol][topRow];
                tileNum2 = gp.tileM.mapTileNum[rightCol][topRow];
                if (gp.tileM.tile[tileNum1].collision || gp.tileM.tile[tileNum2].collision) {
                    entity.collisionOn = true;
                }
            }
            case "down" -> {
                bottomRow = (entityBottomWorldY + entity.speed) / tileSize;
                leftCol = entityLeftWorldX / tileSize;
                rightCol = entityRightWorldX / tileSize;
                tileNum1 = gp.tileM.mapTileNum[leftCol][bottomRow];
                tileNum2 = gp.tileM.mapTileNum[rightCol][bottomRow];
                if (gp.tileM.tile[tileNum1].collision || gp.tileM.tile[tileNum2].collision) {
                    entity.collisionOn = true;
                }
            }
            case "left" -> {
                leftCol = (entityLeftWorldX - entity.speed) / tileSize;
                topRow = entityTopWorldY / tileSize;
                bottomRow = entityBottomWorldY / tileSize;
                tileNum1 = gp.tileM.mapTileNum[leftCol][topRow];
                tileNum2 = gp.tileM.mapTileNum[leftCol][bottomRow];
                if (gp.tileM.tile[tileNum1].collision || gp.tileM.tile[tileNum2].collision) {
                    entity.collisionOn = true;
                }
            }
            case "right" -> {
                rightCol = (entityRightWorldX + entity.speed) / tileSize;
                topRow = entityTopWorldY / tileSize;
                bottomRow = entityBottomWorldY / tileSize;
                tileNum1 = gp.tileM.mapTileNum[rightCol][topRow];
                tileNum2 = gp.tileM.mapTileNum[rightCol][bottomRow];
                if (gp.tileM.tile[tileNum1].collision || gp.tileM.tile[tileNum2].collision) {
                    entity.collisionOn = true;
                }
            }
        }
    }
}
