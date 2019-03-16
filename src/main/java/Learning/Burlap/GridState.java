package Learning.Burlap;

import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.core.state.UnknownKeyException;
import burlap.mdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;

import static Learning.Burlap.GridWorld.VAR_X;
import static Learning.Burlap.GridWorld.VAR_Y;

/*
I just copied all of this from the website so we can start making the required functions.
I have no clue how to properly run everything! :DDDDDD
 */
@DeepCopyState
public class GridState implements MutableState{
    public int x;
    public int y;
    private final static List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y);

    public GridState() {
    }

    public GridState(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        if(variableKey.equals(VAR_X)){
            this.x = StateUtilities.stringOrNumber(value).intValue();
        }
        else if(variableKey.equals(VAR_Y)){
            this.y = StateUtilities.stringOrNumber(value).intValue();
        }
        else{
            throw new UnknownKeyException(variableKey);
        }
        return this;
    }

    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object variableKey) {
        if(variableKey.equals(VAR_X)){
            return x;
        }
        else if(variableKey.equals(VAR_Y)){
            return y;
        }
        throw new UnknownKeyException(variableKey);
    }

    @Override
    public GridState copy() {
        return new GridState(x, y);
    }

    @Override
    public String toString() {
        return StateUtilities.stateToString(this);
    }
}