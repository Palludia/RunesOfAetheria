package Main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Menu {
    GamePanel gp;
    private Image backgroundImage;

    public Menu(GamePanel gp){
        this.gp = gp;
        try{
            // Load the background image
            backgroundImage = ImageIO.read(new File("res/titlescreen/RunesOfAetheria.png"));
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2) {
        drawTitleScreen(g2); // Call the method to draw the title screen
    }

    public void drawTitleScreen(Graphics2D g2) {
        // Ensure the background is filled with black or any other color before drawing the image
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.getWidth(), gp.getHeight()); // Fill the screen (match the panel size)

        // Draw the background image with specified coordinates (position and size)
        if (backgroundImage != null) {
            Image scaledImage = backgroundImage.getScaledInstance(gp.getWidth(), gp.getHeight(), Image.SCALE_SMOOTH);
            g2.drawImage(scaledImage, 0, 0, null);
        } else {
            System.out.println("Background image not loaded!");
        }

        // Optionally, add any title text or other elements
        int screenWidth = gp.screenWidth;
        int screenHeight = gp.screenHeight;

//        String text = "";
//        g2.setColor(Color.WHITE);
//        g2.setFont(g2.getFont().deriveFont(Font.BOLD,40F));
//        text = "NEW GAME";
//
//        FontMetrics fm = g2.getFontMetrics();
//        int textWidth = fm.stringWidth(text);
//        int textHeight = fm.getAscent();
//
//        int x = (screenWidth - textWidth) / 2;
//        int y = (screenHeight / 2) + (textHeight / 4);
//
//        g2.drawString(text,x,y + 55);
//
//        text = "SETTINGS";
//        g2.drawString(text,x + 10,y + 110);
//
//        text = "QUIT";
//        g2.drawString(text,x + 60,y + 165);

        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 40F));

        String[] menuItems = { "NEW GAME", "SETTINGS", "QUIT" };
        int menuY = (screenHeight / 2) + 50; // Starting vertical position
        int lineSpacing = 55; // Space between lines

        for (int i = 0; i < menuItems.length; i++) {
            String text = menuItems[i];

            // Get text width for proper centering
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent(); // Get ascent for vertical alignment

            // Center each text separately
            int x = (screenWidth - textWidth) / 2;
            int y = menuY + (i * lineSpacing) + (textHeight / 4);

            if(i == gp.commandNum) {
                g2.drawString(">", x - 40, y);
            }

            g2.drawString(text, x, y);
        }

    }
}
