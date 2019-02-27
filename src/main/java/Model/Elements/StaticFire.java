package Model.Elements;

import Model.ParameterManager;

import java.awt.Color;
import java.util.Map;

public class StaticFire extends Element
{
    public StaticFire(int x, int y, ParameterManager parameterManager)
    {
        this.x = x;
        this.y = y;
        this.parameterManager = parameterManager;
        initializeParameters();
        pullParameters();
    }

    public void initializeParameters()
    {
        this.type = "StaticFire";
        this.r = 0;
        this.isBurnable = true;
        this.color = Color.CYAN;
        this.burnIntensity = 10;
        this.ignitionThreshold = 1;
        this.fuel = starting_fuel = 999999999;
        this.moveSpeed = 1;
        this.clearCost = 1;
        //This makes it burn from the start
        this.setTemperature(10.0);
        this.setBurning();
    }
}