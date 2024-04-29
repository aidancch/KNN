import javax.swing.*;
import java.awt.*;

public class CanvasPanel extends JPanel {

    private final Canvas canvas;
    private final int scaling;

    public CanvasPanel(Canvas canvas, int scaling) {
        super();
        this.canvas = canvas;
        this.scaling = scaling;
        int height = scaling * canvas.rows();
        int width = scaling * canvas.columns();
        setPreferredSize(new Dimension(width, height));
    }

    public Canvas getCanvas() {
        return this.canvas;
    }

    public Color gray(int intensity) {
        intensity = 255 - intensity;
        return new Color(intensity, intensity, intensity);
    }

    @Override
    public void paint(Graphics graphics) {
        int rows = this.canvas.rows();
        int columns = this.canvas.columns();
        int scale = this.scaling;

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Color color = gray(this.canvas.get(row, column));
                graphics.setColor(color);
                graphics.fillRect(column*scale, row*scale, scale, scale);
            }
        }
    }
}
