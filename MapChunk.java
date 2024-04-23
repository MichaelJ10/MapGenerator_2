import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

class MapChunk {
    private static int width;
    private static int height;
    private static long seed;
    private BufferedImage img;
    private int renderStep = -1;
    float scale = 1;
    private int x, y;

    public MapChunk(int x, int y) {
        this.x = x;
        this.y = y;
        resetImg();
    }

    public void render(MapGenerator generator, float scale) {
        generator = (MapGenerator) generator.clone();
        generator.scale = setScale(scale);
        resetImg();
        generator.setLocation(x, y, width, height, this.scale, seed);
        generator.getCombinedMap(img);
    }

    public void resetImg() {
        img = new BufferedImage((int) Math.max(Math.round(width * scale), 1), (int) Math.max(Math.round(height * scale), 1), BufferedImage.TYPE_INT_RGB);
    }

    public void DrawTo(Graphics2D CTX) {
        CTX.drawImage(img, x, y, (int) (width), (int) (height), null);
        // CTX.setColor(Color.BLACK);
        // CTX.setStroke(new BasicStroke(1));
        // CTX.drawRect(x, y, width, height);
        if (scale < 2)
            return;
        // File output = new File("temp.png");
        // try {
        // ImageIO.write(img, "PNG", output);
        // } catch (IOException ex) {
        // ex.printStackTrace();
        // }
    }

    public static boolean hasChunk(ArrayList<MapChunk> chunks, int x, int y) {
        for (MapChunk chunk : chunks) {
            if (chunk.getX() == x && chunk.getY() == y)
                return true;
        }
        return false;
    }

    public void setRendered(int step) {
        renderStep = step;
    }

    public void setUnrendered() {
        renderStep = 0;
    }

    public static void setSize(int width, int height) {
        MapChunk.width = width;
        MapChunk.height = height;
    }

    public static void setSeed(long seed) {
        if (seed == 0) {
            Random rand = new Random();
            seed = rand.nextLong();
        }
        MapChunk.seed = seed;
    }

    public float setScale(float scale) {
        if ((int) (width * scale) > 0 && (int) (height * scale) > 0)
            this.scale = scale;
        else
            this.scale = (1f / Math.min(width, height));
        // renderSize = (int) (scale * size);
        return scale;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    public static long getSeed() {
        return seed;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getRendered() {
        return renderStep;
    }
}
