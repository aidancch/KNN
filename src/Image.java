public class Image {

    public static final int WHITE = 0;
    public static final int BLACK = 255;
    public static final int UNKNOWN = -1;
    private static final int THRESHOLD = 128;

    private static int count = 0;

    private int id;
    private int digit;
    private int rows;
    private int columns; //test
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
    public void setDigit(int digit) { this.digit = digit; }
    public void classify(int classificiation) { this.setDigit(classificiation); }

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


    public void threshold(int threshold) {
        for (int row = 0; row < this.rows; row++) {
            for (int col = 0; col < this.columns; col++) {
                int pixel = this.pixels[row][col];
                pixel = (pixel / threshold) * threshold;
                this.pixels[row][col] = (byte) pixel;
            }
        }
    }

    public void downsize(int scale) {
        assert this.rows % scale == 0 && this.columns % scale == 0;

        byte[][] newPix = new byte[this.rows / scale][this.columns / scale];
        for (int i = 0; i < newPix.length; i++) {
            for (int j = 0; j < newPix[0].length; j++) {
                int sum = 0;
                for (int y = i * scale; y < (i + 1) * scale; y++) {
                    for (int x = j * scale; x < (j + 1) * scale; x++) {
                        sum += this.pixels[y][x];
                    }
                }
                newPix[i][j] = (byte)(sum / (scale * scale));
            }
        }

        this.pixels = newPix;
        this.columns = this.columns / scale;
        this.rows = this.rows / scale;
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
        return distance();
    }
    public double distance() {
        // just using L2 for now
        int sum = 0;
        for (byte[] row : pixels) {
            for (byte pixel : row) {
                // convert unsigned byte to int
                int value = pixel & 0xFF;
                sum += value * value;
            }
        }
        return Math.pow(sum, 0.5);
    }
    public double dist(Image other) {
        return distance(other);
    }
    public double distance(Image other) {
        double sum = 0;

        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels[0].length; j++) {
                // convert unsigned byte to int
                int pixA = this.pixels[i][j] & 0xFF, pixB = other.pixels[i][j] & 0xFF;
                int diff = pixA - pixB;
                sum += diff * diff;
            }
        }

        return Math.pow(sum, 0.5);
    }
}
