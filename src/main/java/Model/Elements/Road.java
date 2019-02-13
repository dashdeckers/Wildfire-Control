package Model.Elements;

import Model.ParameterManager;

import java.awt.Color;
import java.util.Map;

public class Road extends Element
{
    private static final Color DARK_GREY = new Color(51,51,51);
    public Road(int x, int y, ParameterManager parameterManager)
    {
        this.x = x;
        this.y = y;
        this.parameterManager = parameterManager;
        initializeParameters();
        pullParameters();
    }

    public void initializeParameters()
    {
        this.type = "Road";
        this.color = DARK_GREY;
    }

    @Override
    public String getType() {
        return "Road";
    }
}