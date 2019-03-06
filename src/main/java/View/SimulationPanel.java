package View;

import Model.Elements.Element;
import Model.Elements.StaticFire;
import Model.Simulation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class SimulationPanel extends JPanel implements Observer, MouseListener {
    private Simulation model;
    private Graphics g;
    private List<List<Element>> cells; //Holds the cells
    private boolean draw_all; // intended to be able to draw only changed cells, but not implemented yet. Keep at 1
    private ElementFrame elementFrame;

    public SimulationPanel(Simulation model, int size){
        draw_all = true;
        this.model = model;
        cells = model.getAllCells();
        this.addMouseListener(this);

        this.setPreferredSize(new Dimension(size,size));
        model.addObserver(this);
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        if(elementFrame != null){
            Element e = elementFrame.getElement();
            elementFrame.dispose();
            elementFrame = new ElementFrame(e);
        }
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
        //Draw from top to bottom since GUI defaults origin in upperLeft, but we want lowerLeft
        float y = end_y - y_jump;

        if(draw_all) {  //Else not implemented yet
            for (int i = 0; i < cells.get(0).size(); i++) {
                for (int j = 0; j < cells.size(); j++) {
                    g.setColor(cells.get(j).get(i).getColor());
                    g.fillRect((int) x, (int) y, (int) x_width, (int) y_width);
                    x += x_jump;



                }
                y -= y_jump;
                x = start_x;
            }
            for (int i=0; i<model.getAgents().size(); i++) {
                g.setColor(model.getAgents().get(i).getColor());
                g.fillRect((int) (model.getAgents().get(i).getX()*x_jump), (int) ((end_y-y_jump) - (int) model.getAgents().get(i).getY()*y_jump ) , (int) x_width, (int) y_width);
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

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

        int x = mouseEvent.getX();
        int y = mouseEvent.getY();
        //recreate jump-in boundaries
        x = x -2;
        y = y -2;

        int start_x = 2;
        int start_y = 2;
        int end_x = this.getSize().width - 2;
        int end_y = this.getSize().height - 2;
        float x_jump = (float) (end_x - start_x)/ (float) cells.size();
        float y_jump = (float) (end_y - start_y)/ (float) cells.get(0).size();
        float x_coord = x / x_jump;
        float y_coord = cells.get(0).size() -  y / y_jump;

        Element e = cells.get((int) x_coord).get((int) y_coord);

//        if(mouseEvent.getButton() == MouseEvent.BUTTON1) {
//            for (int i = 0; i < model.getNr_agents(); i++) {
//                if (x >= model.getAgents().get(i).getX() * x_jump
//                        && y >= ((end_y - y_jump) - (int) model.getAgents().get(i).getY() * y_jump)
//                        && x <= model.getAgents().get(i).getX() * x_jump + x_jump - 1
//                        && y <= ((end_y - y_jump) - (int) model.getAgents().get(i).getY() * y_jump + y_jump - 1)) {
//                    e = model.getAgents().get(i);
//                }
//            }
//        }

        if(elementFrame != null){
            elementFrame.setVisible(false);
            elementFrame.dispose();
        }
//        elementFrame = new ElementFrame(e);
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }

}
