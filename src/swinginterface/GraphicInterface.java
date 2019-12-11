package swinginterface;

import javax.swing.*;
import java.awt.*;

public class GraphicInterface {
    private JFrame frame;
    private GraphicsDemo graphicsPanel;
    public static boolean started = false;

    public static void start(String[][] traffic){
        started = true;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    GraphicInterface window = new GraphicInterface(traffic);
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public GraphicInterface(String[][] traffic) {
        frame = new JFrame();
        frame.setBounds(100, 100, traffic.length*100, traffic.length*100);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        graphicsPanel = new GraphicsDemo(traffic);
        graphicsPanel.setLayout(new BorderLayout(0, 0));
        graphicsPanel.setBounds(0, 0, traffic.length*100, traffic.length*100);
        graphicsPanel.setVisible(false);

        frame.getContentPane().add(graphicsPanel);

        graphicsPanel.setVisible(true);
    }

}
