import java.io.*;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static boolean DEBUG = false, PRINT_PROCESS = true;
    public static int kMeans = 10, kNN = 3, iterations = 0, tolerance = -1, viewScale = 10;
    public static String imageFileName = "src/train-images.idx3-ubyte", labelFileName = "src/train-labels.idx1-ubyte";

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

//        Image.downsize(images, 2);

        Clusterer clusterer = new Clusterer(images, kMeans, images[0].rows(), images[0].columns());
        clusterer.cluster(iterations, tolerance);
        clusterer.analyzeBuckets(correctDigits);
        int correct = clusterer.accuracy(correctDigits);
        System.out.printf("Accuracy: %d / %d correct, (%.2f%%)%n", correct, images.length, correct * 100.0 / images.length);

        if(viewScale > 0) {
//            Viewer.view(clusterer.getMeans(), viewScale);
//
//            Image[] bucket = clusterer.getBucket((int)(Math.random() * kMeans));
//            Viewer.view(bucket, viewScale);

            Viewer.view(images, viewScale);
        }
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