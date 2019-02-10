package View;

import Model.Elements.Element;
import Model.Simulation;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class SimulationPanel extends JPanel implements Observer {
    private Simulation model;
    private Graphics g;
    private List<List<Element>> cells; //Holds the cells
    private boolean draw_all; // intended to be able to draw only changed cells, but not implemented yet. Keep at 1

    public SimulationPanel(Simulation model, int size){
        draw_all = true;
        this.model = model;
        cells = model.getAllCells();

        this.setPreferredSize(new Dimension(size,size));
        model.addObserver(this);
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        //Jump-in boundaries
        int start_x = 2;
        int start_y = 2;
        int end_x = this.getSize().width - 2;
        int end_y = this.getSize().height - 2;

        //Width of the cells+gap
        float x_jump = (float) (end_x - start_x)/ (float) cells.size();
        float y_jump = (float) (end_y - start_y)/ (float) cells.get(0).size();
        //Width of the cells - gap
        float x_width = x_jump - 1;
        float y_width = y_jump - 1;

        float x = start_x;
        float y = start_y;

        if(draw_all) {  //Else not implemented yet
            for (int i = 0; i < cells.get(0).size(); i++) {
                for (int j = 0; j < cells.size(); j++) {
                    g.setColor(cells.get(j).get(i).getColor());
                    g.fillRect((int) x, (int) y, (int) x_width, (int) y_width);
                    x += x_jump;
                }
                y += y_jump;
                x = start_x;
            }
        }

    }


    public void update(Observable observable, Object o) {
        //Find that the changed object is a List<List<Element>>
        if(o instanceof List   //Check if List
                && ((List<Object>) o).get(0) instanceof List //THEN check if List of Lists (i.e. 2d Array)
                && ((List<List<Object>>) o).get(0).get(0) instanceof Element ) { //THEN check if 2d array of Elements (i.e. cells)
            cells = (List<List<Element>>) o;
            repaint();
        }
    }
}
