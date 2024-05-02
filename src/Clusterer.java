import java.util.ArrayList;
import java.util.Arrays;

public class Clusterer {
    private static final boolean
            DEBUG = Main.DEBUG,
            FULL_DEBUG = false,
            PROCESS_DEBUG = Main.PRINT_PROCESS,
            CLUSTER_DEBUG = true,
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

    private ArrayList<Image>[] buckets;
    private int k;
    private Image[] means;
    private Image[] images;
    private int rows, cols;

    public Image[] getBucket(int i) {
        Image[] imgs = new Image[buckets[i].size()];
        buckets[i].toArray(imgs);
        return imgs;
    }

    public Clusterer() {}
    public Clusterer(Image[] images, int k) {
        this.images = images;
        this.k = k;
        this.buckets = new ArrayList[k];
        this.rows = images[0].rows();
        this.cols = images[0].columns();
    }
    public Clusterer(Image[] images, int k, int rows, int cols){
        this.images = images;
        this.rows = rows;
        this.cols = cols;
        this.k = k;
        this.buckets = new ArrayList[k];
    }

    public void cluster(Image[] images) {
        this.images = images;
        this.k = Math.min(10, images.length);
        this.buckets = new ArrayList[k];
        cluster(1000, images.length / 1000);
    }
    public void cluster(int interactions) {
        cluster(interactions, -1);
    }
    public void cluster(int iterations, int tolerance) {
        assert images != null && k != 0 && buckets != null;
        if(tolerance == -1) tolerance = images.length / 1000;

        if((DEBUG || PROCESS_DEBUG) && (FULL_DEBUG || CLUSTER_DEBUG)) {
            System.out.printf("Clusterer (P): Clustering %d images into %d groups for %d iterations...%n",
                    images.length, k, iterations);
        }

        // bucket array cannot be an array; "You cannot create arrays of parameterized types"
        ArrayList<Image>[] oldBuckets;
        buckets = createEmptyBuckets(k);

        for (int i = 0; i < k; i++) {
            buckets[i].add(images[i]);
        }

        means = calculateMeans();
        fillBuckets(k);
        if(DEBUG && (FULL_DEBUG || CLUSTER_DEBUG)) {
            System.out.printf("Clusterer (Cu): sizes: %d, %d, %d%n", buckets[0].size(), buckets[1].size(), buckets[2].size());
        }

        for (int i = 0; i < iterations; i++) {
            means = calculateMeans();
            oldBuckets = buckets;
            buckets = createEmptyBuckets(k);
            fillBuckets();
            int difference = difference(oldBuckets, buckets);
            if(difference <= tolerance) {
                if((DEBUG || PROCESS_DEBUG) && (FULL_DEBUG || CLUSTER_DEBUG)) System.out.printf("Clusterer (P/Cu): Tolerance met. Breaking after %d iterations.%n", i);
                break;
            }
            if( (iterations / 100) != 0 && (i % (iterations / 100) == 0 && (DEBUG || PROCESS_DEBUG)) ) {
                System.out.printf("Clusterer (P): %.2f%%, current difference: %d%n", i * 100.0 / iterations, difference);
            }
            if(DEBUG && (FULL_DEBUG || CLUSTER_DEBUG)) {
                System.out.printf("Clusterer (Cu): sizes: %d, %d, %d%n", buckets[0].size(), buckets[1].size(), buckets[2].size());
            }
        }

        if((DEBUG || PROCESS_DEBUG) && (FULL_DEBUG || CLUSTER_DEBUG)) System.out.printf("Clusterer (P): Clustering complete.%n");
        classifyImagesByBucketIndex();
    }

    public ArrayList<Image>[] createEmptyBuckets(int k) {
        ArrayList<Image>[] buckets = new ArrayList[k];

        for (int i = 0; i < k; i++) {
            buckets[i] = new ArrayList<>();
        }

        return buckets;
    }
    public Image[] calculateMeans() {
        Image[] means = new Image[buckets.length];

        // for each bucket
        for (int i = 0; i < means.length; i++) {
            int[][] mean = new int[cols][rows];
            for(Image img : buckets[i]) {
                // sum the pixel values
                for (int j = 0; j < cols; j++) {
                    for (int k = 0; k < rows; k++) {
                        mean[j][k] += img.get(k, j);
                    }
                }
            }

            byte[][] img = new byte[cols][rows];
            for (int j = 0; j < cols; j++) {
                for (int k = 0; k < rows; k++) {
                    img[j][k] = (byte)(mean[j][k] / Math.max(buckets[i].size(), 1));
                }
            }

            means[i] = new Image(img);
        }

        return means;
    }
    private void fillBuckets() {
        fillBuckets(0);
    }
    private void fillBuckets(int startIndex) {
        for (int i = startIndex; i < images.length; i++) {
            buckets[ closestBucket(images[i]) ].add( images[i] );
        }
    }
    private int closestBucket(Image img) {
        int min = 0;
        double minDiff = Double.MAX_VALUE;

        for (int i = 0; i < means.length; i++) {
            double diff = norm(i, img);

            if(diff < minDiff) {
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
        if(buckets1.length != buckets2.length) return false;

        for (int i = 0; i < buckets1.length; i++) {
            if(buckets1[i].size() != buckets2[i].size()) return false;

            for (int j = 0; j < buckets1[i].size(); j++) {
                if(buckets1[i].get(j) != buckets2[i].get(j)) return false;
            }
        }

        return true;
    }
    private static int difference(ArrayList<Image>[] buckets1, ArrayList<Image>[] buckets2) {
        assert buckets1.length == buckets2.length;

        int count = 0;
        for (int i = 0; i < buckets1.length; i++) {
            for (int j = 0; j < Math.max(buckets1[i].size(), buckets1[i].size()); j++) {
                if(j >= buckets1[i].size()) count += buckets2[i].size() - j;
                if(j >= buckets2[i].size()) count += buckets1[i].size() - j;

                if(buckets1 != buckets2) count++;
            }
        }

        return count;
    }

    private void classifyImagesByBucketIndex() {
        if(DEBUG || PROCESS_DEBUG) System.out.println("Clusterer (Any): Classifying images by bucket index...");
        for (int i = 0; i < buckets.length; i++) {
            for(Image img : buckets[i]) {
                img.classify(i);
            }
        }
    }

    private static void printClassifications(Image[] images, int[] realClassifications) {
        int count = 0;
        for (int i = 0; i < images.length; i++) {
            if(DEBUG && (FULL_DEBUG || CLASSIFY_DEBUG)) System.out.printf("Clusterer (Ca): Image #%d: clustered as category %d, is a %d. (distance %.2f)%n",
                    i, images[i].digit(), realClassifications[i], images[i].dist());
            if(images[i].digit() == realClassifications[i]) count++;
        }
    }

    public void analyzeBuckets(int[] realClassifications) {
        int num = 0;
        for(int i = 0; i < buckets.length; i++) {
            int[] counts = new int[buckets.length];
            for(Image img : buckets[i]){
                counts[realClassifications[img.id()]]++;
            }
            int max = 0;
            for (int j = 1; j < counts.length; j++) {
                if(counts[j] > counts[max]) max = j;
            }
            System.out.printf("Clusterer: Bucket %d: %d mode, total %d " + Arrays.toString(counts) + "%n",
                    num, max, buckets[i].size());
            num++;
        }
    }

}
