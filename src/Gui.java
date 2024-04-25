import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Gui {

    private JFrame frame;
    private Canvas canvas;
    private JLabel label;

    public class NextButton extends JButton implements ActionListener {

        private final int direction;

        public NextButton(int direction) {
            super(direction > 0 ? "Next" : "Prev");
            this.addActionListener(this);
            this.direction = direction;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            Image next = direction > 0 ? Viewer.next() : Viewer.prev();
            if (next != null) Gui.this.draw(next);
        }
    }


    public Gui(Canvas canvas, int scaling) {
        this.canvas = canvas;
        this.frame = new JFrame("MNIST");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.label = new JLabel("Image #0: ?");
        this.label.setAlignmentX(0.5f);
        this.label.setAlignmentY(0.5f);
        Font font = label.getFont();
        this.label.setFont(new Font(font.getName(), Font.BOLD, 20));
        JPanel labelPanel = new JPanel();

        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
        labelPanel.add(this.label);

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(new NextButton(+1));
        buttons.add(new NextButton(-1));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new CanvasPanel(canvas, scaling));
        panel.add(label);
        panel.add(buttons);

        Container content = frame.getContentPane();
        content.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    public void draw(Image image) {
        for (int row = 0; row < image.rows(); row++) {
            for (int column = 0; column < image.columns(); column++) {
                this.canvas.set(row, column, image.get(row, column));
            }
        }
        this.label.setText("Image #" + image.id() + ": " + image.digit());
        this.redraw();
    }


    public void redraw() {
        frame.repaint();
    }
}
