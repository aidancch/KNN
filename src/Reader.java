import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class Reader {

    public static Image[] read(String imageFileName) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(imageFileName)));
        int magicNumber = dataInputStream.readInt();
        int numberOfImages = dataInputStream.readInt();
        int rows = dataInputStream.readInt();
        int cols = dataInputStream.readInt();

        System.out.println("magic number is " + magicNumber);
        System.out.println("number of images is " + numberOfImages);
        System.out.println("number of rows is: " + rows);
        System.out.println("number of cols is: " + cols);

        Image[] images = new Image[numberOfImages];

        for(int i = 0; i < numberOfImages; i++) {
            Image image = new Image(rows, cols);
            for (int j = 0; j < rows; j++) {
                for (int k = 0; k < cols; k++) {
                    image.set(j, k, dataInputStream.readUnsignedByte());
                }
            }
            images[i] = image;
        }
        dataInputStream.close();
        return images;
    }

    public static Image[] read(String imageFileName, String labelFileName) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(imageFileName)));
        int magicNumber = dataInputStream.readInt();
        int numberOfImages = dataInputStream.readInt();
        int rows = dataInputStream.readInt();
        int cols = dataInputStream.readInt();

        System.out.println("magic number is " + magicNumber);
        System.out.println("number of images is " + numberOfImages);
        System.out.println("number of rows is: " + rows);
        System.out.println("number of cols is: " + cols);

        DataInputStream labelInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(labelFileName)));
        int labelMagicNumber = labelInputStream.readInt();
        int numberOfLabels = labelInputStream.readInt();

        System.out.println("labels magic number is: " + labelMagicNumber);
        System.out.println("number of labels is: " + numberOfLabels);

        Image[] images = new Image[numberOfImages];

        assert numberOfImages == numberOfLabels;

        for(int i = 0; i < numberOfImages; i++) {
            Image image = new Image(rows, cols);
            image.setDigit(labelInputStream.readUnsignedByte());
            for (int j = 0; j < rows; j++) {
                for (int k = 0; k < cols; k++) {
                    image.set(j, k, dataInputStream.readUnsignedByte());
                }
            }
            images[i] = image;
        }
        dataInputStream.close();
        labelInputStream.close();
        return images;
    }
}