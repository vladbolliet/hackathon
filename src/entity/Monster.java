package src.entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import src.main.GamePanel;

public class Monster extends Entity {
    GamePanel gp;

    public Monster(GamePanel gp) {
        this.gp = gp;
        // Initialize solid area (same layout as Player) to allow collision checks
        solidArea = new Rectangle();
        solidArea.x = 8;
        solidArea.y = 16;
        solidArea.width = gp.tileSize - (solidArea.x * 2);
        solidArea.height = gp.tileSize - solidArea.y;
        setDefaultValues();
        getMonsterImage();
    }

    public void setDefaultValues() {
        worldX = gp.tileSize * 25;
        worldY = gp.tileSize * 21;
        speed = 2;
        direction = "down";
    }

    // Array to hold the 8 right-facing walking sprites
    private BufferedImage[] rightWalk = new BufferedImage[8];

    // AI state
    private String state = "PATROL"; // PATROL or ATTACK
    private final int detectionRadiusTiles = 5; // tiles
    private int detectionRadiusPixels;

    // Patrol parameters
    private int patrolPixelsRemaining = 0;
    private long pauseEndTime = 0L;
    private final long patrolPauseMs = 2000L; // 2 seconds
    private int baseSpeed = 2;
    // Fallback movement when blocked during ATTACK
    private String fallbackDirection = null;
    private int fallbackPixelsRemaining = 0;
    // Combat / hit tracking
    private int hitsTaken = 0;
    private final int maxHits = 5;
    private boolean alive = true;
    private final long hitCooldownMs = 300L;
    private long lastHitTime = 0L;
    // Knockback pause
    private final long knockbackPauseMs = 1000L; // 1 second
    private long knockbackPauseEndTime = 0L;

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
        if (!alive) return;

        // Ensure detection radius is computed (gp available here)
        detectionRadiusPixels = detectionRadiusTiles * gp.tileSize;

        // Decide state based on player distance
        int dx = gp.player.worldX - worldX;
        int dy = gp.player.worldY - worldY;
        double dist = Math.hypot(dx, dy);
        state = dist <= detectionRadiusPixels ? "ATTACK" : "PATROL";

        // Remember base speed and clamp
        baseSpeed = Math.max(1, baseSpeed);

        long now = System.currentTimeMillis();
        // If in knockback pause, do not move or change state; still accept hits
        if (now < knockbackPauseEndTime) {
            handlePlayerCollision();
            return;
        }

        if (state.equals("ATTACK")) {
            handleAttack(dx, dy);
        } else {
            handlePatrol();
        }

        // Check for player hitting monster with sword
        handlePlayerCollision();

