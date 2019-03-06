
import Learning.HumanController;
import Model.Simulation;
import View.MainFrame;
import Learning.Cosyne;

public class Main {
	public static void main(String[] args) {
		boolean use_gui;
		if (args.length > 0 && args[0].equals("no_gui")) {
			final long startTime = System.currentTimeMillis();
			System.out.println("NO GUI!");
			use_gui = false;
			new Simulation(use_gui).start();
			final long endTime = System.currentTimeMillis();
			System.out.println("Total execution time: " + (endTime - startTime));
		} else if (args.length > 0 && args[0].equals("cosyne_gui")) {
			System.out.println("Cosyne gui");
			new Cosyne();

		}else if(args.length > 0 && args[0].equals("human")){
			HumanController hc = new HumanController();
			Simulation s = new Simulation(hc);
			MainFrame f = new MainFrame(s);
			f.simulationPanel.addKeyListener(hc);
			hc.simulationPanel = f.simulationPanel;

		}else {
				use_gui = true;
				Simulation model = new Simulation(use_gui);
				new MainFrame(model);

		}
	}
}
