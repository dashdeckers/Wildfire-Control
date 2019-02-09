package Model.Elements;

import java.awt.Color;

/*
	The abstract element class contains most of what is needed for each element,
	each subclass must implement initializeParameters() which defines the unique
	properties of each specific element.

	The update function
 */

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

	public abstract void initializeParameters();

	public void update()
	{
		if (isBurnt)
		{
			return;
		}
		if (isBurning)
		{
			burnTimer--;
			if (burnTimer <= 0)
			{
				isBurning = false;
				isBurnt = true;
			}
		}
		else
		{
			updateFireActivity();
			if (fireActivity > ignitionThreshold)
			{
				setBurning();
			}
		}
	}

	private void updateFireActivity()
	{
		// for each neighbouring and inbounds position
		// if burning
		// sum up burnIntensities
		// set fireActivity = sum
	}

	public void setBurning() { isBurning = true; }

	public boolean isBurning() { return isBurning; }

	public boolean isBurnt() { return isBurnt; }

	public boolean isBurnable() { return burnable; }

	public int getX() { return x; }

	public int getY() { return y; }

	public Color getColor() { if (isBurning) { return Color.RED; } else { return color; } }

	public int getBurnDuration() { return burnDuration; }

	public int getBurnIntensity() { return burnIntensity; }

	public int getIgnitionThreshold() { return ignitionThreshold; }
}
