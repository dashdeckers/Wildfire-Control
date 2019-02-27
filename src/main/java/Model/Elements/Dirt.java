package Model.Elements;

import Model.ParameterManager;

import java.awt.*;

public class Dirt extends Element {
    private static final Color LIGHT_BROWN = new Color(153,102,0);
    public Dirt(int x, int y, ParameterManager parameterManager)
    {
        this.x = x;
        this.y = y;
        this.parameterManager = parameterManager;
        initializeParameters();
        pullParameters();
    }

    public void initializeParameters()
    {
        this.type = "Dirt";
        this.color = LIGHT_BROWN;
        this.moveSpeed = 10;
    }
}
