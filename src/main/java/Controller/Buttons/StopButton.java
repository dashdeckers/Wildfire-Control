package Controller.Buttons;

import Model.Simulation;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class StopButton extends JButton {
    public StopButton(Simulation model){
        super("Stop");
        this.addActionListener(new StopAction(model));
    }
}

class StopAction extends AbstractAction{
    Simulation model;
    public StopAction(Simulation model){
        this.model = model;
    }


    public void actionPerformed(ActionEvent e) {
        model.stop();
    }

}
