package Controller.TextFields;

import Model.ParameterManager;
import Model.Simulation;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class parameterAction implements DocumentListener {
    private ParameterManager parameterManager;
    private JFormattedTextField t;
    private String parameter;
    public parameterAction(ParameterManager parameterManager, JFormattedTextField t, String parameter){
        this.parameterManager = parameterManager;
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
            parameterManager.changeParameter("Model", parameter, Float.parseFloat(inputString));
        }
    }
}
