import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

public class ImageTest {
    public static boolean DEBUG = false, PRINT_PROCESS = true;
    public static int kMeans = 10, kNN = 3, iterations = 0, tolerance = -1, viewScale = 10;
    public static String imageFileName = "train-images.idx3-ubyte", labelFileName = "train-labels.idx1-ubyte";

    public static Random random = new Random(123);

    public static void main(String[] args) {
        Image[] images = new Image[0];
        int[] correctDigits = new int[0];

        parseArgs(args);

        try {
            if(imageFileName != null && labelFileName != null) {

                images = Reader.read(imageFileName, labelFileName);
                correctDigits = new int[images.length];
                for (int i = 0; i < images.length; i++) {
                    correctDigits[i] = images[i].digit();
                }

            } else if (imageFileName != null) {
                images = Reader.read(imageFileName);
            } else {
                throw new FileNotFoundException("Cannot read null file / cannot run on only label file");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        Image.threshold(images, 128);

//        Image.downsize(images, 2);

        Image[] test = new Image[23];

        for (int i = 0; i < test.length; i++) {
            test[i] = images[random.nextInt(60000)];
        }

//        Image.despeckle(images, 8);

        Image.deskew(test);

        Viewer.view(test, viewScale);

    }

    public static void parseArgs(String[] args) {
        for(int i = 0; i < args.length; i++) {
            switch(args[i]) {
                case "-km", "-kmeans", "-buckets", "-clusters" -> kMeans = Integer.parseInt(args[++i]);
                case "-kn", "-knn", "-kneighbors" -> kNN = Integer.parseInt(args[++i]);
                case "-i", "-iterations" -> iterations = Integer.parseInt(args[++i]);
                case "-t", "-tolerance" -> tolerance = Integer.parseInt(args[++i]);

                case "-f", "-img", "-images" -> imageFileName = args[++i];
                case "-l", "-train", "-labels" -> labelFileName = args[++i];

                case "-s", "-scale", "-view" -> viewScale = Integer.parseInt(args[++i]);
            }
        }
    }
}
