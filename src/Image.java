public class Image {

    public static final int WHITE = 0;
    public static final int BLACK = 255;
    public static final int UNKNOWN = -1;
    private static final int THRESHOLD = 128;

    private static int count = 0;

    private int id;
    private int digit;
    private int rows;
    private int columns;
    private byte[][] pixels;

    public Image(int rows, int columns, int digit) {
        if (digit < UNKNOWN | digit > 9) {
            throw new IllegalArgumentException("Digit: " + digit);
        }
        if (rows <= 0) {
            throw new IllegalArgumentException("Rows: " + rows);
        }
        if (columns <= 0) {
            throw new IllegalArgumentException("Columns: " + columns);
        }

        this.id = Image.count++;
        this.digit = digit;
        this.rows = rows;
        this.columns = columns;
        this.pixels = new byte[rows][columns];
    }


    public Image(int rows, int columns) {
        this(rows, columns, UNKNOWN);
    }

    public Image(byte[][] pixels, int digit) {
        if (digit < UNKNOWN | digit > 9) {
            throw new IllegalArgumentException("Digit: " + digit);
        }

        this.id = Image.count++;
        this.digit = digit;
        this.rows = pixels.length;
        this.columns = pixels[0].length;
        this.pixels = pixels;
    }

    public Image(byte[][] pixels) {
        this(pixels, UNKNOWN);
    }

    public Image(Canvas canvas, int digit) {
        this(canvas.rows(), canvas.columns(), digit);
        for (int row = 0; row < canvas.rows(); row++) {
            for (int col = 0; col < canvas.columns(); col++) {
                this.set(row, col, canvas.get(row, col));
            }
        }
    }

    public int id() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int digit() {
        return this.digit;
    }

    public int rows() {
        return this.rows;
    }

    public int columns() {
        return this.columns;
    }

    public byte[][] pixels() {
        return this.pixels;
    }

    public int get(int row, int column) {
        int value = this.pixels[row][column];
        return (value < 0) ? value + 256 : value;
    }

    public void set(int row, int column, int value) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("Pixel: " + value);
        }
        this.pixels[row][column] = (byte) value;
    }

    public boolean isWhite(int row, int column, int threshold) {
        return get(row, column) < threshold;
    }

    public boolean isWhite(int row, int column) {
        return isWhite(row, column, THRESHOLD);
    }

    public boolean isBlack(int row, int column, int threshold) {
        return get(row, column) >= threshold;
    }

    public boolean isBlack(int row, int column) {
        return isBlack(row, column, THRESHOLD);
    }


    public void reduce(int threshold) {
        for (int row = 0; row < this.rows; row++) {
            for (int col = 0; col < this.columns; col++) {
                int pixel = this.pixels[row][col];
                pixel = (pixel / threshold) * threshold;
                this.pixels[row][col] = (byte) pixel;
            }
        }
    }


    public boolean equals(Image other) {
        if (this.rows != other.rows) return false;
        if (this.columns != other.columns) return false;
        if (this.digit != other.digit) return false;

        for (int row = 0; row < this.rows; row++) {
            for (int col = 0; col < this.columns; col++) {
                if (this.pixels[row][col] != other.pixels[row][col]) return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Image && this.equals((Image) other);
    }


    @Override
    public int hashCode() {
        int hash = this.rows * this.columns;
        for (int row = 0; row < this.rows; row++) {
            for (int col = 0; col < this.columns; col++) {
                hash = 256 * hash + this.pixels[row][col];
            }
        }
        return this.digit * hash;
    }


    @Override
    public String toString() {
        String result = "";
        String lineSeparator = "";
        String itemSeparator = "";
        for (int row = 0; row < this.rows; row++) {
            result += lineSeparator;
            itemSeparator = " ";
            for (int col = 0; col < this.columns; col++) {
                result += itemSeparator;
                result += String.format("%3s", get(row, col));
                itemSeparator = " ";
            }
            lineSeparator = "\n";
        }
        return result;
    }

    public String image(int threshold) {
        String result = "";
        String lineSeparator = "";
        String itemSeparator = "";
        for (int row = 0; row < this.rows; row++) {
            result += lineSeparator;
            itemSeparator = " ";
            for (int col = 0; col < this.columns; col++) {
                result += (get(row, col) >= threshold) ? '*' : ' ';
            }
            lineSeparator = "\n";
        }
        return result;
    }

    public String image() {
        return image(THRESHOLD);
    }

    public double dist() {
        return distance(2);
    }
    public double distance() {
        return distance(2);
    }
    public double distance(int L) {
        double sum = 0;
        double pow = 1;

        for (byte[] line : pixels) {
            for (byte pixel : line) {

                for (int k = 0; k < L; k++) {
                    pow *= pixel;
                }

                sum += pow;
                pow = 1;
            }
        }

        return Math.pow(sum, 1.0/L);
    }
    public double dist(Image other) {
        return distance(other, 2);
    }
    public double distance(Image other) {
        return distance(other, 2);
    }
    public double distance(Image other, int L) {
        double sum = 0;
        double pow = 1;

        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels[0].length; j++) {

                for (int k = 0; k < L; k++) {
                    pow *= this.pixels[i][j] - other.pixels[i][j];
                }

                sum += pow;
                pow = 1;
            }
        }

        return Math.pow(sum, 1.0/L);
    }
}
