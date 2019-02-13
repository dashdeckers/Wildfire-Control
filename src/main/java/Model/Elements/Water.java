package Model.Elements;

import Model.ParameterManager;

import java.awt.Color;
import java.util.Map;

public class Water extends Element
{
    private static final Color DARK_BLUE = new Color(0,0,125);
    public Water(int x, int y, ParameterManager parameterManager)
    {
        this.x = x;
        this.y = y;
        this.parameterManager = parameterManager;
        initializeParameters();
        pullParameters();
    }

    public void initializeParameters()
    {
        this.type = "Water";
        this.color = DARK_BLUE;
    }

}