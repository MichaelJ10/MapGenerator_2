import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;

public class MapGeneratorDriver extends JFrame {
    Map map;
    MapSettingsPanel settings;

    // JButton button;

    public void generateSTL() {
        MapGenerator generator = new MapGenerator(1997847, 2000208, 1317, 1017, 5817820451664102427l, false, false, true, 3f, 0.5f, 0.01f, 10, 10f);
        STL stl = new STL();
        final int MAX_WIDTH = 140;
        final int MAX_HEIGHT = 40;
        float mapValue = -0.25f;
        float baseHeight = 3;
        System.out.println("start generation");
        float[][] height = generator.getMap(0, mapValue);
        System.out.println("done generating");
        float scale = 0.5f;
        scale = Math.min(scale, ((float) MAX_WIDTH / generator.renderWidth));
        scale = Math.min(scale, ((float) MAX_HEIGHT / generator.renderHeight));
        float multiplier = 300*(float) Math.pow(Math.E, -mapValue) * scale * generator.targetScale;
        int maxY = height[0].length - 1;
        int maxX = (height.length - 1);
        System.out.println("generating stl");
        for (int x = 1; x < height.length; x++) {
            System.out.print("step: " + x + "    \r");
            stl.faces.add(new STL.Face(scale * (-x + 1), 0, multiplier * Math.max(height[x - 1][0] - generator.oceanLevel, 0) + baseHeight, -x * scale, 0, multiplier * Math.max(height[x][0] - generator.oceanLevel, 0) + baseHeight, -x * scale, 0, 0));
            stl.faces.add(new STL.Face(scale * (-x + 1), 0, multiplier * Math.max(height[x - 1][0] - generator.oceanLevel, 0) + baseHeight, -x * scale, 0, 0, scale * (-x + 1), 0, 0));
            for (int y = 1; y < height[x].length; y++) {
                if (x == 1) {
                    stl.faces.add(new STL.Face(0, scale * y, multiplier * Math.max(height[0][y] - generator.oceanLevel, 0) + baseHeight, 0, scale * (y - 1), multiplier * Math.max(height[0][y - 1] - generator.oceanLevel, 0) + baseHeight, 0, scale * y, 0));
                    stl.faces.add(new STL.Face(0, scale * (y - 1), multiplier * Math.max(height[0][y - 1] - generator.oceanLevel, 0) + baseHeight, 0, scale * (y - 1), 0, 0, scale * y, 0));
                    stl.faces.add(new STL.Face(-maxX * scale, scale * (y - 1), multiplier * Math.max(height[maxX][y - 1] - generator.oceanLevel, 0) + baseHeight, -maxX * scale, scale * y, multiplier * Math.max(height[maxX][y] - generator.oceanLevel, 0) + baseHeight, -maxX * scale, scale * y, 0));
                    stl.faces.add(new STL.Face(-maxX * scale, scale * (y - 1), 0, -maxX * scale, scale * (y - 1), multiplier * Math.max(height[maxX][y - 1] - generator.oceanLevel, 0) + baseHeight, -maxX * scale, scale * y, 0));

                }
                stl.faces.add(new STL.Face(-x * scale, scale * y, multiplier * Math.max(height[x][y] - generator.oceanLevel, 0) + baseHeight, scale * (-x + 1), scale * (y - 1), multiplier * Math.max(height[x - 1][y - 1] - generator.oceanLevel, 0) + baseHeight, scale * (-x + 1), scale * y, multiplier * Math.max(height[x - 1][y] - generator.oceanLevel, 0) + baseHeight));
                stl.faces.add(new STL.Face(scale * (-x + 1), scale * (y - 1), multiplier * Math.max(height[x - 1][y - 1] - generator.oceanLevel, 0) + baseHeight, -x * scale, scale * y, multiplier * Math.max(height[x][y] - generator.oceanLevel, 0) + baseHeight, scale * (-x), scale * (y - 1), multiplier * Math.max(height[x][y - 1] - generator.oceanLevel, 0) + baseHeight));
            }
            stl.faces.add(new STL.Face(-x * scale, scale * maxY, multiplier * Math.max(height[x][maxY] - generator.oceanLevel, 0) + baseHeight, scale * (-x + 1), scale * maxY, multiplier * Math.max(height[x - 1][maxY] - generator.oceanLevel, 0) + baseHeight, -x * scale, scale * maxY, 0));
            stl.faces.add(new STL.Face(-x * scale, scale * maxY, 0, scale * (-x + 1), scale * maxY, multiplier * Math.max(height[x - 1][maxY] - generator.oceanLevel, 0) + baseHeight, scale * (-x + 1), scale * maxY, 0));
        }
        System.out.println("stl generated");
        // stl.faces.add(new STL.Face(0,0,0,-maxX,0,0,-maxX,maxY,0));
        // stl.faces.add(new STL.Face(0,0,0,-maxX,maxY,0, 0, maxY, 0));
        System.out.println("parsing bytes: " + stl.faces.size() + " faces");
        byte[] bytes = stl.getBytes();
        System.out.println("finished parsing bytes: " + convertBytes(bytes.length));
        try {
            FileOutputStream output = new FileOutputStream("map.stl");
            output.write(bytes);
            output.close();

            Thread.sleep(100);
            System.out.println("start remesh process");
            ProcessBuilder processBuilder = new ProcessBuilder("py", "repair.py", "-i", "map.stl", "-o", "map_.stl", "-s", "repair_script.mlx");
            Process process = processBuilder.start();
            InputStream input = process.getInputStream();
            InputStream error = process.getErrorStream();
            while (process.isAlive()) {
                System.out.print("\u001B[37m" + new String(input.readAllBytes()));
                System.err.print("\u001B[31m" + new String(error.readAllBytes()));
            }
            System.out.print("\u001B[37m" + new String(input.readAllBytes()));
            System.err.print("\u001B[31m" + new String(error.readAllBytes()));
            System.out.println("\u001B[37m" + "py script finished with exit code = " + process.exitValue());
            if(process.exitValue() != 0) {
                System.exit(process.exitValue());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        // System.exit(0);
    }

    public MapGeneratorDriver() {
        setVisible(true);
        setTitle("Map Generator");
        setLocation(0, 0);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
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

    public static String convertBytes(long bytes) {
        String[] suffixes = new String[]{"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB"};
        int suffixIndex = 0;
        double value = bytes;

        while (value >= 1024 && suffixIndex < suffixes.length - 1) {
            value /= 1024;
            suffixIndex++;
        }

        return String.format("%.2f %s", value, suffixes[suffixIndex]);
    }
    
    public static void main(String[] args) {
        new MapGeneratorDriver();
    }
}