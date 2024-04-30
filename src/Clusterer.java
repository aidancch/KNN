import java.util.ArrayList;
import java.util.Arrays;

public class Clusterer {
    private static boolean FULL_DEBUG = false;
    private static boolean PROCESS_DEBUG = Main.DEBUG, CLUSTER_DEBUG = false, CLASSIFY_DEBUG = false;
    private static boolean STATS = true;

    public static void main(String[] args) {
        int numImages = 1000;
        int numSources = 3;
        int variance = 32;
        int clusterIterations = 1000;
        int rows = 3, cols = 3;

        // test

        // 3 different source types:
        // type a:      type b:      type c:
        // 00, FF, 00   FF, FF, 00   88, FF, 00
        // FF, 88, FF   FF, 88, FF   FF, 00, 00
        // FF, 00, FF   FF, FF, 00   88, FF, 00

        byte[][][] sources = TestSet.generateSourceImages(numSources, rows, cols);

//        {
//            {
//                {0x00, 0x00, 0x00},
//                {0x00, 0x00, 0x00},
//                {0x00, 0x00, 0x00}
//            },
//            {
//                {0x80, 0x80, 0x80},
//                {0x80, 0x80, 0x80},
//                {0x80, 0x80, 0x80}
//            },
//            {
//                {0xFF, 0xFF, 0xFF},
//                {0xFF, 0xFF, 0xFF},
//                {0xFF, 0xFF, 0xFF}
//            }
//        };

        Image[] images = TestSet.generateImagesFromSources(numImages, sources, variance);
        int[] realClassifications = new int[numImages];
        for (int i = 0; i < images.length; i++) {
            realClassifications[i] = images[i].digit();
        }

        Clusterer clusterer = new Clusterer();
        clusterer.rows = rows;
        clusterer.cols = cols;
        clusterer.cluster(images, 3, clusterIterations);

        if (FULL_DEBUG || CLASSIFY_DEBUG || STATS) printClassifications(images, realClassifications);
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
    private Image[] means;
    private Image[] images;
    private int rows, cols;

    public void cluster(Image[] images) {
        cluster(images, Math.min(10, images.length), 1000);
    }
    public void cluster(Image[] images, int k, int iterations) {
        assert images.length >= k;

        this.images = images;

        if(FULL_DEBUG || CLUSTER_DEBUG || PROCESS_DEBUG) {
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
        if(FULL_DEBUG || CLUSTER_DEBUG) {
            System.out.printf("Clusterer (Cu): sizes: %d, %d, %d%n", buckets[0].size(), buckets[1].size(), buckets[2].size());
        }

        for (int i = 0; i < iterations; i++) {
            means = calculateMeans();
            oldBuckets = buckets;
            buckets = createEmptyBuckets(k);
            fillBuckets();
            if(equalArrays(oldBuckets, buckets)) {
                if(FULL_DEBUG || CLUSTER_DEBUG || PROCESS_DEBUG) System.out.printf("Clusterer (P/Cu): breaking after %d iterations.%n", i);
                break;
            }
            if(FULL_DEBUG || CLUSTER_DEBUG) {
                System.out.printf("Clusterer (Cu): sizes: %d, %d, %d%n", buckets[0].size(), buckets[1].size(), buckets[2].size());
            }
        }

        if(FULL_DEBUG || CLUSTER_DEBUG || PROCESS_DEBUG) System.out.printf("Clusterer (P): Clustering complete.%n");
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
    public void fillBuckets() {
        fillBuckets(0);
    }
    public void fillBuckets(int startIndex) {
        for (int i = startIndex; i < images.length; i++) {
            buckets[ closestBucket(images[i]) ].add( images[i] );
        }
    }
    public int closestBucket(Image img) {
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
    public double norm(int bucketIndex, Image img) {
        return Math.abs(img.dist(means[bucketIndex]));
    }
    public static boolean equalArrays(ArrayList<Image>[] buckets1, ArrayList<Image>[] buckets2) {
        if(buckets1.length != buckets2.length) return false;

        for (int i = 0; i < buckets1.length; i++) {
            if(buckets1[i].size() != buckets2[i].size()) return false;

            for (int j = 0; j < buckets1[i].size(); j++) {
                if(buckets1[i].get(j) != buckets2[i].get(j)) return false;
            }
        }

        return true;
    }

    public void classifyImagesByBucketIndex() {
        if(FULL_DEBUG || CLUSTER_DEBUG || CLASSIFY_DEBUG || PROCESS_DEBUG) System.out.println("Clusterer (Any): Classifying images by bucket index...");
        for (int i = 0; i < buckets.length; i++) {
            for(Image img : buckets[i]) {
                img.classify(i);
            }
        }
    }

    private static void printClassifications(Image[] images, int[] realClassifications) {
        int count = 0;
        for (int i = 0; i < images.length; i++) {
            if(FULL_DEBUG || CLASSIFY_DEBUG) System.out.printf("Clusterer (Ca): Image #%d: clustered as category %d, is a %d. (distance %.2f)%n",
                    i, images[i].digit(), realClassifications[i], images[i].dist());
            if(images[i].digit() == realClassifications[i]) count++;
        }
    }

    public void analyzeBuckets(int[] realClassifications) {
        int num = 0;
        for(ArrayList<Image> bucket : buckets) {
            int countA = 0, countB = 0, countC = 0;
            for(Image img : bucket){
                switch(realClassifications[img.id()]) {
                    case 0 -> countA++;
                    case 1 -> countB++;
                    case 2 -> countC++;
                }
            }
            System.out.printf("Clusterer: Bucket %d: %4d 0, %4d 1, %4d 2, for %4d total%n",
                    num, countA, countB, countC, bucket.size());
            num++;
        }
    }

}
