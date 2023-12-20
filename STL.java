import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

public class STL {
    public static void main(String[] args) {
        
    }

    public ArrayList<Face> faces = new ArrayList<>();

    public STL() {

    }

    private byte[] getHeader() {
        byte[] header = new byte[84];
        Arrays.fill(header, (byte) ' ');

        byte[] numfaces = getBytes(faces.size());
        for (int i = 0; i < numfaces.length; i++)
            header[i + 80] = numfaces[i];

        return header;
    }

    public byte[] getBytes() {
        ArrayList<Byte> bytes = new ArrayList<>();
        for (Face face : faces) {
            byte[] faceBytes = face.getBytes();
            for (byte faceByte : faceBytes) {
                bytes.add(faceByte);
            }
        }
        return mergeBytes(getHeader(), getBytes(bytes.toArray(new Byte[] {})));
    }

    private static byte[] getBytes(int num) {
        return ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN).putInt(num).array();
    }

    private static byte[] getBytes(float num) {
        return ByteBuffer.allocate(Float.BYTES).order(ByteOrder.LITTLE_ENDIAN).putFloat(num).array();
    }

    private static byte[] getBytes(Byte[] vals) {
        byte[] bytes = new byte[vals.length];
        for (int i = 0; i < vals.length; i++)
            try {
                bytes[i] = (byte) vals[i];
            } catch (NullPointerException e) {
                bytes[i] = 0;
            }
        return bytes;
    }

    private static byte[] mergeBytes(byte[]... vals) {
        ArrayList<Byte> bytes = new ArrayList<>();
        for (byte[] valsBytes : vals) {
            for (byte valsByte : valsBytes) {
                bytes.add(valsByte);
            }
        }
        return getBytes(bytes.toArray(new Byte[] {}));
    }

    public static class Face {
        public Point[] vertices = new Point[3];

        public Face() {
            for (int i = 0; i < vertices.length; i++)
                vertices[i] = new Point();
        }

        public Face(Point[] vertices) {
            if (vertices.length == 3)
                this.vertices = vertices;
        }

        public Face(Point vertex1, Point vertex2, Point vertex3) {
            vertices = new Point[] { vertex1, vertex2, vertex3 };
        }

        public Face(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
            this(new Point(x1, y1, z1), new Point(x2, y2, z2), new Point(x3, y3, z3));
        }

        public byte[] getBytes() {
            ArrayList<Byte> bytes = new ArrayList<>();
            byte[] normalBytes = calculateNormalVector().getBytes();
            for (byte normalByte : normalBytes) {
                bytes.add(normalByte);
            }
            for (Point verticy : vertices) {
                byte[] verticyBytes = verticy.getBytes();
                for (byte verticyByte : verticyBytes) {
                    bytes.add(verticyByte);
                }
            }
            bytes.addAll(Arrays.asList(new Byte[2]));
            return STL.getBytes(bytes.toArray(new Byte[] {}));
        }

        private Point calculateNormalVector() {
            // Calculate two vectors in the plane of the triangle
            float x1 = vertices[1].x - vertices[0].x;
            float y1 = vertices[1].y - vertices[0].y;
            float z1 = vertices[1].z - vertices[0].z;

            float x2 = vertices[2].x - vertices[0].x;
            float y2 = vertices[2].y - vertices[0].y;
            float z2 = vertices[2].z - vertices[0].z;

            // Calculate the cross product of the two vectors to get the normal vector
            float nx = y1 * z2 - z1 * y2;
            float ny = z1 * x2 - x1 * z2;
            float nz = x1 * y2 - y1 * x2;

            return new Point(nx, ny, nz);
        }
    }

    public static class Point {
        public float x, y, z;

        public Point() {

        }

        public Point(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public byte[] getBytes() {
            return mergeBytes(STL.getBytes(x), STL.getBytes(y), STL.getBytes(z));
        }
    }
}
