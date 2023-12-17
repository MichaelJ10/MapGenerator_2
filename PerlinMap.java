import java.awt.geom.Point2D;
import java.util.Random;

public class PerlinMap {
    private static final int GRID_SIZE = 400;

    public static float[][] generateMap(float startX, float startY, float width, float height, int renderWidth, int renderHeight, long seed) {
        Random rand = new Random(seed);
        long[] keys = new long[3];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = rand.nextLong();
        }
        float[][] map = new float[renderWidth][renderHeight];
        for (int x = 0; x < renderWidth; x++) {
            for (int y = 0; y < renderHeight; y++) {
                float aX = map(x, 0, renderWidth, 0, width);
                float aY = map(y, 0, renderHeight, 0, height);
                float val = 0;

                float freq = 1;
                float amp = 1;

                for (int i = 0; i < 12; i++) {
                    val += perlin((aX + startX) * freq / GRID_SIZE, (aY + startY) * freq / GRID_SIZE, keys) * amp;

                    freq *= 2;
                    amp /= 2;

                }

                // Contrast
                // val *= 1.2;

                // Clipping
                if (val > 1.0f)
                    val = 1.0f;
                else if (val < -1.0f)
                    val = -1.0f;

                // Convert 1 to -1 into 255 to 0
                map[x][y] = ((val + 1.0f) * 0.5f);
            }
        }

        return map;
    }

    public static float map(float x, float x1, float x2, float y1, float y2) {
        return ((y2 - y1) / (x2 - x1)) * (x - x1) + y1;
    }

    public static float perlin(float x, float y, long[] keys) {

        // Determine grid cell corner coordinates
        int x0 = (int) x;
        int y0 = (int) y;
        int x1 = x0 + 1;
        int y1 = y0 + 1;

        // Compute Interpolation weights
        float sx = x - x0;
        float sy = y - y0;

        // Compute and interpolate top two corners
        float n0 = dotGridGradient(x0, y0, x, y, keys);
        float n1 = dotGridGradient(x1, y0, x, y, keys);
        float ix0 = interpolate(n0, n1, sx);

        // Compute and interpolate bottom two corners
        n0 = dotGridGradient(x0, y1, x, y, keys);
        n1 = dotGridGradient(x1, y1, x, y, keys);
        float ix1 = interpolate(n0, n1, sx);

        // Final step: interpolate between the two previously interpolated values, now
        // in y
        float value = interpolate(ix0, ix1, sy);

        return value;
    }

    public static float interpolate(float a0, float a1, float w) {
        return (a1 - a0) * (3f - w * 2f) * w * w + a0;
    }

    public static float dotGridGradient(int ix, int iy, float x, float y, long[] keys) {
        // Get gradient from integer coordinates
        Point2D gradient = randomGradient(ix, iy, keys);

        // Compute the distance vector
        float dx = x - ix;
        float dy = y - iy;

        // Compute the dot-product
        return (float) (dx * gradient.getX() + dy * gradient.getY());
    }

    public static Point2D randomGradient(int ix, int iy, long[] keys) {
        // No precomputed gradients mean this works for any number of grid coordinates
        final int w = 8 * Integer.SIZE;
        final int s = w / 2;

        int a = ix, b = iy;
        a *= keys[0];

        b ^= a << s | a >>> w - s;
        b *= keys[1];

        a ^= b << s | b >>> w - s;
        a *= keys[2];

        float random = (float) (a * (Math.PI / ~(~0 >>> 1))); // in [0, 2*Pi]

        // Create the vector from the angle
        Point2D v = new Point2D.Float((float) Math.sin(random), (float) Math.cos(random));

        return v;
    }
}
