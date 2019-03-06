package View;

import Model.Simulation;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    /**
     * The frame of our simulation including the simulation and controls
     * @param model
     */
    public JPanel simulationPanel;
    public MainFrame(Simulation model){
        //Use gridbaglayout for lot of control
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        this.setLayout(gbl);
        /*Simulation area set to a square, with 300 extra width for control panel*/
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int simulation_size = screenSize.height * 2/3;


        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;

        simulationPanel = new SimulationPanel(model, simulation_size);
        this.add(simulationPanel, c);

        c.gridx = 1;
        this.add(new ControlPanel(model), c);

        this.setResizable(false);   //Ensures simulation remains square
        this.setTitle("Wildfire simulation");

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);



        this.pack();
        this.setPreferredSize(new Dimension(simulation_size + 300,simulation_size));

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
