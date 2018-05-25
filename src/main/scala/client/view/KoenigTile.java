package client.view;

import javax.swing.*;
import java.awt.*;

public class KoenigTile extends JPanel {

    private Image img;

    public KoenigTile(int COLOR) {
        img = new ImageIcon(getClass().getResource("../images/könig" + COLOR+".png")).getImage();
        Dimension size = new Dimension(img.getWidth(this), img.getHeight(this));
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);
    }

    public void paint(Graphics g) {
        g.drawImage(img, 0, 0, this);
    }

}
