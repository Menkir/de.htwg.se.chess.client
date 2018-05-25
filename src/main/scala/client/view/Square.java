package client.view;

import javax.swing.*;
import java.awt.*;

public class Square extends JPanel {

    //private Image img;

    /*public Square(String img) {
        this(new ImageIcon(img).getImage());
    }*/

    public Square(int r, int g, int b) {
        setPreferredSize(new Dimension(60,60));
        setBackground(new Color(r, g, b));
    }

}
