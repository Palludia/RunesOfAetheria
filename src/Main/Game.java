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
        double drawInterval = 1000000000.0/FPS_SET;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0, frame = 0;

        while(gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            while(delta >= 1) {
                gamePanel.updatePos();
                delta--;
                frame++;
            }
            gamePanel.repaint();
            if(timer >= 1000000000) {
                System.out.println("FPS: " + frame);
                frame = 0;
                timer = 0;
            }
        }
    }

}
