package Learning;

import Model.Elements.Element;
import Model.Simulation;

public class Fitness {
	public double fourPathEncirclementChecker(Simulation model) {
		double count = 0;

		// for every activeCell (=burning)
		//   take four paths in each direction
		//   if there is a burning or dead cell, abort
		//   if not, count burnable tiles until we hit a non-burnable
		//   if depth allows, take the two extra paths as well

		return count;
	}
}
