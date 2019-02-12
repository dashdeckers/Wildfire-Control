package Model.Elements;

import java.awt.Color;
import java.util.Map;

public class House extends Element
{
    private static final Color LIGHT_BROWN = new Color(153,102,0);
    public House(int x, int y, Map<String, Float> parameters)
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
        this.color = LIGHT_BROWN;
        this.burnIntensity = 5;
        this.ignitionThreshold = 1;
        this.fuel = 25;
        this.moveSpeed = 1;
    }
}