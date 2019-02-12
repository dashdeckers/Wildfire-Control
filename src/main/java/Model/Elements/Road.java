package Model.Elements;

import java.awt.Color;
import java.util.Map;

public class Road extends Element
{
    private static final Color DARK_GREY = new Color(51,51,51);
    public Road(int x, int y, Map<String, Float> parameters)
    {
        this.x = x;
        this.y = y;
        this.parameters = parameters;
        initializeParameters();
    }

    public void initializeParameters()
    {
        this.color = DARK_GREY;
    }
}