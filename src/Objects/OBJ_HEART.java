package Objects;

import Main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class OBJ_HEART {
    GamePanel gp;
    public BufferedImage image1,image2,image3;
    private static final int HEART_WIDTH = 32;
    private static final int HEART_HEIGHT = 32;


    public OBJ_HEART(GamePanel gp) {
        this.gp = gp;
        try{
            image1 = ImageIO.read(new File("res/player/heart_full.png"));
            image2 = ImageIO.read(new File("res/player/heart_half.png"));
            image3 = ImageIO.read(new File("res/player/heart_blank.png"));
            image1 = scaleImage(image1,HEART_WIDTH,HEART_HEIGHT);
            image2 = scaleImage(image2,HEART_WIDTH,HEART_HEIGHT);
            image3 = scaleImage(image3,HEART_WIDTH,HEART_HEIGHT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedImage scaleImage(BufferedImage img, int width, int height) {
        BufferedImage scaledImage = new BufferedImage(width,height, img.getType());

        Graphics2D g2 = scaledImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.drawImage(img,0,0,width,height,null);

        g2.dispose();
        return scaledImage;
    }
}
