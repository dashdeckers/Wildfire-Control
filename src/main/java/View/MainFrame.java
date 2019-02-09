package View;

import Model.Simulation;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame(Simulation model){
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        this.setLayout(gbl);
        int simulation_size = 1000;
        this.setPreferredSize(new Dimension(simulation_size + 300,simulation_size));
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;


        this.add(new SimulationPanel(model, simulation_size), c);

        c.gridx = 1;
        this.add(new ControlPanel(model), c);

        this.setResizable(false);
        this.setTitle("Wildfire simulation");

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
