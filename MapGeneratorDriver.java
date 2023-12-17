import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;

public class MapGeneratorDriver extends JFrame {
    Map map;
    MapSettingsPanel settings;
    static final boolean TEMP = false;

    // JButton button;

    public MapGeneratorDriver() {
        if(TEMP) {
            BufferedImage img = MapGenerator.getCombinedMap(new MapGenerator.Settings(44776, 454789, 1, 1, 4567245778672964256l, false, true, false, 500f, 0.5f, 0f, 10, 10f));
            File tempOut = new File("temp.png");
            try {
                ImageIO.write(img, "png", tempOut);
            }catch(IOException e) {
                e.printStackTrace();
            }
        }
        setVisible(!TEMP);
        setTitle("Map Generator");
        setLocation(0, 0);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
        if (TEMP)
            return;
        // map = new Map(1000, 772, 3f, "andrew.json", this);
        JSON.Object data = (JSON.Object) JSON.getJSON(new File("Render/data.json"));
        map = new Map(1000, 772, 3f, data, this);
        add(map);

        // settings = new MapSettingsPanel(map);
        // add(settings);

        JButton save = new JButton("Save");
        // save.setEnabled(false);
        add(save);

        JButton fullRender = new JButton("Full Render");
        add(fullRender);
        fullRender.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                map.toggleFullRender();
            }
        });

        map.addRenderedListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // save.setEnabled(true);
            }
        });

        map.addRenderingListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // save.setEnabled(false);
            }
        });

        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File dir = new File("Render");
                if(!dir.exists() || !dir.isDirectory()) dir.mkdir();
                File output = new File("Render/Render.png");
                JSON data = map.getJSON();
                try {
                    ImageIO.write(map.getRender(), "PNG", output);
                    FileWriter writer = new FileWriter("Render/data.json");
                    writer.write(data.toJSON());
                    writer.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        pack();
    }

    public static void main(String[] args) {
        new MapGeneratorDriver();
    }
}