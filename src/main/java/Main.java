import Model.Simulation;
import View.MainFrame;

import javax.swing.*;

public class Main
{
	public static void main(String[] args)
	{
        Simulation model = new Simulation(10);
	    JFrame mainFrame = new MainFrame(model);
	}
}
