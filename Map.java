import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.Timer;

public class Map extends Canvas2D {
    final float MAX_ZOOM = 10f;
    final float MIN_ZOOM = -10f;
    final int CHUNK_WIDTH = 50;
    final int CHUNK_HEIGHT = 75;
    final int RENDER_SPEED = 40;
    final float MIN_SCALE = 0.0625f;
    final float MAX_SCALE = 5f;
    Timer timer;
    int x = -2000000, y = -2000000;
    float posX, posY;
    float zoom = 0;
    ArrayList<MapChunk> chunks = new ArrayList<>();
    boolean reRender = false;
    boolean rendered = false;
    boolean executeRender = false;
    boolean fullRender = true;
    boolean moved = true;
    int index = 0;
    int renderStepCt = 0;
    boolean[][] isRendered;
    ArrayList<ActionListener> renderedActions = new ArrayList<>();
    ArrayList<ActionListener> renderingActions = new ArrayList<>();
    ArrayList<MapGenerator.Settings> renderStep = new ArrayList<>();

    public void getData(JSON.Object data) {
        this.renderStep = new ArrayList<>();
        this.x = (int) ((JSON.Value.Integer) data.getItem("x")).getInteger();
        this.y = (int) ((JSON.Value.Integer) data.getItem("y")).getInteger();
        MapChunk.setSeed(((JSON.Value.Integer) data.getItem("seed")).getInteger());
        this.scale = (float) ((JSON.Value.Float) data.getItem("scale")).getFloat();
        this.zoom = (float) ((JSON.Value.Float) data.getItem("zoom")).getFloat();
        for (JSON renderStep : ((JSON.Array) data.getItem("renderSteps"))) {
            JSON.Object renderObj = (JSON.Object) renderStep;
            boolean fullDefinition = ((JSON.Value.Boolean) renderObj.getItem("fullDefinition")).getBoolean();
            boolean physical = ((JSON.Value.Boolean) renderObj.getItem("physical")).getBoolean();
            boolean shadow = ((JSON.Value.Boolean) renderObj.getItem("shadow")).getBoolean();
            boolean topography = ((JSON.Value.Boolean) renderObj.getItem("topography")).getBoolean();
            int topologySteps = (int) ((JSON.Value.Integer) renderObj.getItem("topologySteps")).getInteger();
            float topologySize = (float) ((JSON.Value.Float) renderObj.getItem("topologySize")).getFloat();
            float targetScale = (float) ((JSON.Value.Float) renderObj.getItem("targetScale")).getFloat();
            float oceanLevel = (float) ((JSON.Value.Float) renderObj.getItem("oceanLevel")).getFloat();
            float oceanBlend = (float) ((JSON.Value.Float) renderObj.getItem("oceanBlend")).getFloat();
            if (fullDefinition) {
                this.renderStep.add(new MapGenerator.Settings(shadow, topography, physical, oceanLevel, oceanBlend, topologySteps, topologySize));
            } else {
                this.renderStep.add(new MapGenerator.Settings(shadow, topography, physical, targetScale, oceanLevel, oceanBlend, topologySteps, topologySize));
            }
        }
    }

    public JSON getJSON() {
        JSON.Object data = new JSON.Object();
        data.add(new JSON.Object.Value("x", new JSON.Value.Integer(x)));
        data.add(new JSON.Object.Value("y", new JSON.Value.Integer(y)));
        data.add(new JSON.Object.Value("scale", new JSON.Value.Float(scale)));
        data.add(new JSON.Object.Value("zoom", new JSON.Value.Float(zoom)));
        data.add(new JSON.Object.Value("seed", new JSON.Value.Integer(MapChunk.getSeed())));
        JSON.Array stepsData = new JSON.Array();
        for (MapGenerator.Settings settings : renderStep) {
            JSON.Object stepData = new JSON.Object();
            stepData.add(new JSON.Object.Value("fullDefinition", new JSON.Value.Boolean(settings.fullDefinition)));
            stepData.add(new JSON.Object.Value("physical", new JSON.Value.Boolean(settings.physical)));
            stepData.add(new JSON.Object.Value("shadow", new JSON.Value.Boolean(settings.shadow)));
            stepData.add(new JSON.Object.Value("topography", new JSON.Value.Boolean(settings.topography)));
            stepData.add(new JSON.Object.Value("topologySize", new JSON.Value.Float(settings.topologySize)));
            stepData.add(new JSON.Object.Value("topologySteps", new JSON.Value.Integer(settings.topologySteps)));
            stepData.add(new JSON.Object.Value("targetScale", new JSON.Value.Float(settings.targetScale)));
            stepData.add(new JSON.Object.Value("oceanLevel", new JSON.Value.Float(settings.oceanLevel)));
            stepData.add(new JSON.Object.Value("oceanBlend", new JSON.Value.Float(settings.oceanBlend)));
            stepsData.add(stepData);
        }
        data.add(new JSON.Object.Value("renderSteps", stepsData));
        return data;
    }

