package Controller.Buttons;

import View.ViewUtil.ObservableString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;

public class RadioListener implements ActionListener {

    public ObservableString activeType;
    public RadioListener(ObservableString activeType){
        this.activeType = activeType;
    }
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        activeType.setString(actionEvent.getActionCommand());
    }
}