        // bounce at world edge
        if (worldY > gp.maxWorldHeight - gp.tileSize) direction = "up";
        if (worldY < 0) direction = "down";
        if (worldX > gp.maxWorldWidth - gp.tileSize) direction = "left";
        if (worldX < 0) direction = "right";

    }

    public void draw(Graphics2D g2) {
        if (!alive) return;

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

    // ----- Helper methods (modular behavior) -----
    private boolean isNextTileBlocked(String testDir) {
        String oldDir = direction;
        direction = testDir;
        collisionOn = false;
        gp.cChecker.checkTile(this);
        boolean blocked = collisionOn;
        direction = oldDir;
        return blocked;
    }

    private void moveInDirection(String dir, int pixels) {
        switch (dir) {
            case "down" -> worldY += pixels;
            case "up" -> worldY -= pixels;
            case "left" -> worldX -= pixels;
            case "right" -> worldX += pixels;
        }
    }

    private void startPatrolSegment() {
        int steps = 1 + (int)(Math.random() * 3); // 1-3 tiles
        patrolPixelsRemaining = steps * gp.tileSize;
        java.util.List<String> dirs = java.util.Arrays.asList("up", "down", "left", "right");
        direction = dirs.get((int)(Math.random() * dirs.size()));
    }

    private void handlePatrol() {
        speed = baseSpeed;
        long now = System.currentTimeMillis();
        if (patrolPixelsRemaining <= 0) {
            if (now < pauseEndTime) {
                return; // still pausing
            }
            startPatrolSegment();
        }

        if (patrolPixelsRemaining > 0) {
            if (isNextTileBlocked(direction)) {
                // stop and pause
                patrolPixelsRemaining = 0;
                pauseEndTime = System.currentTimeMillis() + patrolPauseMs;
                return;
            }
            int move = Math.min(speed, patrolPixelsRemaining);
            moveInDirection(direction, move);
            patrolPixelsRemaining -= move;
            if (patrolPixelsRemaining <= 0) {
                pauseEndTime = System.currentTimeMillis() + patrolPauseMs;
            }
        }
    }

    private void handleAttack(int dx, int dy) {
        speed = baseSpeed + 1;

        // If executing fallback, continue it
        if (fallbackPixelsRemaining > 0 && fallbackDirection != null) {
            if (!isNextTileBlocked(fallbackDirection)) {
                int move = Math.min(speed, fallbackPixelsRemaining);
                moveInDirection(fallbackDirection, move);
                fallbackPixelsRemaining -= move;
                if (fallbackPixelsRemaining <= 0) fallbackDirection = null;
                return;
            } else {
                // cancel fallback and continue with normal logic
                fallbackPixelsRemaining = 0;
                fallbackDirection = null;
            }
        }

        // choose greedy direction
        String primary = Math.abs(dx) > Math.abs(dy) ? (dx > 0 ? "right" : "left") : (dy > 0 ? "down" : "up");
        direction = primary;
        if (!isNextTileBlocked(direction)) {
            moveInDirection(direction, speed);
            return;
        }

        // attempt perpendicular directions persistently
        java.util.List<String> alt = new java.util.ArrayList<>();
        if ("left".equals(direction) || "right".equals(direction)) {
            alt.add("up");
            alt.add("down");
        } else {
            alt.add("left");
            alt.add("right");
        }
        for (String d : alt) {
            if (!isNextTileBlocked(d)) {
                // set fallback to attempt 2 tiles of sidestep
                fallbackDirection = d;
                fallbackPixelsRemaining = gp.tileSize * 2;
                return;
            }
        }

        // try backoff
        String opposite = switch (direction) {
            case "up" -> "down";
            case "down" -> "up";
            case "left" -> "right";
            default -> "left";
        };
        if (!isNextTileBlocked(opposite)) {
            fallbackDirection = opposite;
            fallbackPixelsRemaining = gp.tileSize; // back off one tile
        }
        // else all blocked: do nothing this frame
    }

    // Check if a candidate world position would collide with collidable tiles
    private boolean canMoveTo(int candidateWorldX, int candidateWorldY) {
        int entityLeftWorldX = candidateWorldX + solidArea.x;
        int entityRightWorldX = candidateWorldX + solidArea.x + solidArea.width;
        int entityTopWorldY = candidateWorldY + solidArea.y;
        int entityBottomWorldY = candidateWorldY + solidArea.y + solidArea.height;

        int tileSize = gp.tileSize;
        int leftCol = entityLeftWorldX / tileSize;
        int rightCol = entityRightWorldX / tileSize;
        int topRow = entityTopWorldY / tileSize;
        int bottomRow = entityBottomWorldY / tileSize;

        // Bounds check
        if (leftCol < 0 || rightCol >= gp.maxWorldCol || topRow < 0 || bottomRow >= gp.maxWorldRow) {
            return false; // treat out of bounds as non-walkable for knockback purposes
        }

        int tileNum1 = gp.tileM.mapTileNum[leftCol][topRow];
        int tileNum2 = gp.tileM.mapTileNum[rightCol][topRow];
        int tileNum3 = gp.tileM.mapTileNum[leftCol][bottomRow];
        int tileNum4 = gp.tileM.mapTileNum[rightCol][bottomRow];

        return !(gp.tileM.tile[tileNum1].collision || gp.tileM.tile[tileNum2].collision || gp.tileM.tile[tileNum3].collision || gp.tileM.tile[tileNum4].collision);
    }

    private void handlePlayerCollision() {
        if (!gp.player.hasSword) return;
        long now = System.currentTimeMillis();
        if (now - lastHitTime < hitCooldownMs) return;

        // compute world rectangles
        Rectangle pRect = new Rectangle(gp.player.worldX + gp.player.solidArea.x, gp.player.worldY + gp.player.solidArea.y, gp.player.solidArea.width, gp.player.solidArea.height);
        Rectangle mRect = new Rectangle(worldX + solidArea.x, worldY + solidArea.y, solidArea.width, solidArea.height);
        if (pRect.intersects(mRect)) {
            // apply hit
            hitsTaken += 1;
            lastHitTime = now;
            // compute knockback direction away from player
            int kdx = worldX - gp.player.worldX;
            int kdy = worldY - gp.player.worldY;
            String kbDir;
            if (Math.abs(kdx) > Math.abs(kdy)) kbDir = kdx > 0 ? "right" : "left";
            else kbDir = kdy > 0 ? "down" : "up";

            applyKnockback(kbDir, 2); // 2 tiles
            // Start knockback pause
            knockbackPauseEndTime = now + knockbackPauseMs;

            if (hitsTaken >= maxHits) {
                die();
            }
        }
    }

    private void applyKnockback(String kbDir, int tiles) {
        int tileSize = gp.tileSize;
        for (int i = 1; i <= tiles; i++) {
            int candX = worldX;
            int candY = worldY;
            switch (kbDir) {
                case "down" -> candY = worldY + i * tileSize;
                case "up" -> candY = worldY - i * tileSize;
                case "left" -> candX = worldX - i * tileSize;
                case "right" -> candX = worldX + i * tileSize;
            }
            if (canMoveTo(candX, candY)) {
                // move to this candidate (the furthest possible)
                worldX = candX;
                worldY = candY;
            } else {
                break; // stop at first blocked
            }
        }
    }

    private void die() {
        alive = false;
        // Optionally: play death animation or drop loot; for now, stop updating/drawing
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
