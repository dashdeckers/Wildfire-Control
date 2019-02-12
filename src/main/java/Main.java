import Model.Simulation;
import View.MainFrame;

public class Main
{
	public static void main(String[] args)
	{
		boolean use_gui;
		if(args.length > 0 && args[0].equals("no_gui")){
			final long startTime = System.currentTimeMillis();
			System.out.println("NO GUI!");
			use_gui = false;
			new Simulation(use_gui);
			final long endTime = System.currentTimeMillis();
			System.out.println("Total execution time: " + (endTime - startTime));
		}else {
			use_gui = true;
			Simulation model = new Simulation(use_gui);
			new MainFrame(model);
		}
	}
}
