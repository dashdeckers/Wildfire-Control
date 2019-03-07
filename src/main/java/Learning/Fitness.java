package Learning;

import Model.Elements.Element;
import Model.Simulation;

import java.util.Arrays;
import java.util.HashSet;

public class Fitness {

	/**
	 * Ivo's algorithm for measuring encirclement. It spans out in 4 directions from each burning
	 * cell and counts the burnable (and not on fire or burnt out) cells until it reaches a
	 * non-burnable.
	 * @param model
	 * @return
	 */
	public int straightPathsEncirclementMeasure(Simulation model) {
		int count = 0;

		// TODO: get depth argument, use old dead ends as starting points for looking in (two) new directions again
		// im leaving it like this for now because that will introduce 2 more loops into this thing

		HashSet<String> directions = new HashSet<>(Arrays.asList("N", "S", "E", "W"));
		// for every burning cell
		for (Element cell : model.getActiveCells()) {
			int ox = cell.getX();
			int oy = cell.getY();
			int x = 0;
			int y = 0;

			int layer = 0;
			// loop until no more directions to explore
			while (true) {
				if (directions.isEmpty()) {
					break;
				}
				// increment layer and get coordinates for each direction
				layer ++;
				HashSet<String> deadEnds = new HashSet<>();
				for (String d : directions) {
					switch (d) {
						case "E":
							x = ox + layer;
							y = oy;
							break;
						case "W":
							x = ox - layer;
							y = oy;
							break;
						case "N":
							x = ox;
							y = oy + layer;
							break;
						case "S":
							x = ox;
							y = oy - layer;
							break;
					}
					// check if the element in that direction is burnable and not on fire
					if (model.isInBounds(x, y)) {
						Element e = model.getElementAt(x, y);
						if ( e.getFuel() > 0
								&& e.isBurnable()
								&& !e.isBurning()) {
							count ++;
						} else {
							deadEnds.add(d);
						}
					}else{
						deadEnds.add(d);
					}
				}
				// if we reach a dead end, we don't need to explore that direction further
				directions.removeAll(deadEnds);
			}
		}
		return count;
	}
}
