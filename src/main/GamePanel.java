package main;

import entity.Entity;
import entity.Player;
import main.KeyHandler;
import object.SuperObject;
import tile.TileManager;

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;

public class GamePanel extends JPanel implements Runnable{

    // SCREEN SETTINGS
    final int originalTileSize = 16; // 16x16 tile
    final int scale = 3; // scale image to be larger or smaller

    public final int tileSize = originalTileSize * scale; // 48x48 tile
    public final int maxScreenCol = 16;
    public final int maxScreenRow = 12;
    public final int screenWidth = tileSize * maxScreenCol; // 768 pixels
    public final int screenHeight = tileSize * maxScreenRow; // 576 pixels

    // WORLD SETTINGS
    public final int maxWorldCol = 50;
    public final int maxWorldRow = 50;

    // FPS
    int FPS = 60;

    // SYSTEM
    TileManager tileM = new TileManager(this);
    KeyHandler keyH = new KeyHandler(this);
    Sound music = new Sound();
    Sound se = new Sound();
    public CollisionChecker cChecker = new CollisionChecker(this);
    public AssetSetter aSetter = new AssetSetter(this);
    public UI ui = new UI(this);
    // repeats a process again and again
    Thread gameThread; // drawing a screen 60 times / second

    // ENTITY AND OBJECT
    public Player player = new Player(this, keyH);
    public SuperObject obj[] = new SuperObject[10]; // can only display 10 objects at the same time
    public Entity npc[] = new Entity[10];

    // GAME STATE
    public int gameState;
    public final int playState = 1;
    public final int pauseState = 2;

    public GamePanel() {

        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true); // enabling this can improve game's rendering performance
        this.addKeyListener(keyH);
        this.setFocusable(true);
    }

    public void setupGame() {

        aSetter.setObject();
        aSetter.setNPC();
        playMusic(0);
        stopMusic();
        gameState = playState;
    }

    public void startGameThread() {

        gameThread = new Thread(this); // this means main.GamePanel class
        gameThread.start();
    }

    @Override
    public void run() {

        double drawInterval = 1000000000 / FPS; // 0.01666 seconds
        double nextDrawTime = System.nanoTime() + drawInterval;

        while(gameThread != null) {

            // 1 UPDATE: update information such as character positions
            update();

            // 2 DRAW: draw the screen with the updated information
            repaint();

            // Create cooldown period before updating and repainting
            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime = remainingTime / 1000000;

                if(remainingTime < 0) {
                    remainingTime = 0;
                }

                Thread.sleep((long) remainingTime); // pauses game loop until remainingTime is over

                nextDrawTime += drawInterval;

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        if(gameState == playState){
            // PLAYER
            player.update();
            // NPC
            for(int i = 0; i < npc.length; i++){
                if(npc[i] != null) {
                    npc[i].update();
                }
            }
        }

        if(gameState == pauseState) {

        }
    }

    public void paintComponent(Graphics g) {
        // Graphics class has many functions to draw objects on the screen
        super.paintComponent(g); // super in this case is JPanel

        Graphics2D g2 = (Graphics2D)g; // change Graphics g to Graphics2D class in short more functions

        // DEBUG
        long drawStart = 0;
        if(keyH.checkDrawTime == true){
            drawStart = System.nanoTime();
        }

        // TILE
        tileM.draw(g2); // tiles must be drawn before player to avoid the player being hidden behind the tiles.

        // OBJECT
        for (int i = 0; i < obj.length; i++) {
            if(obj[i] != null) {
                obj[i].draw(g2, this);
            }
        }

        // NPC
        for(int i = 0; i < npc.length; i++){
            if(npc[i] != null) {
                npc[i].draw(g2);
            }
        }

        // PLAYER
        player.draw(g2);

        // UI
        ui.draw(g2);

        // DEBUG
        if(keyH.checkDrawTime == true){
            long drawEnd = System.nanoTime();
            long passed = drawEnd - drawStart;
            g2.setColor(Color.white);
            g2.drawString("Draw Time: " + passed, 10, 400);
            System.out.println("Draw Time: " + passed);
        }
        g2.dispose(); // Dispose of this graphics context and release any system resources that it is using
    }

    public void playMusic(int i) {
        music.setFile(i);
        music.play();
        music.loop();
    }

    public void stopMusic(){
        music.stop();
    }

    public void playSE(int i){
        se.setFile(i);
        se.play();
    }
}
