import java.util.Random;

public class TestSet {

    private static boolean DEBUG = Main.DEBUG;

    private static final Random rand = new Random();
    private static long seed = rand.nextLong();
    {
        rand.setSeed(seed);
    }

    public static void setSeed(long seed) {
        TestSet.seed = seed;
        rand.setSeed(seed);
    }

    public static byte[][][] generateSourceImages(int number, int rows, int cols) {
        if(DEBUG) System.out.printf("TestSet: generating %d new source images...%n", number);
        byte[][][] sourceImages = new byte[number][cols][rows];

        for (int i = 0; i < number; i++) {
            for (int j = 0; j < cols; j++) {
                for (int k = 0; k < rows; k++) {
                    sourceImages[i][j][k] = (byte)rand.nextInt();
                }
            }
        }
        if(DEBUG) printSources(sourceImages);

        return sourceImages;
    }

    public static Image[] generateImagesFromSources(int number, byte[][][] sources, int variance) {

        if(DEBUG) System.out.printf("TestSet: generating %d images from %d sources%n", number, sources.length);

        Image[] images = new Image[number];

        for (int i = 0; i < number; i++) {

            byte[][] pixels = new byte[sources[0].length][sources[0][0].length];
            int source = (int) (Math.random() * sources.length);

            for (int j = 0; j < pixels.length; j++) {
                for (int k = 0; k < pixels[0].length; k++) {
                    int var = (int)(Math.random() * (2 * variance + 1)) - variance;

                    int value = sources[source][j][k] + var;

                    pixels[j][k] = (byte) Math.min(Math.max(0, value), 255);
                }
            }

            images[i] = new Image(pixels, source);

        }

        return images;
    }

    public static Image[] generateImages(int numberSources, int rows, int cols, int numberImages, int variance) {
        return generateImagesFromSources(numberImages, generateSourceImages(numberSources, rows, cols), variance);
    }

    public static void printSources(byte[][][] sources) {
        System.out.println("TestSet: generated new source images: ");

        for (int i = 0; i < sources[0][0].length; i++) {
            for (byte[][] source : sources) {
                for (byte[] col : source) {
                    System.out.printf("%3d ", col[i] & 0xFF);
                }
                System.out.print("  ");
            }
            System.out.println();
        }
    }

}
