package Controller.TextFields;

import Model.Elements.Element;
import Model.ParameterManager;
import View.ViewUtil.ObservableString;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;

import static java.lang.Float.parseFloat;

public class ElementTextFieldListener extends AbstractAction implements Observer {

    private JTextField textField;
    private ParameterManager parameterManager;
    private ObservableString activeElement;
    private String parameter;

    public ElementTextFieldListener(JTextField textField, ParameterManager parameterManager, ObservableString activeElement, String parameter){
        this.textField = textField;
        this.parameterManager = parameterManager;
        this.activeElement = activeElement;
        this.parameter = parameter;

        activeElement.addObserver(this);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        System.out.println("Changing " + activeElement.getString()+ "." + parameter + " to " + parseFloat(textField.getText()));
        parameterManager.changeParameter(activeElement.getString(), parameter, parseFloat(textField.getText()));
    }

    @Override
    public void update(Observable observable, Object o) {
        textField.setText(String.valueOf(parameterManager.getParameter(activeElement.getString(), parameter)));
    }
}
