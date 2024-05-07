import java.util.ArrayList;
import java.util.Arrays;

public class Clusterer {
    private static final boolean
            DEBUG = Main.DEBUG,
            FULL_DEBUG = false,
            PROCESS_DEBUG = Main.PRINT_PROCESS,
            CLUSTER_DEBUG = false,
            CLASSIFY_DEBUG = false,
            STATS = true;

    public static void main(String[] args) {
        int numImages = 1000;
        int numSources = 5;
        int variance = 16;
        int clusterIterations = 1000;
        int rows = 3, cols = 3;
        TestSet.setSeed(2024);

        // source types:
        byte[][][] sources = TestSet.generateSourceImages(numSources, rows, cols);
//        {
//            {
//                {(byte)0x00, (byte)0xFF, (byte)0x00},
//                {(byte)0x00, (byte)0xFF, (byte)0x00},
//                {(byte)0x00, (byte)0xFF, (byte)0x00}
//            },
//            {
//                {(byte)0xFF, (byte)0xFF, (byte)0x00},
//                {(byte)0x00, (byte)0xFF, (byte)0x00},
//                {(byte)0x00, (byte)0xFF, (byte)0xFF}
//            },
//            {
//                {(byte)0xFF, (byte)0xFF, (byte)0x00},
//                {(byte)0xFF, (byte)0xFF, (byte)0x00},
//                {(byte)0xFF, (byte)0xFF, (byte)0x00}
//            }
//        };

        Image[] images = TestSet.generateImagesFromSources(numImages, sources, variance);
        int[] realClassifications = new int[numImages];
        for (int i = 0; i < images.length; i++) {
            realClassifications[i] = images[i].digit();
        }

        Clusterer clusterer = new Clusterer(images, numSources, rows, cols);
        clusterer.cluster(clusterIterations, 0);

        if (DEBUG && (FULL_DEBUG || CLASSIFY_DEBUG || STATS)) printClassifications(images, realClassifications);
        clusterer.analyzeBuckets(realClassifications);
        Viewer.view(images, 50);
    }

    // takes in data and creates labels from it

    // take k-first images
    // for all images calculate their distance to the first images in each bucket
    // add the images to the bucket they're closest to
    // begin iteration:
    //  calculate mean distance (Cg?) of entire bucket from a blank image
    //  drop images in buckets their distance is closest to
    // continue until "stabilized" -> very few images move buckets each iteration

    private int[] buckets;
    private int k;
    private Image[] means;
    private Image[] images;
    private int rows, cols;

    public Image[] getBucket(int i) {
        ArrayList<Image> bucket = new ArrayList<>();

        for (int j = 0; j < buckets.length; j++) {
            if (buckets[j] == i) bucket.add(images[j]);
        }

        Image[] imgs = new Image[bucket.size()];
        return bucket.toArray(imgs);
    }
    public Image[] getMeans() {
        return means;
    }

    public Clusterer() {}

    public Clusterer(Image[] images, int k) {
        this.images = images;
        this.rows = images[0].rows();
        this.cols = images[0].columns();
        this.k = k;
        this.means = new Image[k];
        this.buckets = new int[images.length];
    }

    public Clusterer(Image[] images, int k, int rows, int cols) {
        this.images = images;
        this.rows = rows;
        this.cols = cols;
        this.k = k;
        this.means = new Image[k];
        this.buckets = new int[images.length];
    }

    public void cluster(Image[] images) {
        this.images = images;
        this.k = Math.min(10, images.length);
        this.buckets = new int[images.length];
        cluster(1000, images.length / 1000);
    }

    public void cluster(int iterations) {
        cluster(iterations, -1);
    }

    public void cluster(int iterations, int tolerance) {
        assert images != null && k != 0 && buckets != null;
        if (tolerance == -1) tolerance = images.length / 1000;

        if (DEBUG || PROCESS_DEBUG || FULL_DEBUG || CLUSTER_DEBUG) {
            System.out.printf("Clusterer (P): Clustering %d images into %d groups for %d iterations...%n",
                    images.length, k, iterations);
        }

        // bucket array cannot be an array; "You cannot create arrays of parameterized types"
        buckets = new int[images.length];

        for (int i = 0; i < k; i++) {
            buckets[i] = i;
            means[i] = images[i];
        }

        calculateMeans();
        fillBuckets(k);

        for (int i = 0; ; i++) {
            calculateMeans();
            int difference = fillBuckets();

            if (difference <= tolerance) {
                if (DEBUG || PROCESS_DEBUG || FULL_DEBUG || CLUSTER_DEBUG)
                    System.out.printf("Clusterer (P/Cu): Tolerance met. Breaking after %d iterations.%n", i);
                break;
            }
            if(iterations > 0 && i >= iterations) {
                if (DEBUG || PROCESS_DEBUG || FULL_DEBUG || CLUSTER_DEBUG)
                    System.out.printf("Clusterer (P/Cu): %d Iterations met.%n", iterations);
                break;
            }

            if ((iterations / 100) != 0 && (i % (iterations / 100) == 0 && (DEBUG || PROCESS_DEBUG))) {
                System.out.printf("Clusterer (P): %02.2f%%, current difference: %d%n", (i+1) * 100.0 / iterations, difference);
            }
        }

        if (DEBUG || PROCESS_DEBUG || FULL_DEBUG || CLUSTER_DEBUG)
            System.out.printf("Clusterer (P): Clustering complete.%n");
        classifyImagesByBucketIndex();
    }

