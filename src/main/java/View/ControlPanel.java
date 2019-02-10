package View;

import Controller.Buttons.ControlButton;
import Controller.TextFields.parameterAction;
import Model.Simulation;

import javax.swing.*;
import java.awt.*;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

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
     * This creates parameter textfields according to the parameter map in Simulation. If you want to add parameters, do that there, and this should generate the new parameter
     * @param c
     */
    public void addParameterFields(GridBagConstraints c){

        Map<String, Float> parameter_list = model.getParameters();
        for (String p: parameter_list.keySet()) {


            JLabel l = new JLabel(p);
            this.add(l, c);
            c.gridx = 1;
            //Could make this nicer by putting the TextField in a panel
            JFormattedTextField t = new JFormattedTextField();
            t.setText(parameter_list.get(p).toString());
            t.setColumns(3);
            t.getDocument().addDocumentListener(new parameterAction(model, t, p));
            this.add(t, c);
            c.gridx = 0;
            c.gridy++;
        }
    }
}
