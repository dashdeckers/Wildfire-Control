package Model.Elements;

import java.awt.Color;
import java.util.Map;

public class Grass extends Element
{
    private static final Color LIGHT_GREEN = new Color(0,153,0);
    public Grass(int x, int y, Map<String, Float> parameters)
    {
        this.x = x;
        this.y = y;
        this.parameters = parameters;
        initializeParameters();
    }

    public void initializeParameters()
    {
        this.r = 1;
        this.burnable = true;
        this.color = LIGHT_GREEN;
        this.burnIntensity = 2;
        this.ignitionThreshold = 1;
        this.fuel = 5;
        this.moveSpeed = 10;
    }
}