    public void calculateMeans() {
        for (int bucket = 0; bucket < k; bucket++) {
            if (FULL_DEBUG || CLUSTER_DEBUG) System.out.printf("Calculating the mean for cluster %d%n", bucket);

            int[][] mean = new int[rows][cols];
            int count = 0;

            for (int image = 0; image < buckets.length; image++) {

                if (buckets[image] == bucket) {
                    count++;

                    for (int row = 0; row < rows; row++) {
                        for (int col = 0; col < cols; col++) {
                            mean[row][col] += images[image].get(row, col);
                        }
                    }

                }

            }

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    mean[row][col] /= count;
                }
            }

            means[bucket] = new Image(mean);
        }
    }

    private int fillBuckets() {
        return fillBuckets(0);
    }

    private int fillBuckets(int startIndex) {
        int difference = 0;

        for (int i = startIndex; i < images.length; i++) {
            int closest = closestBucket(images[i]);

            if (closest != buckets[i]) difference++;

            buckets[i] = closest;
        }

        return difference;
    }

    private int closestBucket(Image img) {
        int min = 0;
        double minDiff = Double.MAX_VALUE;

        for (int i = 0; i < means.length; i++) {
            double diff = norm(i, img);

            if (diff < minDiff) {
                min = i;
                minDiff = diff;
            }
        }

        return min;
    }

    private double norm(int bucketIndex, Image img) {
        return Math.abs(img.dist(means[bucketIndex]));
    }

    private double dist(int bucketIndex, Image img) {
        return Math.abs(means[bucketIndex].dist() - img.dist());
    }

    private static boolean equalArrays(ArrayList<Image>[] buckets1, ArrayList<Image>[] buckets2) {
        if (buckets1.length != buckets2.length) return false;

        for (int i = 0; i < buckets1.length; i++) {
            if (buckets1[i].size() != buckets2[i].size()) return false;

            for (int j = 0; j < buckets1[i].size(); j++) {
                if (buckets1[i].get(j) != buckets2[i].get(j)) return false;
            }
        }

        return true;
    }

    private static int difference(ArrayList<Image>[] buckets1, ArrayList<Image>[] buckets2) {
        assert buckets1.length == buckets2.length;

        int count = 0;
        for (int i = 0; i < buckets1.length; i++) {
            for (int j = 0; j < Math.max(buckets1[i].size(), buckets1[i].size()); j++) {
                if (j >= buckets1[i].size()) count += buckets2[i].size() - j;
                if (j >= buckets2[i].size()) count += buckets1[i].size() - j;

                if (buckets1 != buckets2) count++;
            }
        }

        return count;
    }

    public void classifyImagesByBucketIndex() {
        if (DEBUG || PROCESS_DEBUG) System.out.println("Clusterer (Any): Classifying images by bucket index...");
        for (int i = 0; i < buckets.length; i++) {
            images[i].classify(buckets[i]);
        }
    }

    public void classifyImagesByNearestDigit(int[] realClassifications) {
        for (int bucket = 0; bucket < k; bucket++) {
            Image[] imgs = getBucket(bucket);

            for (Image img : imgs) {
                img.classify(realClassifications[imgs[0].id()]);
            }
        }
    }

    private static void printClassifications(Image[] images, int[] realClassifications) {
        int count = 0;
        for (int i = 0; i < images.length; i++) {
            if (DEBUG && (FULL_DEBUG || CLASSIFY_DEBUG))
                System.out.printf("Clusterer (Ca): Image #%d: clustered as category %d, is a %d. (distance %.2f)%n",
                        i, images[i].digit(), realClassifications[i], images[i].dist());
            if (images[i].digit() == realClassifications[i]) count++;
        }
    }

    public void analyzeBuckets(int[] realClassifications) {
        for (int bucket = 0; bucket < k; bucket++) {
            Image[] imgs = getBucket(bucket);

            int[] counts = new int[k];
            for (Image img : imgs) {
                counts[realClassifications[img.id()]]++;
            }

            int max = 0;
            for (int j = 1; j < counts.length; j++) {
                if (counts[j] > counts[max]) max = j;
            }
            System.out.printf("Clusterer: Bucket %d: %d mode, total %d " + Arrays.toString(counts) + "%n",
                    bucket, max, imgs.length);
        }
    }

    public int accuracy(int[] realClassifications) {
        classifyImagesByNearestDigit(realClassifications);

        int correct = 0;
        for (int i = 0; i < images.length; i++) {
            if(images[i].digit() == realClassifications[i]) correct++;
        }
        return correct;
    }

}
