import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KNNClassifier {
    public static boolean DEBUG = true, PRINT_PROCESS = true;
    private static boolean doKmeanCluster = false;      // true: process with k-mean clustering
    private static int iteration = 100;                 // the number of iterations in k-mean clustering
    private static int kValue = 10;                     // the value of k

    // TODO: MOVE TO TOP MAIN in some file called KNN.java, MERGE WITH OHTERS
    public static int checkValue(String arg, int lo, int hi) {
        int value = Integer.parseInt(arg);
        if (value < lo || value > hi) {
            throw new IllegalArgumentException();
        }
        return value;
    }
    public static void main(String[] args) {
        // parse arguments
        for (int i = 0; i < args.length; i++) {
            try {
                switch (args[i]) {
                    case "-cluster":
                        doKmeanCluster = true;
                        break;
                    case "-iteration":
                        if (i + 1 < args.length) {
                            iteration = checkValue(args[i + 1], 1, 255);
                            i++;
                        } else {
                            throw new IllegalArgumentException();
                        }
                        break;
                    case "-k":
                        if (i + 1 < args.length) {
                            kValue = checkValue(args[i + 1], 1, 255);
                            i++;
                        } else {
                            throw new IllegalArgumentException();
                        }
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
            } catch (IllegalArgumentException e) {
                System.err.println("ERROR: Invalid value for " + args[i]);
            }
        }

        System.out.println("Process with k-mean clustering: " + (doKmeanCluster ? "YES" : "NO")
                + ", iteration : " + iteration + ", k : " + kValue + "\n");

        // Step 1: Load the dataset
        Image[] trainData = null;
        Image[] testData = null;
        try {
            trainData = Reader.read("src/train-images.idx3-ubyte", "src/train-labels.idx1-ubyte");
            testData = Reader.read("src/t10k-images.idx3-ubyte", "src/t10k-labels.idx1-ubyte");
        } catch (IOException e) {
            System.out.println("ERROR: IOException in file read");
            System.exit(1);
        }

        // Step 2: Cluster the training dataset using k-means
        // The actual value of a training image is already stored as image.digit when reading the label file.
        // If doKmeanCluster == true, the "digit" field of each image will be updated in cluster labeling;
        // else doKmeanCluster == false, KNN classifier will rely on the original actual values.
        if (doKmeanCluster) {
            int[] correctDigits;
            correctDigits = new int[trainData.length];
            for (int i = 0; i < trainData.length; i++) {
                correctDigits[i] = trainData[i].digit();
            }
            int[] realClassifications = new int[trainData.length];
            for (int i = 0; i < trainData.length; i++) {
                realClassifications[i] = trainData[i].digit();
            }

            Clusterer clusterer = new Clusterer(trainData, kValue, trainData[0].rows(), trainData[0].columns());

            clusterer.cluster(iteration);        // TODO: fix classifyImagesByBucketIndex() ??

            if (DEBUG) Clusterer.printClassifications(trainData, realClassifications);

            clusterer.analyzeBuckets(correctDigits);
            /*
            Image[] bucket = clusterer.getBucket(6);
            Viewer.view(bucket, 10);
             */

            // manually assign labels to each cluster
            int[] assignedLabels = {9, 0, 1, 8, 2, 3, 7, 5, 4, 6};
            if (DEBUG) System.out.println("Assign to each cluster with the labels " + assignedLabels.toString());
            for (int i = 0; i < kValue; i++) {
                Image[] bucket = clusterer.getBucket(i);
                Viewer.view(bucket, 10);
                for (Image img : clusterer.getBucket(i)) {
                    img.classify(assignedLabels[i]);
                }
            }
            if (DEBUG) Clusterer.printClassifications(trainData, realClassifications);

        }

        // Step 3: Implement KNN classifier
        KNNClassifier knn = new KNNClassifier();
        knn.train(trainData);

        // Step 4: Test the classifier on the testing dataset and record accuracy
        int testLimit = 100; //testData.length;    // debug only, set to small number
        int correct = 0;
        for (int i = 0; i < testData.length; i++) {
            if (i >= testLimit) break;       // debug only

            int predictedLabel = knn.predict(testData[i], kValue);
            if (predictedLabel == testData[i].digit()) {
                correct++;
            } else {
                System.out.println("ERR: test " + "i = " + i + " l = " + testData[i].digit() + " p = " + predictedLabel);
            }
        }
//        double accuracy = (double) correct / testData.length * 100;
        double accuracy = (double) correct / testLimit * 100;
        System.out.println("KNN Accuracy: " + accuracy + "%" + " with #trainData " + trainData.length + " #testData " + testData.length + " testLimit " + testLimit + " correct " + correct);
    }

    // KNN classifier:
    // during training, KNN simply stores all the training data points along with their corresponding class labels.
    // During prediction, it calculates distances between the new data point and all training data points, selects
    // the K nearest neighbors, and assigns the most common class label based on majority voting among these neighbors.
    // Finally, it returns the predicted class label for the new data point.

    private Image[] trainedData;

    // Constructor
    public KNNClassifier() {
        this.trainedData = null;
    }
    public KNNClassifier(Image[] trainData) { this.trainedData = trainData; }

    public void train(Image[] trainData) {
        this.trainedData = trainData;
    }

    public int predict(Image image, int k) {
        // Calculate distances between image and all training data points
        List<DistanceLabelPair> distances = new ArrayList<>();
        for (Image trainedImage : trainedData) {
            double distance = image.distance(trainedImage);
            distances.add(new DistanceLabelPair(distance, trainedImage.digit()));
        }

        // Select K nearest neighbors
        Collections.sort(distances); // Sort distances in ascending order
        int[] voteCount = new int[10]; // Count of votes for each label (0-9)
        for (int i = 0; i < k; i++) {
            int label = distances.get(i).getLabel();
            voteCount[label]++;
        }

        // Implement majority voting to assign the class label to image
        int maxVoteIndex = 0;
        for (int i = 1; i < 10; i++) {
            if (voteCount[i] > voteCount[maxVoteIndex]) {
                maxVoteIndex = i;
            }
        }

        return maxVoteIndex; // Return the predicted label
    }
}

class DistanceLabelPair implements Comparable<DistanceLabelPair> {
    private final double distance;
    private final int label;

    public DistanceLabelPair(double distance, int label) {
        this.distance = distance;
        this.label = label;
    }

    public double getDistance() {
        return distance;
    }

    public int getLabel() {
        return label;
    }

    @Override
    public int compareTo(DistanceLabelPair other) {
        return Double.compare(this.distance, other.distance);
    }
}
