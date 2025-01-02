import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.Random;

public class MapGenerator {
    int x = 0;
    int y = 0;
    int width = 0;
    int height = 0;
    int renderWidth = 0;
    int renderHeight = 0;
    long seed = 0;
    boolean fullDefinition = false;
    boolean shadow = SHADOW;
    boolean topography = TOPOGRAPHY;
    boolean physical = PHYSICAL;
    float targetScale = SCALE;
    float scale = SCALE;
    float oceanLevel = OCEAN_LEVEL;
    float oceanBlend = OCEAN_BLEND;
    float topologySize = TOPOLOGY_SIZE;
    int topologySteps = TOPOLOGY_STEPS;


        public MapGenerator(Integer x, Integer y, Integer width, Integer height, Long seed, Boolean shadow, Boolean topography, Boolean physical, Float targetScale, Float oceanLevel, Float oceanBlend, Integer topologySteps, Float topologySize) {
            setLocation(x, y, width, height, targetScale, seed);
            setSettings(shadow, topography, physical, targetScale, oceanLevel, oceanBlend, topologySteps, topologySize);
            fullDefinition = false;
        }

        public MapGenerator(Boolean shadow, Boolean topography, Boolean physical, Float targetScale, Float oceanLevel, Float oceanBlend, Integer topologySteps, Float topologySize) {
            setSettings(shadow, topography, physical, targetScale, oceanLevel, oceanBlend, topologySteps, topologySize);
            fullDefinition = false;
        }

        public MapGenerator(Boolean shadow, Boolean topography, Boolean physical, Float targetScale) {
            setSettings(shadow, topography, physical, targetScale, null, null, null, null);
            fullDefinition = false;
        }

        public MapGenerator(Integer x, Integer y, Integer width, Integer height, Long seed, Boolean shadow, Boolean topography, Boolean physical, Float oceanLevel, Float oceanBlend, Integer topologySteps, Float topologySize) {
            setLocation(x, y, width, height, targetScale, seed);
            setSettings(shadow, topography, physical, targetScale, oceanLevel, oceanBlend, topologySteps, topologySize);
            fullDefinition = true;
        }

        public MapGenerator(Boolean shadow, Boolean topography, Boolean physical, Float oceanLevel, Float oceanBlend, Integer topologySteps, Float topologySize) {
            setSettings(shadow, topography, physical, null, oceanLevel, oceanBlend, topologySteps, topologySize);
            fullDefinition = true;
        }

        public MapGenerator(Boolean shadow, Boolean topography, Boolean physical) {
            setSettings(shadow, topography, physical, null, null, null, null, null);
            fullDefinition = true;
        }

        public MapGenerator(Integer x, Integer y, Integer width, Integer height, Long seed) {
            setLocation(x, y, width, height, targetScale, seed);
        }

        public MapGenerator() {

        }

        public void setSettings(Boolean shadow, Boolean topography, Boolean physical, Float targetScale, Float oceanLevel, Float oceanBlend, Integer topologySteps, Float topologySize) {
            if (shadow != null)
                this.shadow = shadow;
            if (topography != null)
                this.topography = topography;
            if (physical != null)
                this.physical = physical;
            if (targetScale != null)
                this.targetScale = targetScale;
            if (oceanLevel != null)
                this.oceanLevel = oceanLevel;
            if (oceanBlend != null)
                this.oceanBlend = oceanBlend;
            if (topologySteps != null)
                this.topologySteps = topologySteps;
            if (topologySize != null)
                this.topologySize = topologySize;
        }

