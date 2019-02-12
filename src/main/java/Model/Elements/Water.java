package Model.Elements;

import java.awt.Color;
import java.util.Map;

public class Water extends Element
{
    private static final Color DARK_BLUE = new Color(0,0,204);
    public Water(int x, int y, Map<String, Float> parameters)
    {
        this.x = x;
        this.y = y;
        this.parameters = parameters;
        initializeParameters();
    }

    public void initializeParameters()
    {
        this.color = DARK_BLUE;
    }
}