package View;

import Controller.Buttons.ControlButton;
import Controller.TextFields.parameterAction;
import Model.ParameterManager;
import Model.Simulation;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ControlPanel extends JPanel {
    private Simulation model;
    private ParameterManager parameterManager;

    public ControlPanel(Simulation model){
        this.model = model;
        parameterManager = model.getParameter_manager();
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
        addTypeParameters(c);
    }

    /**
     * Creates the buttons for controlling. The first part creates full width buttons, the last part half-width buttons
     * All buttons call ControlAction in ControlButton.java. If you add a string to button_names here a button will be drawn.
     * If you add a case with matching string to the switch in ControlAction.actionPerformed() you can add functionality.
     * @param c
     */
    public void addControlButtons(GridBagConstraints c){
        String[] button_names = {"Start", "Stop", "Regenerate", "Reset"};
        c.gridwidth = 2;
        for(String name : button_names) {
            JButton controlButton = new ControlButton(model, name);
            this.add(controlButton, c);
            c.gridy++;
        }
        c.gridwidth = 1;
        JButton stepBack = new ControlButton(model, "Step back");
        this.add(stepBack, c);
        c.gridx = 1;
        JButton stepForward = new ControlButton(model, "Step forward");
        this.add(stepForward,c);
        c.gridx = 0;
        c.gridy++;


    }

    /**
     * This creates parameter text fields according to the parameter map in Simulation.
     * All parameters are fetched from the model. If you want to add parameters to that in Simulation.createParameters()
     * @param c
     */
    public void addParameterFields(GridBagConstraints c){

        Map<String, Float> parameter_list = model.getParameters();
        for (String p: parameter_list.keySet()) {
            //Create a text label
            JLabel l = new JLabel(p);
            this.add(l, c);
            c.gridx = 1;
            //Could make this nicer by putting the TextField in a panel
            JFormattedTextField t = new JFormattedTextField();
            t.setText(parameter_list.get(p).toString()); //import the default value to the text field
            t.getDocument().addDocumentListener(new parameterAction(parameterManager, t, p)); //add functionality
            this.add(t, c);
            c.gridx = 0;
            c.gridy++;
        }
    }

    public void addTypeParameters(GridBagConstraints c){



    }
}
