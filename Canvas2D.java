import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

public class Canvas2D extends Canvas {
    private BufferedImage buffer;
    float scale = 1;

    public Canvas2D(int width, int height, Color Color) {
        setSize(width, height);
        setBackground(Color);
    }

    @Override
    public Graphics2D getGraphics() {
        try {
        buffer = new BufferedImage((int) (scale * getWidth()), (int) (scale * getHeight()), BufferedImage.TYPE_INT_RGB);
        }catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
        Graphics2D CTX = (Graphics2D) buffer.getGraphics();
        CTX.setColor(getBackground());
        CTX.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
        AffineTransform transform = new AffineTransform();
        transform.scale(scale, scale);
        CTX.transform(transform);
        return CTX;
    }

    public BufferedImage getBuffer() {
        return deepCopy(buffer);
    }

    private static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        java.awt.image.WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public void setResolution(float scale) {
        this.scale = scale;
    }

    public float getResolution() {
        return scale;
    }

    public void draw() {
        super.getGraphics().drawImage(buffer, 0, 0, getWidth(), getHeight(), null);
    }
}
