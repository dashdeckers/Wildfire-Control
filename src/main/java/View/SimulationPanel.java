package View;

import Model.Elements.Element;
import Model.Simulation;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SimulationPanel extends JPanel {
    public Simulation model;
    public SimulationPanel(Simulation model){
        this.model = model;
    }

    public void paintComponent(Graphics g){
        List<List<Element>> elements = model.getAllCells();

        //Jump-in boundaries
        int start_x = 10;
        int start_y = 10;
        int end_x = this.getSize().width - 10;
        int end_y = this.getSize().height - 10;

        int x_jump = (end_x - start_x)/elements.size();
        int y_jump = (end_y - start_y)/elements.get(0).size();
        int x_width = x_jump - 2;
        int y_width = y_jump - 2;

        int x = start_x;
        int y = start_y;

        for(int i = 0; i < elements.size(); i++){
            for(int j =0; j < elements.get(0).size(); j++){
                //System.out.println("Drawing rectangle at:" + elements.get(i).get(j).getColor().toString());
                g.setColor(elements.get(i).get(j).getColor());
                g.fillRect(x,y, x_width, y_width);
                x+= x_jump;
            }
            y+= y_jump;
            x = start_x;
        }

    }

}
