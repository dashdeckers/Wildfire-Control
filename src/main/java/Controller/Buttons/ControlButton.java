package Controller.Buttons;
import Model.Simulation;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ControlButton extends JButton {
    public ControlButton(Simulation model, String button_name){
        super(button_name);
        this.addActionListener(new ControlAction(model, button_name));
    }
}

class ControlAction extends AbstractAction{
    private Simulation model;
    private String name;
    public ControlAction(Simulation model, String name){
        this.model = model;
        this.name = name;
    }


    /**
     * name should match the names of the buttons as defined in ControlPanel.addControlButtons()
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
        switch(name){
            case "Reset":
                model.reset();
                break;
            case "Stop":
                model.stop();
                break;
            case "Regenerate":
                model.regenerate();
                break;
            case "Start":
                //Use a thread here so that we still have control while the model is running
                new Thread(new Runnable() {
                    public void run() {
                        model.start();
                    }}).start();

                break;
            case "Step back":
                    model.stepBack();
                break;
            case "Step forward":
                    model.stepForward();
                break;
            default:
                System.out.println("NO ACTION MAPPED TO THIS BUTTON!");
        }
    }

}