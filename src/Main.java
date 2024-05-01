import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static boolean DEBUG = false, PRINT_PROCESS = true;

    public static void main(String[] args) {
        Image[] images = null;
        int[] correctDigits;

        try {
            images = Reader.read("src/train-images.idx3-ubyte", "src/train-labels.idx1-ubyte");
            correctDigits = new int[images.length];
            for (int i = 0; i < images.length; i++) {
                correctDigits[i] = images[i].digit();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Image.downsize(images, 2);

//        Clusterer clusterer = new Clusterer(images, 10, images[0].rows(), images[0].columns());
//        clusterer.cluster(100 );
//        clusterer.analyzeBuckets(correctDigits);
//        Image[] bucket = clusterer.getBucket(6);

//        Viewer.view(bucket, 10);
        Viewer.view(images,20);
    }
}