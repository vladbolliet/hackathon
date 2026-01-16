package src.entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import src.main.GamePanel;

public class Monster extends Entity {
    GamePanel gp;

    public Monster(GamePanel gp) {
        this.gp = gp;
        setDefaultValues();
        getMonsterImage();
    }

    public void setDefaultValues() {
        worldX = gp.tileSize * 23;
        worldY = gp.tileSize * 21;
        speed = 2;
        direction = "down";
    }

    // Array to hold the 8 right-facing walking sprites
    private BufferedImage[] rightWalk = new BufferedImage[8];

    public void getMonsterImage() {
        try {
            BufferedImage sheet = ImageIO.read(getClass().getResourceAsStream("/res/monster/goblin/goblin_spritesheet.png"));
            // Provided coordinates for 8 right-facing sprites
            rightWalk[0] = sheet.getSubimage(11, 118, 36-11+1, 142-118+1);
            rightWalk[1] = sheet.getSubimage(93, 119, 116-93+1, 142-119+1);
            rightWalk[2] = sheet.getSubimage(173, 118, 195-173+1, 142-118+1);
            rightWalk[3] = sheet.getSubimage(255, 118, 276-255+1, 142-118+1);
            rightWalk[4] = sheet.getSubimage(336, 118, 356-336+1, 142-118+1);
            rightWalk[5] = sheet.getSubimage(416, 119, 436-416+1, 142-119+1);
            rightWalk[6] = sheet.getSubimage(493, 118, 516-493+1, 142-118+1);
            rightWalk[7] = sheet.getSubimage(571, 117, 596-571+1, 142-117+1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update() {
        // Simple AI: move down, bounce at map edge
        switch (direction) {
            case "down" -> worldY += speed;
            case "up" -> worldY -= speed;
            case "left" -> worldX -= speed;
            case "right" -> worldX += speed;
        }
        // Example: bounce at world edge
        if (worldY > gp.maxWorldHeight - gp.tileSize) direction = "up";
        if (worldY < 0) direction = "down";
        if (worldX > gp.maxWorldWidth - gp.tileSize) direction = "left";
        if (worldX < 0) direction = "right";
    }

    public void draw(Graphics2D g2) {
        // Use spriteCounter to animate (cycle through 8 frames)
        int frame = (spriteCounter / 8) % 8; // adjust divisor for speed
        BufferedImage image = rightWalk[frame];
        boolean flip = false;
        if ("left".equals(direction)) {
            flip = true;
        }
        // For up/down, use right sprite (no flip)
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;
        if (flip) {
            // Flip horizontally for left direction
            g2.drawImage(flipImage(image), screenX, screenY, gp.tileSize, gp.tileSize, null);
        } else {
            g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
        }
        // Animate
        spriteCounter = (spriteCounter + 1) % 64;
    }

    // Utility to flip a BufferedImage horizontally
    private BufferedImage flipImage(BufferedImage img) {
        BufferedImage flipped = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        java.awt.Graphics2D g = flipped.createGraphics();
        g.drawImage(img, img.getWidth(), 0, -img.getWidth(), img.getHeight(), null);
        g.dispose();
        return flipped;
    }
}
