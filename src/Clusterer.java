import java.util.ArrayList;
import java.util.Arrays;

public class Clusterer {

    public static void main(String[] args) {
        Image[] images = new Image[1000];
        for (int i = 0; i < images.length; i++) {
            images[i] = new Image(new byte[3][3]);
        }

        Clusterer clusterer = new Clusterer();

        clusterer.cluster(images, 10);
    }

    // takes in data and creates labels from it

    // take k-first images
    // for all images calculate their distance to the first images in each bucket
    // add the images to the bucket they're closest to
    // begin iteration:
    //  calculate mean distance (Cg?) of entire bucket from a blank image
    //  drop images in buckets their distance is closest to
    // continue until "stabilized" -> very few images move buckets each iteration

    public ArrayList<Image>[] cluster(Image[] images) {
        return cluster(images, Math.min(10, images.length));
    }
    public ArrayList<Image>[] cluster(Image[] images, int k) {
        assert images.length >= k;

        // bucket array cannot be an array; "You cannot create arrays of parameterized types"
        ArrayList<Image>[] buckets = createBuckets(k);

        for (int i = 0; i < k; i++) {
            buckets[i].add(images[i]);
        }

        double[] means = calculateMeans(buckets);

        fillBuckets(buckets, means, images, k);

        for (int i = 0; i < 100; i++) {
            calculateMeans(buckets);
            buckets = createBuckets(k);
            fillBuckets(buckets, means, images, 0);
        }

        return buckets;
    }

    public ArrayList<Image>[] createBuckets(int k) {
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
        double dist;
        int min = 0;
        for (int i = startIndex; i < images.length; i++) {
            dist = images[i].dist();

            for (int j = 0; j < means.length; j++) {
                if(Math.abs(means[i] - dist) < Math.abs(means[min] - dist)) {
                    min = i;
                }
            }

            buckets[min].add(images[i]);
        }
    }

}
