package swinginterface;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class GraphicsDemo extends JPanel {
    String[][] traffic;
    public static GraphicsDemo instance = null;

    GraphicsDemo(String[][] traffic){

        GraphicsDemo.instance = this;

        this.traffic = traffic;
        this.repaint();
    }

    public void setTraffic(String[][] traffic){
        this.traffic = traffic;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = 100;
        int height = 100;
        int x = 0;
        int y = 0;
        BufferedImage plane;
        BufferedImage sky;

        try {
            plane = ImageIO.read(new File("src/swinginterface/plane.png"));
            sky = ImageIO.read(new File("src/swinginterface/sky.jpg"));

            for (String[] cell : this.traffic) {
                for (String s : cell) {

                    switch (s) {
                        case "null":
                            g.drawImage(sky, x, y, width, height, this);
                            break;
                        default:
                            g.drawImage(plane, x, y, width, height, this);
                            break;
                    }
                    x += width;
                }
                x = 0;
                y += height;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
