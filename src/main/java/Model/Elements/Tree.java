package Model.Elements;

import java.awt.Color;

public class Tree extends Element
{
	public Tree(int x, int y)
	{
		this.x = x;
		this.y = y;
		initializeParameters();
	}

	public void initializeParameters()
	{
		this.burnable = true;
		this.color = Color.GREEN;
		this.burnDuration = 3;
		this.burnTimer = burnDuration;
		this.burnIntensity = 3;
		this.ignitionThreshold = 3;
		this.fireActivity = 0;
		this.isBurning = false;
		this.isBurnt = false;
	}

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

	public boolean isBurnable() { return burnable; }

	public int getX() { return x; }

	public int getY() { return y; }

	public Color getColor() { if(isBurning){ return Color.RED;}else{ return color; }}

	public int getBurnDuration() { return burnDuration; }

	public int getBurnIntensity() { return burnIntensity; }

	public int getIgnitionThreshold() { return ignitionThreshold; }
}
