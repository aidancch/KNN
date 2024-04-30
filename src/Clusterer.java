import java.util.ArrayList;
import java.util.Arrays;

public class Clusterer {
    private static boolean FULL_DEBUG = true;//Main.DEBUG;
    private static boolean PROCESS_DEBUG = true, CLUSTER_DEBUG = false, CLASSIFY_DEBUG = true;

    public static void main(String[] args) {
        int numImages = 1000;
        int numSources = 3;
        int variance = 16;
        int clusterIterations = 1000;

        // test

        // 3 different source types:
        // type a:      type b:      type c:
        // 00, FF, 00   FF, FF, 00   88, FF, 00
        // FF, 88, FF   FF, 88, FF   FF, 00, 00
        // FF, 00, FF   FF, FF, 00   88, FF, 00
        int[][][] sources = {
                {
                        {0x00, 0x00, 0x00},
                        {0x00, 0x00, 0x00},
                        {0x00, 0x00, 0x00}
                },
                {
                        {0x80, 0x80, 0x80},
                        {0x80, 0x80, 0x80},
                        {0x80, 0x80, 0x80}
                },
                {
                        {0xFF, 0xFF, 0xFF},
                        {0xFF, 0xFF, 0xFF},
                        {0xFF, 0xFF, 0xFF}
                }
        };

        Image[] images = new Image[numImages];
        int[] realClassifications = new int[numImages];

        if (FULL_DEBUG || PROCESS_DEBUG) System.out.printf("Generating %d images...%n", numImages);

        for (int i = 0; i < images.length; i++) {

            byte[][] pixels = new byte[sources[0].length][sources[0][0].length];
            int type = (int) (Math.random() * numSources);
            realClassifications[i] = type;

            for (int j = 0; j < pixels.length; j++) {
                for (int k = 0; k < pixels[0].length; k++) {
                    int var = (int)(Math.random() * (2 * variance + 1)) - variance;

                    int value = sources[type][j][k] + var;

                    pixels[j][k] = (byte) Math.min(Math.max(0, value), 255);
                }
            }

            images[i] = new Image(pixels);
        }

        Clusterer clusterer = new Clusterer();
        clusterer.cluster(images, 3, clusterIterations);

        if (FULL_DEBUG || CLASSIFY_DEBUG) printClassifications(images, realClassifications);
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

    public void cluster(Image[] images) {
        cluster(images, Math.min(10, images.length), 1000);
    }
    public void cluster(Image[] images, int k, int iterations) {
        assert images.length >= k;

        if(FULL_DEBUG || CLUSTER_DEBUG || PROCESS_DEBUG) {
            System.out.printf("Clustering %d images into %d groups for %d iterations...%n",
                    images.length, k, iterations);
        }

        // bucket array cannot be an array; "You cannot create arrays of parameterized types"
        ArrayList<Image>[] oldBuckets;
        ArrayList<Image>[] buckets = createEmptyBuckets(k);

        for (int i = 0; i < k; i++) {
            buckets[i].add(images[i]);
        }

        double[] means = calculateMeans(buckets);
        fillBuckets(buckets, means, images, k);
        if(FULL_DEBUG || CLUSTER_DEBUG) {
            System.out.println(Arrays.toString(means));
            System.out.printf("sizes: %d, %d, %d%n", buckets[0].size(), buckets[1].size(), buckets[2].size());
        }

        for (int i = 0; i < iterations; i++) {
            means = calculateMeans(buckets);
            oldBuckets = buckets;
            buckets = createEmptyBuckets(k);
            fillBuckets(buckets, means, images, 0);
            if(FULL_DEBUG || CLUSTER_DEBUG) {
                System.out.println(Arrays.toString(means) + " equal? " + equalArrays(oldBuckets, buckets));
                System.out.printf("sizes: %d, %d, %d%n", buckets[0].size(), buckets[1].size(), buckets[2].size());
            }
        }

        if(FULL_DEBUG || CLUSTER_DEBUG || PROCESS_DEBUG) System.out.printf("Clustering complete.%n");
        classifyImagesByBucketIndex(buckets);
    }

    public ArrayList<Image>[] createEmptyBuckets(int k) {
        ArrayList<Image>[] buckets = new ArrayList[k];

        for (int i = 0; i < k; i++) {
            buckets[i] = new ArrayList<>();
        }

        return buckets;
    }
    public double[] calculateMeans(ArrayList<Image>[] buckets) {
        double[] means = new double[buckets.length];

        for (int i = 0; i < means.length; i++) {
            double sum = 0;
            for (Image img : buckets[i]) {
                sum += img.distance();
            }
            means[i] = sum / buckets[i].size();
        }

        return means;
    }
    public void fillBuckets(ArrayList<Image>[] buckets, double[] means, Image[] images, int startIndex) {
        double dist, minDiff;
        int min = 0;
        for (int i = startIndex; i < images.length; i++) {
            dist = images[i].dist();

            minDiff = Math.abs(means[min] - dist);

            for (int j = 1; j < means.length; j++) {
                double diff = Math.abs(means[j] - dist);

                if(diff < minDiff) {
                    min = j;
                    minDiff = diff;
                }
            }

            buckets[min].add(images[i]);
        }
    }
    public boolean equalArrays(ArrayList<Image>[] buckets1, ArrayList<Image>[] buckets2) {
        if(buckets1.length != buckets2.length) return false;

        for (int i = 0; i < buckets1.length; i++) {
            if(buckets1[i].size() != buckets2[i].size()) return false;

            for (int j = 0; j < buckets1[i].size(); j++) {
                if(buckets1[i].get(j) != buckets2[i].get(j)) return false;
            }
        }

        return true;
    }

    public void classifyImagesByBucketIndex(ArrayList<Image>[] buckets) {
        for (int i = 0; i < buckets.length; i++) {
            for(Image img : buckets[i]) {
                img.classify(i);
            }
        }
    }

    private static void printClassifications(Image[] images, int[] realClassifications) {
        int count = 0;
        for (int i = 0; i < images.length; i++) {
            System.out.printf("Image #%d: clustered as %d, really %d. (distance %.2f)%n",
                    i, images[i].digit(), realClassifications[i], images[i].dist());
            if(images[i].digit() == realClassifications[i]) count++;
        }
        System.out.printf("Final count: %d/%d correct (%3.1f%%)%n", count, images.length, count * 100.0 / images.length);
    }

}
