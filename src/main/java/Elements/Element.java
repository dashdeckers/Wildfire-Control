package Elements;

import java.awt.Color;

public abstract class Element
{
	int x = 0;
	int y = 0;
	Color color = Color.WHITE;
	boolean burnable = false;

	int burnDuration = 0;
	int burnTimer = burnDuration;
	int burnIntensity = 0;
	int ignitionThreshold = 10;
	int fireActivity = 0;
	boolean isBurning = false;
	boolean isBurnt = false;

	public abstract void setBurning();
	public abstract boolean isBurnable();
	public abstract int getX();
	public abstract int getY();
	public abstract Color getColor();
	public abstract int getBurnDuration();
	public abstract int getBurnIntensity();
	public abstract int getIgnitionThreshold();

	public abstract void initializeParameters();
	public abstract void update();
}
