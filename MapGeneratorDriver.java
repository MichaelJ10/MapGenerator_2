import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;

public class MapGeneratorDriver extends JFrame {
    Map map;
    MapSettingsPanel settings;
    static final boolean TEMP = true;

    // JButton button;

    public MapGeneratorDriver() {
        if (TEMP) {
            MapGenerator.Settings settings = new MapGenerator.Settings(1997847, 2000208, 1317, 1017, 5817820451664102427l, false, false, true, 0.1f, 0.5f, 0.01f, 10, 10f);
            // BufferedImage img = MapGenerator.getCombinedMap(settings);
            // File tempOut = new File("temp.png");
            // try {
            // ImageIO.write(img, "png", tempOut);
            // }catch(IOException e) {
            // e.printStackTrace();
            // }
            STL stl = new STL();
            float baseHeight = 3;
            float[][] height = MapGenerator.getMap(settings, 0);
            float multiplier = 500 * settings.targetScale;
            int maxY = height[0].length - 1;
            int maxX = (height.length - 1);
            for (int x = 1; x < height.length; x++) {
                stl.faces.add(new STL.Face(-x + 1, 0, multiplier * Math.max(height[x - 1][0] - settings.oceanLevel, 0) + baseHeight, -x, 0, multiplier * Math.max(height[x][0] - settings.oceanLevel, 0) + baseHeight, -x, 0, 0));
                stl.faces.add(new STL.Face(-x + 1, 0, multiplier * Math.max(height[x - 1][0] - settings.oceanLevel, 0) + baseHeight, -x, 0, 0, -x + 1, 0, 0));
                for (int y = 1; y < height[x].length; y++) {
                    if (x == 1) {
                        stl.faces.add(new STL.Face(0, y, multiplier * Math.max(height[0][y] - settings.oceanLevel, 0) + baseHeight, 0, y - 1, multiplier * Math.max(height[0][y - 1] - settings.oceanLevel, 0) + baseHeight, 0, y, 0));
                        stl.faces.add(new STL.Face(0, y-1, multiplier * Math.max(height[0][y-1] - settings.oceanLevel, 0) + baseHeight, 0, y - 1, 0, 0, y, 0));
                        stl.faces.add(new STL.Face(-maxX, y - 1, multiplier * Math.max(height[maxX][y - 1] - settings.oceanLevel, 0) + baseHeight, -maxX, y, multiplier * Math.max(height[maxX][y] - settings.oceanLevel, 0) + baseHeight, -maxX, y, 0));
                        stl.faces.add(new STL.Face(-maxX, y - 1, 0, -maxX, y-1, multiplier * Math.max(height[maxX][y-1] - settings.oceanLevel, 0) + baseHeight, -maxX, y, 0));
                        
                    }
                    stl.faces.add(new STL.Face(-x, y, multiplier * Math.max(height[x][y] - settings.oceanLevel, 0) + baseHeight, -x + 1, y - 1, multiplier * Math.max(height[x - 1][y - 1] - settings.oceanLevel, 0) + baseHeight, -x + 1, y, multiplier * Math.max(height[x - 1][y] - settings.oceanLevel, 0) + baseHeight));
                    stl.faces.add(new STL.Face(-x + 1, y - 1, multiplier * Math.max(height[x - 1][y - 1] - settings.oceanLevel, 0) + baseHeight, -x, y, multiplier * Math.max(height[x][y] - settings.oceanLevel, 0) + baseHeight, -x, y - 1, multiplier * Math.max(height[x][y - 1] - settings.oceanLevel, 0) + baseHeight));
                }
                stl.faces.add(new STL.Face(-x, maxY, multiplier * Math.max(height[x][maxY] - settings.oceanLevel, 0) + baseHeight, -x + 1, maxY, multiplier * Math.max(height[x - 1][maxY] - settings.oceanLevel, 0) + baseHeight, -x, maxY, 0));
                stl.faces.add(new STL.Face(-x, maxY, 0, -x + 1, maxY, multiplier * Math.max(height[x - 1][maxY] - settings.oceanLevel, 0) + baseHeight, -x + 1, maxY, 0));
            }
            stl.faces.add(new STL.Face(0,0,0,-maxX,0,0,-maxX,maxY,0));
            stl.faces.add(new STL.Face(0,0,0,-maxX,maxY,0, 0, maxY, 0));
            byte[] bytes = stl.getBytes();
            try {
                FileOutputStream output = new FileOutputStream("map.stl");
                output.write(bytes);
                output.close();
            } catch (IOException e) {
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
                if (!dir.exists() || !dir.isDirectory())
                    dir.mkdir();
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