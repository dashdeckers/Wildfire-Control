package Controller.Buttons;

import Model.Simulation;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class StartButton extends JButton {
    public StartButton(Simulation model){
        super("Start");
        this.addActionListener(new StartAction(model));
    }
}

class StartAction extends AbstractAction{
    Simulation model;
    public StartAction(Simulation model){
        this.model = model;
    }


    public void actionPerformed(ActionEvent e) {
        model.start();
    }

}
