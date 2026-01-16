package src.entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import src.main.GamePanel;
import src.main.KeyHandler;

public class Player extends Entity {

    public boolean hasSword = false;
    public int attackPoints = 0;
    // Map sword positions to replacement tile numbers (col, row) -> tileNum
    // Example: {(10, 12): 0, (15, 20): 5} means sword at (10,12) becomes grass (0), at (15,20) becomes sand (5)
    // Fill this map as needed for your swords
    public java.util.Map<String, Integer> swordReplacementTiles = new java.util.HashMap<>();

    GamePanel gp;
    KeyHandler keyH;

    public final int screenX;
    public final int screenY;

    // Player images
    public BufferedImage up0, up1, up2;
    public BufferedImage down0, down1, down2;
    public BufferedImage left0, left1, left2;
    public BufferedImage right0, right1, right2;

    // Animation counters
    public int spriteCounter = 0;
    public int spriteNum = 0;

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;

        screenX = gp.screenWidth / 2 - (gp.tileSize / 2);
        screenY = gp.screenHeight / 2 - (gp.tileSize / 2);

        solidArea = new Rectangle();
        solidArea.x = 8;
        solidArea.y = 16;
        solidArea.width = gp.tileSize - (solidArea.x * 2); // 32
        solidArea.height = gp.tileSize - solidArea.y; // 32

        setDefaultValues();
        getPlayerImage();

        // Example: set up sword replacement mapping (col, row) -> tileNum
        // You must fill this with the actual positions and replacement tile numbers you want
        // swordReplacementTiles.put("col,row", tileNum);
        // Example: swordReplacementTiles.put("10,12", 0); // sword at (10,12) becomes grass (0)
    }

    // get the images of the player and put it in a variable
    // example up from boy_up_0.png
    // using ImageIO.read()
    public final void getPlayerImage() {
        try {
            String[] paths = {
                "/res/player/p0/boy_up_0.png",
                "/res/player/p0/boy_up_1.png",
                "/res/player/p0/boy_up_2.png",
                "/res/player/p0/boy_down_0.png",
                "/res/player/p0/boy_down_1.png",
                "/res/player/p0/boy_down_2.png",
                "/res/player/p0/boy_left_1.png",
                "/res/player/p0/boy_left_2.png",
                "/res/player/p0/boy_right_1.png",
                "/res/player/p0/boy_right_2.png"
            };
            for (String path : paths) {
                if (getClass().getResourceAsStream(path) == null) {
                    System.out.println("Resource not found: " + path);
                }
            }
            up0    = ImageIO.read(getClass().getResourceAsStream("/res/player/p0/boy_up_0.png"));
            up1    = ImageIO.read(getClass().getResourceAsStream("/res/player/p0/boy_up_1.png"));
            up2    = ImageIO.read(getClass().getResourceAsStream("/res/player/p0/boy_up_2.png"));

            down0  = ImageIO.read(getClass().getResourceAsStream("/res/player/p0/boy_down_0.png"));
            down1  = ImageIO.read(getClass().getResourceAsStream("/res/player/p0/boy_down_1.png"));
            down2  = ImageIO.read(getClass().getResourceAsStream("/res/player/p0/boy_down_2.png"));

            left0  = ImageIO.read(getClass().getResourceAsStream("/res/player/p0/boy_left_1.png")); // fallback to left_1
            left1  = ImageIO.read(getClass().getResourceAsStream("/res/player/p0/boy_left_1.png"));
            left2  = ImageIO.read(getClass().getResourceAsStream("/res/player/p0/boy_left_2.png"));

            right0 = ImageIO.read(getClass().getResourceAsStream("/res/player/p0/boy_right_1.png")); // fallback to right_1
            right1 = ImageIO.read(getClass().getResourceAsStream("/res/player/p0/boy_right_1.png"));
            right2 = ImageIO.read(getClass().getResourceAsStream("/res/player/p0/boy_right_2.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // the default value of the speed, the location and the direction the player is
    // facing
    public final void setDefaultValues() {
        worldX = gp.tileSize * 23;
        worldY = gp.tileSize * 21;
        speed = 4;
        direction = "down";
    }

    // if a key is pressed change the direction, check the collision if he can move,
    // if he can add or substract the speed from his coordonates and select what
    // image to use to maka an animation
    public void update() {
        boolean moving = false;

        if (keyH.upPressed) {
            direction = "up";
            moving = true;
        } else if (keyH.downPressed) {
            direction = "down";
            moving = true;
        } else if (keyH.leftPressed) {
            direction = "left";
            moving = true;
        } else if (keyH.rightPressed) {
            direction = "right";
            moving = true;
        }

        collisionOn = false;
        gp.cChecker.checkTile(this);

        // Sword pickup logic (after collision check, before movement)
        int playerCol = (worldX + solidArea.x + solidArea.width / 2) / gp.tileSize;
        int playerRow = (worldY + solidArea.y + solidArea.height / 2) / gp.tileSize;
        int tileNum = gp.tileM.mapTileNum[playerCol][playerRow];
        if (!hasSword && tileNum == 6) { // 6 = sword tile
            hasSword = true;
            attackPoints = 1;
            // Find replacement tile for this sword position
            String key = playerCol + "," + playerRow;
            int replacement = swordReplacementTiles.getOrDefault(key, 0); // default to grass (0)
            gp.tileM.mapTileNum[playerCol][playerRow] = replacement;
            // Optionally: print or trigger sound/message here
        }

        // Move player if no collision
        if (moving && !collisionOn) {
            switch (direction) {
                case "up" -> worldY -= speed;
                case "down" -> worldY += speed;
                case "left" -> worldX -= speed;
                case "right" -> worldX += speed;
            }

            // Update sprite for simple animation
            spriteCounter++;
            if (spriteCounter > 12) { // change every 12 frames
                spriteNum = (spriteNum + 1) % 3; // cycles 0,1,2
                spriteCounter = 0;
            }
        } else {
            spriteNum = 0; // idle sprite
        }
    }



    public void draw(Graphics2D g2) {
        BufferedImage image = null;

        switch (direction) {
            case "up" -> {
                if (spriteNum == 0)
                    image = up0;
                if (spriteNum == 1)
                    image = up1;
                if (spriteNum == 2)
                    image = up2;
            }
            case "down" -> {
                if (spriteNum == 0)
                    image = down0;
                if (spriteNum == 1)
                    image = down1;
                if (spriteNum == 2)
                    image = down2;
            }
            case "left" -> {
                if (spriteNum == 0)
                    image = left1;
                if (spriteNum == 1)
                    image = left1;
                if (spriteNum == 2)
                    image = left2;
            }
            case "right" -> {
                if (spriteNum == 0)
                    image = right1;
                if (spriteNum == 1)
                    image = right1;
                if (spriteNum == 2)
                    image = right2;
            }
        }
        g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
    }
}
