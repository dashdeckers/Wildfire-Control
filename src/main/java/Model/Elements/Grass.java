package Model.Elements;

import Model.ParameterManager;

import java.awt.Color;
import java.util.Map;

public class Grass extends Element
{
    private static final Color LIGHT_GREEN = new Color(0,153,0);
    public Grass(int x, int y, ParameterManager parameterManager)
    {
        this.x = x;
        this.y = y;
        this.parameterManager = parameterManager;
        initializeParameters();
        pullParameters();
    }

    public void initializeParameters()
    {
        this.type = "Grass";
        this.r = 1;
        this.isBurnable = true;
        this.color = LIGHT_GREEN;
        this.burnIntensity = 8;
        this.ignitionThreshold = 30;
        this.fuel = starting_fuel = 20;
        this.moveSpeed = 10;
        this.clearCost = 1;
    }
}