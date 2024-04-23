import java.awt.geom.IllegalPathStateException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class STL {
    public static void main(String[] args) {
        
    }

    public ArrayList<Face> faces = new ArrayList<>();

    public STL() {

    }

    private byte[] getHeader() {
        byte[] header = new byte[84];
        Arrays.fill(header, (byte) ' ');

        byte[] numFaces = getBytes(faces.size());
        for (int i = 0; i < numFaces.length; i++)
            header[i + 80] = numFaces[i];

        return header;
    }

    public byte[] getBytes() {
        int numThreads = Runtime.getRuntime().availableProcessors();
        int numFaces = faces.size();
        int minFacesPerThread = numFaces / numThreads;
        int extraFaces = numFaces % numThreads;
        Face[][] facets = new Face[numThreads][0];
        byte[][][] bytes = new byte[numThreads][0][0];
        int pos = 0;
        System.out.println("num threads: " + numThreads);
        AtomicInteger count = new AtomicInteger(numFaces);
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);
        for(int i = 0; i < numThreads; i++) {
            int currentNum = minFacesPerThread;
            if(extraFaces > 0) {
                currentNum++;
                extraFaces--;
            }
            bytes[i] = new byte[currentNum][0];
            facets[i] = faces.subList(pos, pos + currentNum).toArray(new Face[] {});
            if(bytes[i].length != facets[i].length) throw new IllegalPathStateException();
            pos += currentNum;
            final int index = i;
            executor.submit(() -> {
                for(int k = 0; k < facets[index].length; k++) {
                    bytes[index][k] = facets[index][k].getBytes();
                    facets[index][k] = null;
                    count.decrementAndGet();
                }
                facets[index] = null;
            });
        }
        executor.shutdown();
        while(!executor.isTerminated()) {
            System.out.print(count.get() + ", " + convertBytes(Runtime.getRuntime().freeMemory()) + "      \r");
            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("compiling bytes");
        byte[] flattenedBytes = mergeBytes(bytes);
        return mergeBytes(getHeader(), flattenedBytes);
    }

    public static String convertBytes(long bytes) {
        String[] suffixes = new String[]{"Bytes", "KB", "MB", "GB", "TB"};
        int suffixIndex = 0;
        double value = bytes;

        while (value >= 1024 && suffixIndex < suffixes.length - 1) {
            value /= 1024;
            suffixIndex++;
        }

        return String.format("%.2f %s", value, suffixes[suffixIndex]);
    }

    private static byte[] getBytes(int num) {
        return ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN).putInt(num).array();
    }

    private static byte[] getBytes(float num) {
        return ByteBuffer.allocate(Float.BYTES).order(ByteOrder.LITTLE_ENDIAN).putFloat(num).array();
    }

    private static byte[] getBytes(int[] vals) {
        byte[] bytes = new byte[vals.length];
        for(int i = 0; i < vals.length; i++) 
            bytes[i] = (byte) vals[i];
        return bytes;
    }

    private static byte[] mergeBytes(byte[][]... bytes) {
        int length = 0;
        for(int i = 0; i < bytes.length; i++) {
            for(int j = 0; j < bytes[i].length; j++) {
                length += bytes[i][j].length;
            }
        }
        byte[] flattenedBytes = new byte[length];
        int pos = 0;
        for(int i = 0; i < bytes.length; i++) {
            byte[] newBytes = mergeBytes(bytes[i]);
            for(int j = 0; j < newBytes.length; j++) {
                flattenedBytes[pos] = newBytes[j];
                pos++;
            }
        }
        return flattenedBytes;
    }

    private static byte[] mergeBytes(byte[]... bytes) {
        int length = 0;
        for(int i = 0; i < bytes.length; i++) {
            length += bytes[i].length;
        }
        byte[] flattenedBytes = new byte[length];
        int pos = 0;
        for(int i = 0; i < bytes.length; i++) {
            for(int j = 0; j < bytes[i].length; j++) {
                flattenedBytes[pos] = bytes[i][j];
                pos++;
            }
        }
        return flattenedBytes;
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
            return STL.mergeBytes(calculateNormalVector().getBytes(), STL.getBytes(Arrays.stream(vertices).map(vertex -> vertex.getBytes()).flatMapToInt(nums -> IntStream.range(0, nums.length).map(i -> nums[i])).toArray()), new byte[2]);
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
