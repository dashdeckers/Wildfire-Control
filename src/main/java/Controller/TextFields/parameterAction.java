package Controller.TextFields;

import Model.Simulation;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class parameterAction implements DocumentListener {
    private Simulation model;
    private JFormattedTextField t;
    private String parameter;
    public parameterAction(Simulation model, JFormattedTextField t, String parameter){
        this.model = model;
        this.t = t;
        this.parameter = parameter;

    }

    public void insertUpdate(DocumentEvent documentEvent) {
        changed();
    }

    public void removeUpdate(DocumentEvent documentEvent) {
        changed();
    }

    public void changedUpdate(DocumentEvent documentEvent) {
        changed();

    }

    public void changed(){
        String inputString = this.t.getText();
        if (!inputString.equals("")){
            model.changeParameter(parameter, Float.parseFloat(inputString));
        }
    }
}
