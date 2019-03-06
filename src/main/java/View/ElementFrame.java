package View;

import Model.Elements.Element;

import javax.swing.*;
import java.awt.*;

public class ElementFrame extends JFrame {
    Element e;

    /**
     * Simple frame to float on the top-left to show some inforamtion about an element
     * @param e The element for which this frame is spawned.
     */
    public ElementFrame (Element e){
        this.e = e;

        JPanel elementPanel = new ElementPanel(e);

        this.add(elementPanel);

        this.setResizable(false);   //Ensures simulation remains square
        this.setTitle("Element display");

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);



        this.pack();

        this.setVisible(true);
    }


    public Element getElement() {
        return e;
    }
}

/**
 * Panel to fill the ElementFrame
 */
class ElementPanel extends JPanel{
    Element displayElement;
    int square_width, square_height;

    public ElementPanel(Element e){
        displayElement = e;
        square_width = square_height = 100;

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        this.setLayout(gbl);

        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 5;
        c.ipady = 5;
        c.anchor = GridBagConstraints.CENTER;

        JPanel squarePanel  = new SquarePanel();

        squarePanel.setBorder(BorderFactory.createLineBorder(Color.black));
        this.add(squarePanel, c);
        c.gridx = 1;


        JLabel coords = new JLabel("Coordinates = ("+ Integer.toString(e.getX()) + "," + Integer.toString(e.getY()) +")" );
        coords.setPreferredSize(new Dimension(150, square_height));
        coords.setBorder(BorderFactory.createLineBorder(Color.black));
        coords.setHorizontalAlignment(JLabel.CENTER);
        c.weightx = 0;
        this.add(coords, c);
        c.weightx  = 1;
        c.gridy = 1;
        c.gridx = 0;

        JLabel type = new JLabel( "Type = " + e.getType());
        c.weighty = 0;
        type.setPreferredSize(new Dimension(square_width, 70));
        type.setBorder(BorderFactory.createLineBorder(Color.black));
        type.setHorizontalAlignment(JLabel.CENTER);
        this.add(type, c);
        c.weighty = 1;

        c.gridx = 1;
        c.gridy = 1;
        String fireString;
        if(e.isBurning()){
            fireString = "On fire";
        }else{
            fireString = "Not on fire";
        }
        JLabel fire = new JLabel(fireString);
        fire.setHorizontalAlignment(JLabel.CENTER);
        fire.setBorder(BorderFactory.createLineBorder(Color.black));

        this.add(fire, c);

        c.gridx = 2;
        c.gridy = 1;
        JLabel temperature = new JLabel("Temperature: " + Double.toString(e.getTemperature()));

        temperature.setHorizontalAlignment(JLabel.CENTER);
        temperature.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        this.add(temperature,c);

        c.gridy = 0;
        c.gridx = 2;

        JLabel fuel = new JLabel("Fuel: " + Integer.toString(e.getFuel()));

        fuel.setHorizontalAlignment(JLabel.CENTER);
        fuel.setBorder(BorderFactory.createLineBorder(Color.BLACK));


        this.add(fuel, c);


    }

    /**
     * Just a panel to show the color of the element
     */
    private class SquarePanel extends JPanel {

        @Override
        public void paintComponent(Graphics g) {

            g.setColor(displayElement.getColor());
            g.fillRect(5, 5, square_width -5, square_height -5);
        }
    }


}
