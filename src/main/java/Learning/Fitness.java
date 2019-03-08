package Learning;

import Model.Elements.Element;
import Model.Simulation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

public class Fitness implements Serializable {
	straightPathsEncirclementMeasure SPE;

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
		LinkedList<Element> startingPoints = new LinkedList<>(model.getActiveCells());

		// for every starting point (at first: every burning cell)
		while (true) {
			if (startingPoints.isEmpty()) {
				break;
			}
			Element cell = startingPoints.pop();
			int ox = cell.getX();
			int oy = cell.getY();
			int x = -1;
			int y = -1;

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
					int savedX = x;
					int savedY = y;
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
							if (savedX != -1 && savedY != -1) {
								startingPoints.add(model.getElementAt(savedX, savedY));
							}
						}
					} else {
						deadEnds.add(d);
					}
				}
				// if we reach a dead end, we don't need to explore that direction further
				directions.removeAll(deadEnds);
			}
		}
		return count;
	}

	public void createSPE(Simulation model) {
		this.SPE = new straightPathsEncirclementMeasure(model);
	}

	class straightPathsEncirclementMeasure {
		Simulation model;
		HashSet<String> directions = new HashSet<>(Arrays.asList("N", "S", "E", "W"));
		HashSet<startingPoint> newPoints = new HashSet<>();
		int ox;
		int oy;

		public straightPathsEncirclementMeasure(Simulation model) {
			this.model = model;
		}

		public int getFitness(int depth) {
			int count = 0;

			for (startingPoint p : newPoints)
			// the first loop goes over activeCells (=burning cells)
			for (Element e : model.getActiveCells()) {
				ox = e.getX();
				oy = e.getY();

				// for each direction
				for (String d : directions) {
					int layer = 0;

					// go in that direction until dead end
					while (true) {
						layer++;

						// if the element passes the check, increase count
						if (d.equals("E")) {
							if (checkElement(ox + layer, oy)) {
								count++;
							// otherwise add previous element to new starting points for the next loop
							} else if (layer > 1) {
								newPoints.add(new startingPoint(ox + (layer - 1), oy, "E"));
								break;
							}
						}
						if (d.equals("W")) {
							if (checkElement(ox - layer, oy)) {
								count++;
							} else if (layer > 1) {
								newPoints.add(new startingPoint(ox - (layer - 1), oy, "W"));
								break;
							}
						}
						if (d.equals("N")) {
							if (checkElement(ox, oy + layer)) {
								count++;
							} else if (layer > 1) {
								newPoints.add(new startingPoint(ox, oy + (layer - 1), "N"));
								break;
							}
						}
						if (d.equals("S")) {
							if (checkElement(ox, oy - layer)) {
								count++;
							} else if (layer > 1) {
								newPoints.add(new startingPoint(ox, oy - (layer - 1), "S"));
								break;
							}
						}
					}
				}
			}

			// now loop for depth iterations over newPoints

			return count;
		}

		private boolean checkElement(int x, int y) {
			if (model.isInBounds(x, y)) {
				Element e = model.getElementAt(x, y);
				return (e.getFuel() > 0
						&& e.isBurnable()
						&& !e.isBurning());
			}
			return false;
		}


		class startingPoint {
			Element element;
			String direction1;
			String direction2;

			startingPoint(int x, int y, String originDir) {
				this.element = model.getElementAt(x, y);
				if (originDir.equals("N") || originDir.equals("S")) {
					direction1 = "E";
					direction2 = "W";
				} else {
					direction1 = "N";
					direction2 = "S";
				}
			}
		}
	}
}
