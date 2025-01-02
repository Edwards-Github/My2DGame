package main;

import entity.Entity;
import entity.Player;
import main.KeyHandler;
import object.SuperObject;
import tile.TileManager;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class GamePanel extends JPanel implements Runnable {

    // ------------------------------------------------------------------------
    // SCREEN SETTINGS
    // ------------------------------------------------------------------------
    private static final int ORIGINAL_TILE_SIZE = 16; // 16x16 tile
    private static final int SCALE = 3;               // scale image to be bigger or smaller

    /** The size of a tile in pixels, after scaling. */
    public final int tileSize = ORIGINAL_TILE_SIZE * SCALE; // => 48x48

    /** Number of columns and rows on the screen in tile units. */
    public final int maxScreenCol = 16;
    public final int maxScreenRow = 12;

    /** Actual screen pixel dimensions. */
    public final int screenWidth  = tileSize * maxScreenCol; // => 768 px
    public final int screenHeight = tileSize * maxScreenRow; // => 576 px

    // ------------------------------------------------------------------------
    // WORLD SETTINGS
    // ------------------------------------------------------------------------
    public final int maxWorldCol = 50;
    public final int maxWorldRow = 50;
    // Potential future expansions: multi-layer maps, dynamic loading, chunk-based systems

    // ------------------------------------------------------------------------
    // FPS
    // ------------------------------------------------------------------------
    private static final int FPS = 60;

    // ------------------------------------------------------------------------
    // SYSTEM CLASSES
    // ------------------------------------------------------------------------
    private Thread gameThread;
    public final KeyHandler keyH = new KeyHandler(this);

    public final TileManager tileM = new TileManager(this);
    public final Sound music = new Sound();
    public final Sound se = new Sound(); // sound effects
    public final CollisionChecker cChecker = new CollisionChecker(this);
    public final AssetSetter aSetter = new AssetSetter(this);
    public final UI ui = new UI(this);
    public final main.EventHandler eHandler = new main.EventHandler(this);

    // ------------------------------------------------------------------------
    // ENTITIES & OBJECTS
    // ------------------------------------------------------------------------
    public final Player player = new Player(this, keyH);

    /** Array for interactive objects (like chests, pickups, or obstacles). */
    public SuperObject[] obj = new SuperObject[10];

    /** Array for NPCs or other moving characters. */
    public Entity[] npc = new Entity[10];

    // ------------------------------------------------------------------------
    // GAME STATES
    // ------------------------------------------------------------------------
    public int gameState;
    public static final int TITLE_STATE    = 0;
    public static final int PLAY_STATE     = 1;
    public static final int PAUSE_STATE    = 2;
    public static final int DIALOGUE_STATE = 3;

    // ------------------------------------------------------------------------
    // CONSTRUCTOR
    // ------------------------------------------------------------------------
    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);  // improves rendering performance
        this.addKeyListener(keyH);
        this.setFocusable(true);       // allows the panel to receive focus for key events
    }

    // ------------------------------------------------------------------------
    // INIT METHODS
    // ------------------------------------------------------------------------
    public void setupGame() {
        aSetter.setObject();
        aSetter.setNPC();
        // Optionally start background music here
        // playMusic(0);
        gameState = TITLE_STATE;
    }

    public void startGameThread() {
        if (gameThread == null) {
            gameThread = new Thread(this, "GameLoopThread");
            gameThread.start();
        }
    }

    // ------------------------------------------------------------------------
    // CORE GAME LOOP (Runnable)
    // ------------------------------------------------------------------------
    @Override
    public void run() {

        // Using System.nanoTime for more precise timing
        double drawInterval = 1_000_000_000.0 / FPS; // ~0.01666 secs
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null) {

            // 1. UPDATE: game logic
            update();

            // 2. DRAW: render the updated scene
            repaint();

            // 3. FRAME CONTROL (sleep)
            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime /= 1_000_000.0; // convert ns to ms

                if (remainingTime < 0) {
                    // If we're behind schedule, skip sleeping
                    remainingTime = 0;
                }
                Thread.sleep((long) remainingTime);
                nextDrawTime += drawInterval;

            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // ------------------------------------------------------------------------
    // UPDATE LOGIC
    // ------------------------------------------------------------------------
    public void update() {
        switch (gameState) {
            case PLAY_STATE:
                // Update the player
                player.update();

                // Update NPCs
                for (Entity npcEntity : npc) {
                    if (npcEntity != null) {
                        npcEntity.update();
                    }
                }
                break;

            case PAUSE_STATE:
                // You could do logic for paused UI
                break;

            case TITLE_STATE:
            case DIALOGUE_STATE:
            default:
                // For dialogue, you might handle input to proceed text
                // For title, you might handle menu input
                break;
        }
    }

    // ------------------------------------------------------------------------
    // RENDER LOGIC
    // ------------------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        // DEBUG - measure drawing time if toggled
        long drawStart = 0;
        if (keyH.checkDrawTime) {
            drawStart = System.nanoTime();
        }

        // 1. Title Screen
        if (gameState == TITLE_STATE) {
            ui.draw(g2);
        }
        // 2. Game Scenes
        else {
            // Draw Tiles
            tileM.draw(g2);

            // Draw Objects
            for (SuperObject superObj : obj) {
                if (superObj != null) {
                    superObj.draw(g2, this);
                }
            }

            // Draw NPCs
            for (Entity npcEntity : npc) {
                if (npcEntity != null) {
                    npcEntity.draw(g2);
                }
            }

            // Draw Player
            player.draw(g2);

            // Draw UI
            ui.draw(g2);
        }

        // DEBUG: measure draw time
        if (keyH.checkDrawTime) {
            long drawEnd = System.nanoTime();
            long passed = drawEnd - drawStart;
            g2.setColor(Color.WHITE);
            g2.drawString("Draw Time: " + passed + " ns", 10, 400);
            System.out.println("Draw Time: " + passed + " ns");
        }

        g2.dispose();
    }

    // ------------------------------------------------------------------------
    // AUDIO CONTROLS
    // ------------------------------------------------------------------------
    public void playMusic(int i) {
        music.setFile(i);
        music.play();
        music.loop();
    }

    public void stopMusic() {
        music.stop();
    }

    public void playSE(int i) {
        se.setFile(i);
        se.play();
    }

    // ------------------------------------------------------------------------
    // Potential Additional Methods
    // ------------------------------------------------------------------------
    /**
     * Gracefully stops the game thread, e.g., on window close or re-initialization.
     */
    public void stopGameThread() {
        if (gameThread != null) {
            Thread tmp = gameThread;
            gameThread = null;
            tmp.interrupt(); // optional to break out of run() faster
        }
    }
}