    public Map(int width, int height, float resolution, JSON.Object data, JFrame obj) {
        this(width, height, resolution, obj, true);
        getData(data);
        timer.start();
    }

    public Map(int width, int height, float resolution, JFrame obj) {
        this(width, height, resolution, obj, false);
    }

    public Map(int width, int height, float resolution, JFrame obj, boolean mode) {
        super(width, height, Color.WHITE);

        setResolution(resolution);
        MapChunk.setSize(CHUNK_WIDTH, CHUNK_HEIGHT);
        MapChunk.setSeed(0);

        renderStep.add(new MapGenerator.Settings(false, false, true, 0.01f));
        renderStep.add(new MapGenerator.Settings(true, false, true, 1f));
        renderStep.add(new MapGenerator.Settings(true, false, true));

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                Point pos = e.getPoint();
                posX = (float) (x - pos.x / getZoom());
                posY = (float) (y - pos.y / getZoom());
                moved = true;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point pos = e.getPoint();
                x = (int) (posX + pos.x / getZoom());
                y = (int) (posY + pos.y / getZoom());
                moved = true;
            }
        });

        addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                Point pos = e.getPoint();
                x -= pos.x / getZoom();
                y -= pos.y / getZoom();
                zoom -= e.getPreciseWheelRotation() * 0.1f;
                zoom = Math.min(Math.max(zoom, MIN_ZOOM), MAX_ZOOM);
                x += pos.x / getZoom();
                y += pos.y / getZoom();
                reRenderAll();
                MapChunk.setSize((int) Math.max(1, CHUNK_WIDTH / getZoom()), (int) Math.max(1, CHUNK_HEIGHT / getZoom()));
                chunks.clear();
                moved = true;
            }
        });

        timer = new Timer(21, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                x = Math.max(Math.min(x, 0), -4000000);
                y = Math.max(Math.min(y, 0), -4000000);

                int chunkWidth = Math.max((int) (CHUNK_WIDTH / getZoom()), 1);
                int chunkHeight = Math.max((int) (CHUNK_HEIGHT / getZoom()), 1);
                MapChunk.setSize(chunkWidth, chunkHeight);

                int startX = (int) Math.floor(((float) -x) / (chunkWidth));
                int startY = (int) Math.floor(((float) -y) / (chunkHeight));

                int widthX = (int) Math.ceil(((float) (getWidth() / getZoom())) / (chunkWidth)) + 1;
                int widthY = (int) Math.ceil(((float) (getHeight() / getZoom())) / (chunkHeight)) + 1;

                int endX = widthX + startX;
                int endY = widthY + startY;

                if (reRender)
                    chunks.clear();

                ArrayList<MapChunk> removeList = new ArrayList<>();
                for (MapChunk chunk : chunks) {
                    int posX = chunk.getX() / chunkWidth;
                    int posY = chunk.getY() / chunkHeight;
                    if (posX < startX || posX > endX || posY < startY || posY > endY) {
                        removeList.add(chunk);
                    }
                }
                if (reRender || moved || isRendered == null || widthX != isRendered.length || widthY != isRendered[0].length || removeList.size() > 0) {
                    try {
                        isRendered = new boolean[widthX][widthY];
                    } catch (NegativeArraySizeException ex) {
                        ex.printStackTrace();
                    }
                    index = 0;
                    renderStepCt = 0;
                }
                chunks.removeAll(removeList);
                moved = false;
                reRender = false;

                int ct = 0;
                int last = renderStep.size() - 1;
                loop: for (int k = renderStepCt; k <= last && ct <= 0; k++) {
                    long start = System.currentTimeMillis();
                    MapGenerator.Settings settings = renderStep.get(k);
                    float scale = ((getHeight() * Map.this.scale) / widthY) / chunkHeight;
                    scale *= settings.targetScale;
                    settings.scale = scale;
                    if (ct > 0)
                        break;
                    step: if (k == 0) {
                        for (int i = 0; i < widthX; i++) {
                            for (int j = 0; j < widthY; j++) {
                                if (System.currentTimeMillis() - start > RENDER_SPEED && ct > 0)
                                    break loop;
                                int posX = (i + startX) * chunkWidth;
                                int posY = (j + startY) * chunkHeight;
                                if (isRendered[i][j])
                                    continue;
                                if (!MapChunk.hasChunk(chunks, posX, posY)) {
                                    MapChunk chunk = new MapChunk(posX, posY);
                                    chunk.render(settings, scale);
                                    chunk.setRendered(k);
                                    chunks.add(chunk);
                                    ct++;
                                }
                                isRendered[i][j] = true;
                            }
                        }
                    } else {
                        if (chunks.get(chunks.size() - 1).getRendered() >= k)
                            break step;
                        for (int l = index; l < chunks.size(); l++) {
                            if (l == chunks.size() - 1)
                                System.currentTimeMillis();
                            MapChunk chunk = chunks.get(l);
                            if (System.currentTimeMillis() - start > RENDER_SPEED && ct > 0)
                                break loop;
                            obj.setTitle("Map Generator: (" + (chunks.indexOf(chunk)) + " / " + (chunks.size()) + ")");
                            if (chunk.getRendered() < k) {
                                chunk.render(settings, scale);
                                chunk.setRendered(k);
                                ct++;
                                obj.setTitle("Map Generator: (" + (chunks.indexOf(chunk) + 1) + " / " + (chunks.size()) + ")");
                            }
                            index = Math.max(index, l);
                        }
                    }
                    if (ct <= 0 && (k + 1) > renderStepCt) {
                        index = 0;
                        renderStepCt = k + 1;
                    }
                }
                rendered = (ct <= 0);

                if (rendered != executeRender) {
                    for (ActionListener event : (rendered ? renderedActions : renderingActions)) {
                        event.actionPerformed(e);
                    }
                }
                executeRender = rendered;

                // MapChunk.setScale(getZoom());
                // reRenderAll();

                Graphics2D CTX = getGraphics();
                AffineTransform transform = new AffineTransform();
                transform.scale(getZoom(), getZoom());
                CTX.transform(transform);
                CTX.translate(x, y);
                for (MapChunk chunk : chunks) {
                    chunk.DrawTo(CTX);
                }
                draw();
                // System.out.println(chunks.size() +
            }
        });

        // MapChunk chunk1 = new MapChunk(0, 0);
        // chunk1.render();
        // chunks.add(chunk1);

        // MapChunk chunk2 = new MapChunk(100, 100);
        // chunk2.render();
        // chunks.add(chunk2);

        if (!mode)
            timer.start();
    }

    public BufferedImage getRender() {
        return getBuffer();
    }

    public String getData() {
        String str = "";
        str += "{";
        str += "\"seed\": ";
        str += MapChunk.getSeed();
        str += ",\"x\": ";
        str += x;
        str += ",\"y\": ";
        str += y;
        str += "}";
        return str;
    }

    public void toggleFullRender() {
        fullRender = !fullRender;
        reRenderAll();
    }

    public void addRenderedListener(ActionListener listener) {
        renderedActions.add(listener);
    }

    public void addRenderingListener(ActionListener listener) {
        renderingActions.add(listener);
    }

    public void reRenderAll() {
        reRender = true;
    }

    private float getZoom() {
        return (float) Math.pow(Math.E, zoom);
    }

    @Override
    public void setResolution(float scale) {
        float originalScale = this.scale;
        super.setResolution(Math.max(Math.min(scale, MAX_SCALE), MIN_SCALE));
        if (this.scale != originalScale)
            reRenderAll();
        MapChunk.setSize(Math.max((int) Math.floor(getWidth() / (getZoom() * (this.scale * getWidth() - 1))), CHUNK_WIDTH), CHUNK_HEIGHT);
    }
}
