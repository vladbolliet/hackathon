package src.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import src.entity.Player;
import src.tile.TileManager;

public class GamePanel extends JPanel implements Runnable {
        // Monster instance
        public src.entity.Monster monster = new src.entity.Monster(this);
    // UI inventory bar image
    private java.awt.image.BufferedImage uiBarImage;
    // Sword icon for inventory
    private java.awt.image.BufferedImage swordIcon;
    // SCREEN SETTINGS
    final int originalTitleSize = 16; // 16 * 16, tile or size of the sprite
    final int scale = 3; // scale for the tile to make it bigger or smaller

    public final int tileSize = originalTitleSize * scale; // 48*48, size of the tile on the screen
    public final int maxScreenCol = 16;
    public final int maxScreenRow = 12;
    // screen is 16 * 12 so a ration of 4 * 3
    public final int screenWidth = tileSize * maxScreenCol; // 768 pixels
    public final int screenHeight = tileSize * maxScreenRow; // 576 pixels

    // WORLD SETTINGS
    public final int maxWorldCol = 50;
    public final int maxWorldRow = 50;
    public final int maxWorldWidth = tileSize * maxWorldCol;
    public final int maxWorldHeight = tileSize * maxWorldRow;

    // FPS
    int FPS = 60;

    public TileManager tileM = new TileManager(this);
    KeyHandler keyH = new KeyHandler();
    Thread gamThread;
    public CollisionChecker cChecker = new CollisionChecker(this);
    public Player player = new Player(this, keyH);

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight)); // preffered size for the window
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true); // the gamepanel can be focused on to read key input
        // Load sword icon (replace with your actual image path)
        try {
            swordIcon = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/res/items/swords.png")).getSubimage(0, 0, 16, 16);
        } catch (Exception e) {
            System.out.println("Could not load sword icon: /res/items/swords.png");
        }
    }

    public void startGameThread() {

        gamThread = new Thread(this);
        gamThread.start();
    }


    //draw and update at the fps rate with System.nanoTime() to get the current time
    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS; // nanoseconds per frame
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (true) {
            // 1. Update game state
            update();

            // 2. Repaint the screen
            repaint();

            // 3. Sleep until the next frame
            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime = remainingTime / 1_000_000; // convert to milliseconds

                if (remainingTime < 0) {
                    remainingTime = 0; // no negative sleep
                }

                Thread.sleep((long) remainingTime);

                nextDrawTime += drawInterval; // schedule next frame
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void update() {
        player.update();
        monster.update();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        tileM.draw(g2);
        player.draw(g2);

        // Draw monster after map, before UI
        monster.draw(g2);


        // Draw only the sword icon at the bottom center if player has sword, scaled up
        int gap = 16; // pixels of gap from the bottom
        if (player.hasSword && swordIcon != null) {
            int scale = 3; // scale factor for inventory icon
            int iconW = swordIcon.getWidth() * scale;
            int iconH = swordIcon.getHeight() * scale;
            int iconX = (screenWidth - iconW) / 2;
            int iconY = screenHeight - iconH - gap;
            g2.drawImage(swordIcon, iconX, iconY, iconW, iconH, null);
        }

        g2.dispose();
    }
}

