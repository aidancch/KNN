import java.awt.geom.AffineTransform;

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

    public Image(int[][] pixels) {
        byte[][] bPix = new byte[pixels.length][pixels[0].length];

        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels[i].length; j++) {
                bPix[i][j] = (byte)pixels[i][j];
            }
        }

        this.id = Image.count++;
        this.digit = UNKNOWN;
        this.rows = bPix.length;
        this.columns = bPix[0].length;
        this.pixels = bPix;
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

    // Image alterations

    public void threshold(int threshold) {
        for (int row = 0; row < this.rows; row++) {
            for (int col = 0; col < this.columns; col++) {
                int pixel = Byte.toUnsignedInt(this.pixels[row][col]);
                this.pixels[row][col] = (pixel < threshold) ? (byte) (0) : (byte) (255);
            }
        }
    }

    public static void threshold(Image[] images, int threshold) {
        for (Image image : images) {
            image.threshold(threshold);
        }
    }

    public void downsize(int scale) {
        assert this.rows % scale == 0 && this.columns % scale == 0;
        byte[][] newPic = new byte[this.rows / scale][this.columns / scale];
        for (int i = 0; i < newPic.length; i++) {
            for (int j = 0; j < newPic[0].length; j++) {
                int sum = 0;
                for (int y = i * scale; y < (i + 1) * scale; y++) {
                    for (int x = j * scale; x < (j + 1) * scale; x++) {
                        sum += Byte.toUnsignedInt(this.pixels[y][x]);
                    }
                }
                newPic[i][j] = (byte)(sum / (scale * scale));
            }
        }

        this.pixels = newPic;
        this.columns = this.columns / scale;
        this.rows = this.rows / scale;
    }

    public static void downsize(Image[] images, int scale) {
        for (Image image : images) {
            image.downsize(scale);
        }
    }

    public void despeckle(int limit) {
        boolean[][] visited = new boolean[this.rows][this.columns];
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                if (Byte.toUnsignedInt(pixels[i][j]) != 0 && !visited[i][j]) {
                    int size = floodFill(i, j, visited);
                    if (size < limit) {
                        fillWithWhite(i, j, visited);
                    }
                }
            }
        }
    }

    private int floodFill(int row, int col, boolean[][] visited) {
        if (row < 0 || row >= this.rows || col < 0 || col >= this.columns || Byte.toUnsignedInt(pixels[row][col]) == 0 || visited[row][col]) {
            return 0;
        }
        visited[row][col] = true;
        int size = 1;
        size += floodFill(row + 1, col, visited);
        size += floodFill(row - 1, col, visited);
        size += floodFill(row, col + 1, visited);
        size += floodFill(row, col - 1, visited);

        return size;
    }

    private void fillWithWhite(int row, int col, boolean[][] visited) {
        if (row < 0 || row >= this.rows || col < 0 || col >= this.columns || Byte.toUnsignedInt(pixels[row][col]) == 0) {
            return;
        }
        visited[row][col] = true;
        this.pixels[row][col] = (byte) 0;

        fillWithWhite(row + 1, col, visited);
        fillWithWhite(row - 1, col, visited);
        fillWithWhite(row, col + 1, visited);
        fillWithWhite(row, col - 1, visited);
    }


    public static void despeckle(Image[] images, int limit) {
        for (Image image : images) {
            image.despeckle(limit);
        }
    }

    public void deskew() { // kinda shit
        int left_row = 0;
        int left_col = 0;
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                if (Byte.toUnsignedInt(pixels[j][i]) != 0) {
                    left_row = j;
                    left_col = i;
                    break;
                }
            }
            if (left_row != 0 && left_col != 0) {
                break;
            }
        }
        int right_row = 0;
        int right_col = 0;
        for (int i = this.rows - 1; i >= 0; i--) {
            for (int j = this.columns - 1; j >= 0; j--) {
                if (Byte.toUnsignedInt(pixels[j][i]) != 0) {
                    right_row = j;
                    right_col = i;
                    break;
                }
            }
            if (right_row != 0 && right_col != 0) {
                break;
            }
        }
        int top_row = 0;
        int top_col = 0;
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                if (Byte.toUnsignedInt(pixels[i][j]) != 0) {
                    top_row = i;
                    top_col = j;
                    break;
                }
            }
            if (top_row != 0 && top_col != 0) {
                break;
            }
        }
        int bot_row = 0;
        int bot_col = 0;
        for (int i = this.rows - 1; i >= 0; i--) {
            for (int j = this.columns - 1; j >= 0; j--) {
                if (Byte.toUnsignedInt(pixels[i][j]) != 0) {
                    bot_row = i;
                    bot_col = j;
                    break;
                }
            }
            if (bot_row != 0 && bot_col != 0) {
                break;
            }
        }

