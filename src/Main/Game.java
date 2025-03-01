package Main;

public class Game implements Runnable{
    private final GameFrame gameFrame;
    private final GamePanel gamePanel;
    private final int FPS_SET = 60;
    private Thread gameThread;

    public Game(){
        gamePanel = new GamePanel();
        gameFrame = new GameFrame(gamePanel);
        startGameLoop();
    }

    private void startGameLoop() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS_SET; // Time per frame (in nanoseconds)
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int frameCount = 0;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            while (delta >= 1) {
                gamePanel.updatePos(); // Update game logic
                gamePanel.repaint();   // Render the game
                delta--;
                frameCount++;
            }

            // FPS Counter
            if (timer >= 1000000000) {
                System.out.println("FPS: " + frameCount);
                frameCount = 0;
                timer = 0;
            }

            // Prevent CPU overuse
            try {
                Thread.sleep(1); // Sleep for 1ms to avoid maxing out CPU
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
