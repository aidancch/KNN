public class BoundingBox {

    private int left;
    private int right;
    private int bottom;
    private int top;

    public BoundingBox(Canvas canvas) {
        this.left = canvas.columns() - 1;
        this.right = 0;
        this.bottom = 0;
        this.top = canvas.rows() - 1;

        for (int row = 0; row < canvas.rows(); row++) {
            for (int col = 0; col < canvas.columns(); col++) {
                int pixel = canvas.get(row, col);
                if (pixel > 0) {
                    if (col < this.left) this.left = col;
                    if (col > this.right) this.right = col;
                    if (row < this.top) this.top = row;
                    if (row > this.bottom) this.bottom = row;
                }
            }
        }
    }

    public int left()   { return this.left; }
    public int right()  { return this.right; }
    public int bottom() { return this.bottom; }
    public int top()    { return this.top; }

    public int width()  { return this.right - this.left + 1; }
    public int height() { return this.bottom - this.top + 1; }

    @Override
    public String toString() {
        String result = "(";
        result += left;
        result += ",";
        result += top;
        result +=") to (";
        result += right;
        result += ",";
        result += bottom;
        result += ")";
        return result;
    }
}
//test fork
