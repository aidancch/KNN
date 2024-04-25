import java.awt.Color;
import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Canvas {

    public static final int WHITE = 0;
    public static final int BLACK = 255;
    private static final int THRESHOLD = 128;

    private int rows;
    private int columns;
    private int[][] canvas;

    public Canvas(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.canvas = new int[rows][columns];
    }

    public int rows()    { return this.rows; }
    public int columns() { return this.columns; }

    public boolean isOnCanvas(int row, int column) {
        return (row >= 0 && row < this.rows) &&
                (column >= 0 && column < this.columns);
    }

    public int get(int row, int column) {
        if (isOnCanvas(row, column)) {
            return this.canvas[row][column];
        } else {
            return WHITE;
        }
    }

    public void set(int row, int column, int value) {
        if (isOnCanvas(row, column)) {
            this.canvas[row][column] = value;
        }
    }

    public void clear(int row, int column) {
        set(row, column, WHITE);
    }

    public void clear() {
        for (int row = 0; row < this.rows; row++) {
            for (int column = 0; column < this.columns; column++) {
                this.canvas[row][column] = WHITE;
            }
        }
    }

    public Canvas crop(BoundingBox bounds, int margin) {
        int height = bounds.height() + 2 * margin;
        int width = bounds.width() + 2 * margin;
        int left = bounds.left();
        int top = bounds.top();
        Canvas result = new Canvas(height, width);

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                result.set(row+margin, col+margin, this.get(left+row, top+col));
            }
        }
        return result;
    }

    public Canvas crop(int margin) {
        return crop(new BoundingBox(this), margin);
    }

    public int total(int top, int left, int height, int width) {
        int sum = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                sum += this.get(top+i, left+j);
            }
        }
        return sum;
    }

    public int total() {
        return total(0, 0, rows, columns);
    }

    private static Color grayToColor(int shade) {
        shade = 255 - shade;
        return new Color(shade, shade, shade);
    }

    private static int RGBtoGray(int rgb) {
        return 255 - (rgb % 256);
    }

    private BufferedImage getBufferedImage() {
        BufferedImage result = new BufferedImage(columns, rows, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = result.createGraphics();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                g.setColor(grayToColor(this.get(row, col)));
                g.fillRect(col, row, 1, 1);
            }
        }
        return result;
    }

    public Canvas resize(int height, int width) {
        java.awt.Image image = this.getBufferedImage().getScaledInstance(width, height, Image.SCALE_DEFAULT);
        Canvas result = new Canvas(width, height);

        // for (int row = 0; row < height; row++) {
        // for (int col = 0; col < width; col++) {
        // int pixel = image.getRGB(col, row);
        // result.set(row, col, RGBtoGray(pixel));
        // }
        // }
        return result;
    }

    public Canvas center() {
        int rowSum = 0;
        int colSum = 0;
        int sum = 0;
        for (int row = 0; row < this.rows; row++) {
            for (int col = 0; col < this.columns; col++) {
                int pixel = get(row, col);
                rowSum += (row + 1) * pixel;
                colSum += (col + 1) * pixel;
                sum += pixel;
            }
        }

        int centerRow = rowSum / sum;
        int centerCol = colSum / sum;
        int rowShift = rows/2 - centerRow;
        int colShift = columns/2 - centerCol;

        // System.out.println("CenterRow = " + centerRow);
        // System.out.println("CenterCol = " + centerCol);
        // System.out.println("RowShift = " + rowShift);
        // System.out.println("ColShift = " + colShift);

        Canvas result = new Canvas(rows, columns);
        for (int row = 0; row < this.rows; row++) {
            for (int col = 0; col < this.columns; col++) {
                set(row, col, get(row + rowShift, col + colShift));
            }
        }
        return result;
    }

    public String image(int threshold) {
        // Produces an image of the canvas that can be printed on System.out.
        String result = "";
        for (int row = 0; row < this.rows; row++) {
            for (int column = 0; column < this.columns; column++) {
                result += get(row, column) > threshold ? "*" : " ";
            }
            result += "\n";
        }
        return result;
    }

    public String image() {
        return this.image(THRESHOLD);
    }

    @Override
    public String toString() {
        String result = "";
        for (int row = 0; row < this.rows; row++) {
            for (int column = 0; column < this.columns; column++) {
                result += String.format("%3d ", get(row, column));
            }
            result += "\n";
        }
        return result;
    }
}
