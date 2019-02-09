package Controller.Buttons;

import Model.Simulation;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ResetButton extends JButton {
    public ResetButton(Simulation model){
        super("Reset");
        this.addActionListener(new ResetAction(model));
    }
}

class ResetAction extends AbstractAction{
    Simulation model;
    public ResetAction(Simulation model){
        this.model = model;
    }


    public void actionPerformed(ActionEvent e) {
        model.reset();
    }

}
