import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class JSONConvert {
    public static JSON getJSON(int val) {
        return getJSON((long) val);
    }

    public static JSON getJSON(int[] vals) {
        JSON.Array list = new JSON.Array();
        for(int val : vals) {
            list.add(getJSON(val));
        }
        return list;
    }

    public static JSON getJSON(int[][] vals) {
        JSON.Array list = new JSON.Array();
        for(int[] val : vals) {
            list.add(getJSON(val));
        }
        return list;
    }

    public static JSON getJSON(float val) {
        return getJSON((double) val);
    }

    public static JSON getJSON(float[] vals) {
        JSON.Array list = new JSON.Array();
        for(float val : vals) {
            list.add(getJSON(val));
        }
        return list;
    }

    public static JSON getJSON(float[][] vals) {
        JSON.Array list = new JSON.Array();
        for(float[] val : vals) {
            list.add(getJSON(val));
        }
        return list;
    }

    public static JSON getJSON(long val) {
        return new JSON.Value.Integer(val);
    }

    public static JSON getJSON(long[] vals) {
        JSON.Array list = new JSON.Array();
        for(long val : vals) {
            list.add(getJSON(val));
        }
        return list;
    }

    public static JSON getJSON(long[][] vals) {
        JSON.Array list = new JSON.Array();
        for(long[] val : vals) {
            list.add(getJSON(val));
        }
        return list;
    }

    public static JSON getJSON(double val) {
        return new JSON.Value.Float(val);
    }

    public static JSON getJSON(double[] vals) {
        JSON.Array list = new JSON.Array();
        for(double val : vals) {
            list.add(getJSON(val));
        }
        return list;
    }

    public static JSON getJSON(double[][] vals) {
        JSON.Array list = new JSON.Array();
        for(double[] val : vals) {
            list.add(getJSON(val));
        }
        return list;
    }

    public static JSON getJSON(boolean val) {
        return new JSON.Value.Boolean(val);
    }

    public static JSON getJSON(boolean[] vals) {
        JSON.Array list = new JSON.Array();
        for(boolean val : vals) {
            list.add(getJSON(val));
        }
        return list;
    }

    public static JSON getJSON(boolean[][] vals) {
        JSON.Array list = new JSON.Array();
        for(boolean[] val : vals) {
            list.add(getJSON(val));
        }
        return list;
    }

    public static JSON getJSON(String val) {
        return new JSON.Value.String(val);
    }

    public static JSON getJSON(String[] vals) {
        JSON.Array list = new JSON.Array();
        for(String val : vals) {
            list.add(getJSON(val));
        }
        return list;
    }

    public static JSON getJSON(String[][] vals) {
        JSON.Array list = new JSON.Array();
        for(String[] val : vals) {
            list.add(getJSON(val));
        }
        return list;
    }

    public static JSON getJSON(Point point) {
        JSON.Object output = new JSON.Object();
        output.add(new JSON.Object.Value("x", getJSON(point.x)));
        output.add(new JSON.Object.Value("x", getJSON(point.y)));
        return output;
    }

    public static Point getPoint(JSON.Object data) {
        int x = getInt(data, "x");
        int y = getInt(data, "y");
        return new Point(x, y);
    }

    public static JSON getJSON(Rectangle rectangle) {
        JSON.Object output = new JSON.Object();
        output.add(new JSON.Object.Value("x", getJSON(rectangle.x)));
        output.add(new JSON.Object.Value("y", getJSON(rectangle.y)));
        output.add(new JSON.Object.Value("width", getJSON(rectangle.width)));
        output.add(new JSON.Object.Value("height", getJSON(rectangle.height)));
        return output;
    }

    public static Rectangle getRectangle(JSON.Object data) {
        int x = getInt(data, "x");
        int y = getInt(data, "y");
        int width = getInt(data, "width");
        int height = getInt(data, "height");
        return new Rectangle(x, y, width, height);
    }

    public static JSON getJSON(Color color) {
        JSON.Object output = new JSON.Object();
        output.add(new JSON.Object.Value("r", getJSON(color.getRed())));
        output.add(new JSON.Object.Value("g", getJSON(color.getGreen())));
        output.add(new JSON.Object.Value("b", getJSON(color.getBlue())));
        output.add(new JSON.Object.Value("a", getJSON(color.getAlpha())));
        return output;
    }

    public static JSON getJSON(Color[] vals) {
        JSON.Array list = new JSON.Array();
        for(Color val : vals) {
            list.add(getJSON(val));
        }
        return list;
    }

    public static JSON getJSON(Color[][] vals) {
        JSON.Array list = new JSON.Array();
        for(Color[] val : vals) {
            list.add(getJSON(val));
        }
        return list;
    }

    public static Color getColor(JSON.Object data) {
        int r = getInt(data, "r");
        int g = getInt(data, "g");
        int b = getInt(data, "b");
        int a = getInt(data, "a");
        return new Color(r, g, b, a);
    }

    public static JSON getJSON(BufferedImage image) {
        Color[][] imgColors = new Color[image.getWidth(null)][image.getHeight(null)];
        for(int x = 0; x < image.getWidth(); x++) {
            for(int y = 0; y < image.getHeight(); y++) {
                imgColors[x][y] = new Color(image.getRGB(x, y));
            }
        }
        return getJSON(imgColors);
    }

    public static BufferedImage getImage(JSON.Array data) {
        int width = data.size();
        if(width <= 0) return null;
        int height = ((JSON.Array) data.get(0)).size();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                image.setRGB(x, y, getColor((JSON.Object) ((JSON.Array) data.get(x)).get(y)).getRGB());
            }
        }
        return image;
    }

    private static int getInt(JSON.Object data, String name) {
        return (int) getLong(data, name);
    }

    private static long getLong(JSON.Object data, String name) {
        return ((JSON.Value.Integer) data.getItem(name)).getInteger();
    }

    @SuppressWarnings("unused")
    private static float getFloat(JSON.Object data, String name) {
        return (float) getDouble(data, name);
    }

    private static double getDouble(JSON.Object data, String name) {
        return ((JSON.Value.Float) data.getItem(name)).getFloat();
    }
}
