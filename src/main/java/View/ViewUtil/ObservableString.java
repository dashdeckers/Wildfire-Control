package View.ViewUtil;

import java.io.Serializable;
import java.util.Observable;

public class ObservableString extends Observable implements Serializable {
    private String string;
    public ObservableString(){}

    public void setString(String string){
        this.string = string;
        setChanged();
        notifyObservers(this);
    }

    public String getString(){
        return string;
    }

    @Override
    public String toString(){
        return string;
    }
}
