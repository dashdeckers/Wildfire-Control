package Model.Elements;

import Model.ParameterManager;

import java.awt.Color;
import java.util.Map;

public class House extends Element
{
    private static final Color LIGHT_BROWN = new Color(153,102,0);
    public House(int x, int y, ParameterManager parameterManager)
    {
        this.x = x;
        this.y = y;
        this.parameterManager = parameterManager;
        initializeParameters();
        pullParameters();
    }

    public void initializeParameters()
    {
        this.type = "House";
        this.r = 1;
        this.burnable = true;
        this.color = LIGHT_BROWN;
        this.burnIntensity = 5;
        this.ignitionThreshold = 1;
        this.fuel = 25;
        this.moveSpeed = 1;
    }
}