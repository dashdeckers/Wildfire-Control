package View;

import Controller.Buttons.ResetButton;
import Controller.Buttons.StartButton;
import Controller.Buttons.StopButton;
import Model.Simulation;

import javax.swing.*;
import java.awt.*;

public class ControlPanel extends JPanel {
    Simulation model;
    public ControlPanel(Simulation model){
        this.model = model;
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        this.setLayout(gbl);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.5;
        c.weighty = 0.5;

        c.gridx = 0;
        c.gridy = 0;
        //Order determines the order they are placed in!
        addControlButtons(c);
        addParameterFields(c);
    }

    public void addControlButtons(GridBagConstraints c){
        JButton startButton = new StartButton(model);
        this.add(startButton, c);
        c.gridy++;
        JButton stopButton = new StopButton(model);
        this.add(stopButton, c);
        c.gridy++;
        JButton resetButton = new ResetButton(model);
        this.add(resetButton, c);
        c.gridy++;

    }

    public void addParameterFields(GridBagConstraints c){

    }
}