//        System.out.println("left (row, col): " + left_row + " " + left_col);
//        System.out.println("right (row, col): " + right_row + " " + right_col);
//        System.out.println("top (row, col): " + top_row + " " + top_col);
//        System.out.println("bot (row, col): " + bot_row + " " + bot_col);

        int pointA_row = 0;
        int pointA_col = 0;
        if (Math.sqrt(Math.pow(left_row - top_row, 2) + Math.pow(left_col - top_col, 2)) < Math.sqrt(Math.pow(left_row - bot_row, 2) + Math.pow(left_col - bot_col, 2))) {
            pointA_row = Math.abs(left_row + top_row) / 2;
            pointA_col = Math.abs(left_col + top_col) / 2;
        }
        else {
            pointA_row = Math.abs(left_row + bot_row) / 2;
            pointA_col = Math.abs(left_col + bot_col) / 2;
        }
        int pointB_row = 0;
        int pointB_col = 0;
        if (Math.sqrt(Math.pow(right_row - top_row, 2) + Math.pow(right_col - top_col, 2)) < Math.sqrt(Math.pow(right_row - bot_row, 2) + Math.pow(right_col - bot_col, 2))) {
            pointB_row = Math.abs(right_row + top_row) / 2;
            pointB_col = Math.abs(right_col + top_col) / 2;
        }
        else {
            pointB_row = Math.abs(right_row + bot_row) / 2;
            pointB_col = Math.abs(right_col + bot_col) / 2;
        }
        pixels[pointA_row][pointA_col] = (byte) 50;
        pixels[pointB_row][pointB_col] = (byte) 50;

        double angle = Math.toDegrees(Math.atan2(pointA_row - pointB_row, pointA_col - pointB_col));

        if (angle > 0 && angle < 45) {
            //anticlockwise by angle
            angle = -angle;
            pixels = rotateMatrix(pixels, "counterclockwise", Math.abs(angle));
        }
        else if (angle > 45 && angle < 90) {
            //clockwise by 90 - angle
            angle = 90 - angle;
            pixels = rotateMatrix(pixels, "clockwise", Math.abs(angle));
        }
        else if (angle > 90 && angle < 135) {
            //anticlockwise by angle - 90
            angle = -(angle - 90);
            pixels = rotateMatrix(pixels, "counterclockwise", Math.abs(angle));
        }
        else if (angle > 135) {
            //clockwise by 180 - angle
            angle = 180 - angle;
            pixels = rotateMatrix(pixels, "clockwise", Math.abs(angle));
        }
        else if (angle < 0 && angle > -45) {
            //clockwise by abs(angle)
            angle = Math.abs(angle);
            pixels = rotateMatrix(pixels, "clockwise", Math.abs(angle));
        }
        else if (angle < -45 && angle > -90) {
            //anticlockwise by 90 - abs(angle)
            angle = -(90 - Math.abs(angle));
            pixels = rotateMatrix(pixels, "counterclockwise", Math.abs(angle));
        }
        else if (angle < -90 && angle > -135) {
            //clockwise abs(angle) - 90
            angle = Math.abs(angle) - 90;
            pixels = rotateMatrix(pixels, "clockwise", Math.abs(angle));
        }
        else if (angle < -135) {
            //anticlockwise 180 - abs(angle)
            angle = -(180 - Math.abs(angle));
            pixels = rotateMatrix(pixels, "counterclockwise", Math.abs(angle));
        }
        else {

        }
    }

    public static byte[][] rotateMatrix(byte[][] pixels, String direction, double angleDegrees) {
        int centerX = pixels.length / 2;
        int centerY = pixels[0].length / 2;

        AffineTransform transform = new AffineTransform();
        transform.translate(centerY, centerX);

        if (direction.equals("counterclockwise")) {
            transform.rotate(-Math.toRadians(angleDegrees));
        } else if (direction.equals("clockwise")) {
            transform.rotate(Math.toRadians(angleDegrees));
        }

        transform.translate(-centerY, -centerX);
        byte[][] rotatedPixels = new byte[pixels.length][pixels[0].length];
        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels[0].length; j++) {
                double[] dest = new double[2];
                transform.transform(new double[]{j, i}, 0, dest, 0, 1);
                int newX = (int) Math.round(dest[0]);
                int newY = (int) Math.round(dest[1]);
                if (newX >= 0 && newX < pixels[0].length && newY >= 0 && newY < pixels.length) {
                    rotatedPixels[newY][newX] = pixels[i][j];
                }
            }
        }
        return rotatedPixels;
    }

    public static void deskew(Image[] images) { // kinda shit
        for (Image image : images) {
            image.despeckle(8);
            image.threshold(128);
            image.deskew();
        }
    }

    // only threshold, downsize, and despeckle are good
    // deskew works but its kinda shit

    public static void customizeImage(Image[] images) {
        for (Image image : images) {
            image.despeckle(8);
            image.threshold(128);
            image.downsize(2);
//            USE THIS AT YOUR DISCRETION
//            image.deskew();
        }
    }
}

