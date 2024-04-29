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

    public int[] cluster(Image[] images) {
        return cluster(images, Math.min(10, images.length));
    }
    public int[] cluster(Image[] images, int k) {
        assert images.length >= k;

        // bucket array cannot be an array; "You cannot create arrays of parameterized types"
        ArrayList<Image>[] buckets = createBuckets(k);

        for (int i = 0; i < k; i++) {
            buckets[i].add(images[i]);
        }

        return null;
    }

    public ArrayList<Image>[] createBuckets(int k) {
        ArrayList<Image>[] buckets = new ArrayList[k];

        for (int i = 0; i < k; i++) {
            buckets[i] = new ArrayList<Image>();
        }

        return buckets;
    }

}
