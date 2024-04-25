import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        byte[] arr = new byte[0];
        try {
            FileInputStream fis = new FileInputStream("src/train-images.idx3-ubyte");
            arr = fis.readAllBytes();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println("WTF");
        }
        for (int i = 0; i < 100; i++) {
            System.out.print(arr[i]);
        }
    }
}