        public void setLocation(int x, int y, int width, int height, float scale, long seed) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.renderWidth = Math.max((int) (width * scale), 1);
            this.renderHeight = Math.max((int) (height * scale), 1);
            this.seed = seed;
        }

        @Override
        public Object clone() {
            return new MapGenerator(x, y, width, height, seed, shadow, topography, physical, targetScale, oceanLevel, oceanBlend, topologySteps, topologySize);
        }

    // adjustable variables
    static final DecimalFormat DF = new DecimalFormat("0.000000000");

    static final boolean SHADOW = true;
    static final boolean TOPOGRAPHY = false;
    static final boolean PHYSICAL = true;
    static final float SCALE = 1f;
    static final float OCEAN_LEVEL = 0.5f; // 0.5
    static final float OCEAN_BLEND = 0.01f;
    static final int TOPOLOGY_STEPS = 15;
    static final float TOPOLOGY_SIZE = 1f;

    static final float CAST_MIN = -0.0075f;
    static final float CAST_MAX = 0f;
    static final float MIN_SHADOW = 0f;
    static final float MAX_SHADOW = 0.5f;
    static final float MIN_HIGHLIGHT = 0f;
    static final float MAX_HIGHLIGHT = 0.175f;
    static float maxShadow = 0;
    static float minShadow = 0;

    static float HEIGHT_MODIFIER = 0;

    // DO NOT ADJUST
    static final float CLIMATE_SEPARATION = 2f, COLOR_BLEND = 1f;
    static final float LEVEL_DEEP = 0.1f, LEVEL_OCEAN = 0.3f, LEVEL_COAST = 0.45f, LEVEL_LAND = 0.7f, LEVEL_MOUNTAIN = 0.9f;

    static final float MIN_X = 0;
    static final float MAX_X = 1;
    static final float CEN_X = 0.5f;
    static final float MIN_Y = 0;
    static final float MAX_Y = 1;
    static final float CEN_Y = 0.5f;
    static float max = Float.NEGATIVE_INFINITY;

    public void getCombinedMap(BufferedImage img) {
        Graphics2D CTX = (Graphics2D) img.getGraphics();
        BufferedImage newImg = getCombinedMap();
        CTX.drawImage(newImg, 0, 0, img.getWidth(), img.getHeight(), null);
    }

    /**
     * This is the primary method of the map generator class this generates the sub maps shadows and compiles then into one image
     * 
     * @return A BufferedImage containing the generated map
     */
    public BufferedImage getCombinedMap() {
        if (renderWidth <= 0 || renderHeight <= 0)
            return null;
        if (seed == 0) {
            seed = new Random().nextLong();
            System.out.println(seed);
        }

        float[][] heightMap = getMap(0, HEIGHT_MODIFIER);
        float[][] tempMap = new float[0][0];
        float[][] humidityMap = new float[0][0];
        int[][] topologyStepMap = new int[0][0];
        if (physical) {
            tempMap = getMap(1);
            humidityMap = getMap(2);
        } else {
            topologyStepMap = getTopologyStep(heightMap);
        }

        boolean[][] topologyMap = new boolean[0][0];
        if (topography) {
            topologyMap = getTopologyMap();
        }

        Color[][] oceanFull = new Color[0][0];
        Color[][] land = new Color[0][0];
        if (physical) {
            oceanFull = getOcean(tempMap, humidityMap, heightMap);
            land = getLandMap(tempMap, humidityMap, heightMap);
        }

        Color[][] finalMap = getFinalMap(oceanFull, land, heightMap, topologyMap, topologyStepMap);
        return getImage(finalMap);
    }

    public static BufferedImage getImage(Color[][] colors) {
        BufferedImage buffer = new BufferedImage(colors.length, colors[0].length, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < colors.length; x++) {
            for (int y = 0; y < colors[x].length; y++) {
                buffer.setRGB(x, y, colors[x][y].getRGB());
            }
        }
        return buffer;
    }

    public boolean[][] getTopologyMap() {
        float topologySizeF = Math.max(topologySize * scale, 0);
        int topologySize = (int) Math.ceil(topologySizeF);
        float realSize = Math.max(topologySize, 0);
        float[][] heightMap = PerlinMap.generateMap(x - realSize, y - realSize, width + (2 * realSize), height + (2 * realSize), renderWidth + (2 * topologySize), renderHeight + (2 * topologySize), getKey(0));
        int[][] levelHeight = getTopologyStep(heightMap);
        boolean[][] topologyMap = new boolean[renderWidth][renderHeight];
        if (topography)
            for (int x = topologySize; x < renderWidth + topologySize; x++) {
                for (int y = topologySize; y < renderHeight + topologySize; y++) {
                    int start = levelHeight[x][y];
                    boolean onLine = (heightMap[x][y] > oceanLevel && heightMap[x][y] < oceanLevel + oceanBlend);
                    for (int k = -topologySize; k <= topologySize; k++) {
                        for (int l = -topologySize; l <= topologySize; l++) {
                            if (distance(0, 0, 0, k, l, 0) <= topologySizeF) {
                                if (levelHeight[x + k][y + l] != start)
                                    onLine = true;
                            }
                        }
                    }
                    topologyMap[x - topologySize][y - topologySize] = onLine;
                }
            }
        return topologyMap;
    }

    public int[][] getTopologyStep(float[][] heightMap) {
        int[][] topologyStepMap = new int[heightMap.length][heightMap[0].length];
        for (int x = 0; x < heightMap.length; x++) {
            for (int y = 0; y < heightMap[0].length; y++) {
                topologyStepMap[x][y] = (int) Math.max(Math.ceil(topologySteps * ((heightMap[x][y] - oceanLevel) / (1 - oceanLevel))), 0);
            }
        }
        return topologyStepMap;
    }

    public Color[][] getFinalMap(Color[][] oceanMap, Color[][] landMap, float[][] heightMap, boolean[][] topologyMap, int[][] topologyStepMap) {
        Color[][] finalMap = new Color[renderWidth][renderHeight];
        for (int x = 0; x < finalMap.length; x++) {
            for (int y = 0; y < finalMap[0].length; y++) {
                if (physical)
                    finalMap[x][y] = blendColor(oceanMap[x][y], landMap[x][y], ((oceanLevel - heightMap[x][y]) / oceanBlend) + 1);
                else
                    finalMap[x][y] = Color.getHSBColor(mapL(topologyStepMap[x][y], 0, topologySteps, 0, 1), 1, 1);
                if (topography && topologyMap[x][y])
                    finalMap[x][y] = Color.BLACK;
            }
        }
        return finalMap;
    }

    /**
     * Generates the sub-maps by indexing the key used to render the map
     * 
     * @param key the index (0 - 2) of what map you are using
     * @return a 2d float array containing the height of the sub-map
     */
    public float[][] getMap(int key) {
        return getMap(key, 0);
    }

    public float[][] getMap(int key, float mapValue) {
        return PerlinMap.generateMap(x, y, width, height, renderWidth, renderHeight, mapValue, getKey(key));
    }

    public long getKey(int num) {
        Random rand = new Random(seed);
        for (int x = 1; x < num; x++)
            rand.nextLong();
        return rand.nextLong();
    }

    /**
     * Generates the Ocean colors by combining the different colors of ocean that are used for this map
     * 
     * @return a 2d Color array containing the ocean data
     */
    public Color[][] getOcean(float[][] tempMap, float[][] humidityMap, float[][] heightMap) {
        final ColorPoint[] COLORS = { new ColorPoint("#281e5d", MIN_X, MIN_Y, LEVEL_DEEP), new ColorPoint("#ffffff", MIN_X, MAX_Y, LEVEL_DEEP), new ColorPoint("#0a1172", MAX_X, MIN_Y, LEVEL_DEEP), new ColorPoint("#016064", MAX_X, MAX_Y, LEVEL_DEEP), new ColorPoint("#3944bc", CEN_X, CEN_Y, LEVEL_DEEP), new ColorPoint("#051049", MIN_X, MIN_Y, LEVEL_OCEAN), new ColorPoint("#92fefd", MIN_X, MAX_Y, LEVEL_OCEAN), new ColorPoint("#1520a6", MAX_X, MIN_Y, LEVEL_OCEAN), new ColorPoint("#017a72", MAX_X, MAX_Y, LEVEL_OCEAN), new ColorPoint("#2832c2", CEN_X, CEN_Y, LEVEL_OCEAN), new ColorPoint("#5443b0", MIN_X, MIN_Y, LEVEL_COAST), new ColorPoint("#82eefd", MIN_X, MAX_Y, LEVEL_COAST), new ColorPoint("#0492c6", MAX_X, MIN_Y, LEVEL_COAST), new ColorPoint("#52b2bf", MAX_X, MAX_Y, LEVEL_COAST), new ColorPoint("#0492c6", CEN_X, CEN_Y, LEVEL_COAST) };

        Color[][] colors = calculateColor(COLORS, tempMap, humidityMap, heightMap);

        for (int x = 0; x < colors.length; x++) {
            for (int y = 0; y < colors[x].length; y++) {
                colors[x][y] = blendColor(colors[x][y], Color.BLACK, 1 - Math.max(Math.min(oceanLevel - (heightMap[x][y] / 1f), 0.5f), 0));
            }
        }

        return colors;
    }

    /**
     * Generate the map for the land this first takes the sub maps generates the colors then it generates the shadow and highlight maps to give the surface texture
     * 
     * @return a 2d Color Array containing the land data
     */
    public Color[][] getLandMap(float[][] tempMap, float[][] humidityMap, float[][] heightMap) {
        final ColorPoint[] COLORS = { new ColorPoint("#014421", MIN_X, MIN_Y, LEVEL_LAND), new ColorPoint("#FAD5A5", MAX_X, MIN_Y, LEVEL_LAND), new ColorPoint("#26580f", MAX_X, MAX_Y, LEVEL_LAND), };

        Color[][] colors = calculateColor(COLORS, tempMap, humidityMap, heightMap);
        // for(int x = 0; x < colors.length; x++) {
        // for(int y = 0; y < colors[0].length; y++) {
        // colors[x][y] = blendColor(Color.WHITE, Color.BLACK, heightMap[x][y]);
        // }
        // }
        if (shadow) {
            float[][] shadow = getShadowMap();
            float[][] highlight = getHighlightMap();
            for (int x = 0; x < colors.length; x++) {
                for (int y = 0; y < colors[0].length; y++) {
                    // colors[x][y] = blendColor(Color.WHITE, Color.BLACK, shadow[x][y]);
                    colors[x][y] = blendColor(Color.BLACK, colors[x][y], shadow[x][y]);
                    colors[x][y] = blendColor(Color.WHITE, colors[x][y], mapL(highlight[x][y], 0, 1, 0, 0.15f));
                }
            }
        }

        return colors;
    }

    public float[][] getHighlightMap() {
        int num = 500;
        float realNum = (num / scale);
        float[][] heightMap = PerlinMap.generateMap(x, y, width, height + realNum, renderWidth, renderHeight + num, HEIGHT_MODIFIER, getKey( 0));
        // float[][] heightMap = PerlinMap.generateMap(x, y, width, height, renderWidth, renderHeight, getKey(seed, 0));
        float[][] highlightMap = new float[renderWidth][renderHeight];
        for (int x = 0; x < renderWidth; x++) {
            for (int y = 0; y < renderHeight; y++) {
                float maxSlope = 0;
                for (int k = 1; k < num; k++) {
                    maxSlope = Math.max(maxSlope, (heightMap[x][y+k]-heightMap[x][y])*MAP_HEIGHT*scale/(k));
                }
                highlightMap[x][y] = (float) Math.max(0, (2 * Math.atan(maxSlope) / Math.PI)-Math.toRadians(SUNLIGHT_ANGLE));
            }
        }
        return highlightMap;
    }

    /**
     * Generates a texture map for the land giving the mountains shadow
     * 
     * @return a 2d float array containing the shadow values
     */

    public static final int SUNLIGHT_ANGLE = 10;
    public static final int MAP_HEIGHT = 300;
    public float[][] getShadowMap() {
        int num = 500;
        float realNum = (num / scale);
        float[][] heightMap = PerlinMap.generateMap(x, y, width, height + realNum, renderWidth, renderHeight + num, HEIGHT_MODIFIER, getKey(0));
        // float[][] heightMap = PerlinMap.generateMap(x, y, width, height, renderWidth, renderHeight, getKey(seed, 0));
        float[][] shadowMap = new float[renderWidth][renderHeight];
        for (int x = 0; x < renderWidth; x++) {
            for (int y = 0; y < renderHeight; y++) {
                float maxSlope = 0;
                for (int k = 1; k < num; k++) {
                    maxSlope = Math.max(maxSlope, -(heightMap[x][y+k]-heightMap[x][y])*MAP_HEIGHT*scale/(k));
                }
                shadowMap[x][y] = (float) Math.max(0, (2 * Math.atan(maxSlope) / Math.PI)-Math.toRadians(SUNLIGHT_ANGLE));
            }
        }
        return shadowMap;
    }

    public static float[] getMinMax(Float[][] nums) {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;

        for (int x = 0; x < nums.length; x++) {
            for (int y = 0; y < nums[x].length; y++) {
                min = Math.min(min, nums[x][y]);
                max = Math.max(max, nums[x][y]);
            }
        }

        return new float[] { min, max };
    }

    /**
     * ColorPoint combines the storage of an RGB color and a 3d point this allows data for the ocean and land map to be generated without needing to store multiply arrays contain points and colors
     */
    public static class ColorPoint {
        int r, g, b;
        float d, x, y, z;

        private ColorPoint(String hex, float x, float y) {
            r = Integer.valueOf(hex.substring(1, 3), 16);
            g = Integer.valueOf(hex.substring(3, 5), 16);
            b = Integer.valueOf(hex.substring(5, 7), 16);
            this.x = x;
            this.y = y;
        }

        private ColorPoint(String hex, float x, float y, float z) {
            this(hex, x, y);
            this.z = z;
        }

        private ColorPoint(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        private ColorPoint(int r, int g, int b, float x, float y) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.x = x;
            this.y = y;
        }

        private ColorPoint(int r, int g, int b, float x, float y, float z, float d) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.x = x;
            this.y = y;
            this.d = d;
        }

        @Override
        public Object clone() {
            return new ColorPoint(r, g, b, x, y, z, d);
        }
    }

    /**
     * produces a color map for the ocean and land maps this gets a list of colors points and converts them into an array of colors using the predefined subarrays
     * 
     * @param colorPoints the list of colorPoints to interpolate between
     * @return a 2d Color array representing the maped colors
     */
    public Color[][] calculateColor(ColorPoint[] colorPoints, float[][] tempMap, float[][] humidityMap, float[][] heightMap) {
        Color[][] colors = new Color[renderWidth][renderHeight];
        float dist = distance(0, 0, 0, 1, 1, 1);
        for (int x = 0; x < renderWidth; x++) {
            for (int y = 0; y < renderHeight; y++) {
                float temp = tempMap[x][y] / 1f;
                float humidity = humidityMap[x][y] / 1f;
                float Height = heightMap[x][y] / 1f;
                float total = 0;
                for (ColorPoint colorPoint : colorPoints) {
                    float d = distance(temp, humidity, Height, colorPoint.x, colorPoint.y, colorPoint.z);
                    float w = mapL(d, 0, dist, 1, 0);
                    d = mapP(w, COLOR_BLEND);
                    colorPoint.d = d;
                    total += colorPoint.d;
                }

                float r = 0, g = 0, b = 0;
                for (ColorPoint colorPoint : colorPoints) {
                    float bias = colorPoint.d / total;
                    r += colorPoint.r * bias;
                    g += colorPoint.g * bias;
                    b += colorPoint.b * bias;
                }

                colors[x][y] = new Color((int) r, (int) g, (int) b);
            }
        }
        return colors;
    }

    /**
     * Calculates the distance from two 3d points
     */
    private static float distance(float x1, float y1, float z1, float x2, float y2, float z2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
    }

    /**
     * Linearly maps a value from one range to another
     * 
     * @param x  value to be mapped
     * @param x1 input value 1
     * @param x2 input value 2
     * @param y1 output value corresponding to 1
     * @param y2 output value corresponding to 2
     * @return the output for the given input
     */
    private static float mapL(float x, float x1, float x2, float y1, float y2) {
        if (Math.abs(x1 - x2) < 0.0001)
            return y1;
        return ((y2 - y1) / (x2 - x1)) * (x - x1) + y1;
    }

    /**
     * Dynamically maps an input from 0 - 1 to an output from 0 - 1
     * 
     * @param x the value to be interpolated
     * @param a the strength of the interpolation
     * @return the mapped value
     */
    private static float mapP(float x, float a) {
        a = (float) Math.pow(Math.E, a);
        if (x < 0) {
            return 0;
        } else if (x < 0.5f) {
            return (float) (((Math.pow(2, a)) / (2)) * (Math.pow(x, a)));
        } else if (x < 1) {
            return (float) ((1) - (((Math.pow(2, a)) / (2)) * (Math.pow((1 - x), a))));
        } else {
            return 1;
        }
    }

    public static Color adjustBrightness(Color color, float brightness) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], brightness));
    }

    /**
     * linearly interpolates between two colors
     * 
     * @param c1   color 1
     * @param c2   color 2
     * @param bias the weight (0 meaning color 1)
     * @return the interpolated color
     */
    private static Color blendColor(Color c1, Color c2, float bias) {
        bias = Math.min(Math.max(bias, 0), 1);
        int r = 0, g = 0, b = 0;
        r += c1.getRed() * bias;
        g += c1.getGreen() * bias;
        b += c1.getBlue() * bias;
        bias = 1 - bias;
        r += c2.getRed() * bias;
        g += c2.getGreen() * bias;
        b += c2.getBlue() * bias;
        return new Color(r, g, b);
    }
}
