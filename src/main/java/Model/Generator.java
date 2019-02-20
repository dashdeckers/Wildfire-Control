package Model;

import Model.Elements.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Generator implements Serializable {
    private List<List<Element>> cells;  //This will hold a 2D array of all cells in the simulation
    private List<Agent> agents;
    private ParameterManager parameter_manager;
    private Simulation model;
    private int width, height, nr_agents;
    Random rand;

    public Generator(Simulation model) {
        rand = model.getRand();
        parameter_manager = model.getParameter_manager();
        cells = model.getAllCells();
        agents = model.getAgents();
        width = parameter_manager.getWidth();
        height = parameter_manager.getHeight();
        nr_agents = model.getNr_agents();
        this.model = model;
    }

    /**
     * Creates a grid of all tree cells with one random burning cell.
     * This is just for ensuring fire is spreading as it should and that the visualization is working.
     *
     * @param x
     * @param y
     */
    public void tree_grid(int x, int y) {
        int fire_x = rand.nextInt(x);
        int fire_y = rand.nextInt(y);
        agents = new ArrayList<>();
        cells = new ArrayList<>();
        model.setCells(cells);
        model.setAgents(agents);
        for (int i = 0; i < x; i++) {
            List<Element> col = new ArrayList<>();
            for (int j = 0; j < y; j++) {
                //Set a random tile on fire
                if (i == fire_x && j == fire_y) {
                    System.out.println("Fire at x= " + i + " y = " + j);
                    Element t = new Tree(i, j, parameter_manager);
                    t.setBurning();
                    col.add(t);
                } else {
                    if (i == 9 || i == 10) {
                        col.add(new Water(i, j, parameter_manager));
                    } else if (i == 12) {
                        col.add(new House(i, j, parameter_manager));
                    } else if (i == 14) {
                        col.add(new Road(i, j, parameter_manager));
                    } else if (j < 0.2 * height) {
                        col.add(new Tree(i, j, parameter_manager));
                    } else {
                        col.add(new Grass(i, j, parameter_manager));
                    }
                }
            }
            cells.add(col);
        }
        // Instead of adding individual agents, all agents will be stored in a final ArrayList added to the tree-grid. This way the amount of agents can be modified easily.
        //This will create one agents which can will be dropped on a random location on the map.
        for (int i = 0; i < nr_agents; i++) {
            Agent agent = new Agent(model, parameter_manager);
            agents.add(agent);
        }
    }

    /**
     * Creates a randomly generated maps
     */
    public void regenerate() {
        int area = width * height;

        /**
         * make two overarching variables:
         * 1) Rural : if high then amount of trees higher and amount of houses & roads lower
         * 2) Wetlands: If high then more rivers & lakes, if low then less rivers
         */
        int numberBushes = rand.nextInt((int) (0.1 * area));
        int numberHouses = rand.nextInt((int) (0.05 * area));
        cells = new ArrayList<List<Element>>();


        //
        // GRASS
        //
        // First fill with grass
        for (int i = 0; i < width; i++) {
            List<Element> row = new ArrayList<Element>();

            for (int j = 0; j < height; j++) {
                row.add(new Grass(i, j, parameter_manager));
            }
            cells.add(row);
        }


        //
        // TREES
        //
        // Add Trees at random points
        for (int i = 0; i < width; i++) {
            List<Element> row = cells.get(i);

            for (int j = 0; j < height; j++) {

                // chance = numberBushes/area that a tree is placed
                if (rand.nextInt(area) < numberBushes) {

                    row.set(j, new Tree(i, j, parameter_manager));
                    //
                    // implement helper function to randomly place trees around
                    //
                    placeBlob(i, j, "Tree"); // implement to define Element
                }
            }
        }


        //
        // HOUSES
        //
        // Add HOUSES at random points
        for (int i = 0; i < width; i++) {
            List<Element> row = cells.get(i);

            for (int j = 0; j < height; j++) {

                // chance = numberBushes/area that a tree is placed
                if (rand.nextInt(area) < numberHouses) {

                    row.set(j, new House(i, j, parameter_manager));
                    // Small chance (2/width) that a lake is placed
                    if (rand.nextInt(width) < 1) {
                        placeBlob(i, j, "House");
                    }
                    //TODO: realistic villages
                }
            }
        }


        //
        // RIVER
        //
        // Add a meandering river, either starting at the left or at the top
        int chooseXY = rand.nextInt(2);

        // make vertical river ( I believe this starts at the top)
        if (chooseXY == 0) {

            // Ensure the south direction is implemented first
            int riverY = 0;
            int riverX = rand.nextInt(width);

            // Then let the river meander with a tendency to go south
            while (riverX >= 0 && riverX < width && riverY < height) {

                List<Element> row = cells.get(riverX);
                row.set(riverY, new Water(riverX, riverY, parameter_manager));
                // Small chance (2/width) that a lake is placed
                if (rand.nextInt(width) < 2) {
                    placeBlob(riverX, riverY, "Water");
                }


                int directionRiver = rand.nextInt(4);

                if (directionRiver == 0) { // West
                    riverX--;
                }
                if (directionRiver == 1 || directionRiver == 2) {//|| directionRiver == 3 || directionRiver == 4) { // South, tendency to go south
                    riverY++;
                }
                if (directionRiver == 3) { // East
                    riverX++;

                }

            }
            // TODO: all river tiles in row road

        } else {

            // make horizontal river (Starts at the left)
            // Ensure the East direction is implemented first
            int riverY = rand.nextInt(height);
            int riverX = 0;

            // Then let the river meander with a tendency to go East
            while (riverX >= 0 && riverX < width && riverY > 0 && riverY < height) {

                List<Element> row = cells.get(riverX);
                row.set(riverY, new Water(riverX, riverY, parameter_manager));
                // Small chance (2/height) that a lake is placed
                if (rand.nextInt(height) < 2) {
                    placeBlob(riverX, riverY, "Water");
                }

                int directionRiver = rand.nextInt(4);

                if (directionRiver == 0) { // North
                    riverY++;
                }
                if (directionRiver == 1 || directionRiver == 2) {// || directionRiver == 3 || directionRiver == 4) { // East, tendency to go East
                    riverX++;
                }
                if (directionRiver == 3) { // South
                    riverY--;

                }

            }
            // TODO: all river tiles in column road
        }

        //
        // ROAD
        //
        // Add either a vertical or a horizontal road
        chooseXY = rand.nextInt(2);


        // make vertical road (Starts at the top)
        if (chooseXY == 0) {
            int randomX = rand.nextInt(width);

            List<Element> row = cells.get(randomX);
            for (int i = 0; i < height; i++) {
                row.set(i, new Road(randomX, i, parameter_manager));
            }


            // make horizontal road (Starts at the left)
        } else {

            int randomY = rand.nextInt(height);

            for (int i = 0; i < width; i++) {
                List<Element> row = cells.get(i);
                row.set(randomY, new Road(i, randomY, parameter_manager));
            }

        }

        //
        // FIRE
        //
        int rand_x = rand.nextInt(width);
        int rand_y = rand.nextInt(height);
        boolean fireStarted = false;
        while (!fireStarted) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (x == rand_x && y == rand_y) {
                        Element cell = cells.get(x).get(y);
                        if (cell.isBurnable() && !cell.getType().equals("Agent")) {
                            cell.setBurning();
                            fireStarted = true;
                        }
                    }
                }
            }
        }

        //
        // AGENTS
        //

        for (int i = 0; i < nr_agents; i++) {
            Agent agent = new Agent(model, parameter_manager);
            agents.add(agent);
        }

        model.setCells(cells);
        model.setAgents(agents);
    }

    /**
     * Places tiles in a random radius around a XY-coordinate
     */
    void placeBlob(int originX, int originY, String element) {

        int radius = rand.nextInt(5);

        for (int x = 0; x <= radius; x++) {
            for (int y = 0; y + x <= radius; y++) {

                System.out.print("in placeBlob\n");

                if (x == 0 && y == 0) {
                    continue;
                }
                if (inBounds(originX + x, originY + y)) {
                    placeElementBlob((originX + x), (originY + y), element);
                }
                if (inBounds(originX + x, originY - y)) {
                    placeElementBlob((originX + x), (originY - y), element);
                }
                if (inBounds(originX - x, originY + y)) {
                    placeElementBlob((originX - x), (originY + y), element);
                }
                if (inBounds(originX - x, originY - y)) {
                    placeElementBlob((originX - x), (originY - y), element);

                }
            }
        }
    }

    // Element cell = cells.get(originX - x).get(originY - y);
    //if (cell.isBurnable) {

    void placeElementBlob(int x, int y, String element) {
        // Determine element
        List<Element> row = cells.get(x);
        switch (element) {
            case "Tree":
                row.set(y, new Tree(x, y, parameter_manager));
                break;
            case "Water":
                row.set(y, new Water(x, y, parameter_manager));
                break;
            case "Grass":
                row.set(y, new Grass(x, y, parameter_manager));
                break;
            case "House":
                row.set(y, new House(x, y, parameter_manager));
                break;
            case "Road":
                row.set(y, new Road(x, y, parameter_manager));
                break;
            case "Dirt":
                row.set(y, new Dirt(x, y, parameter_manager));
                break;
        }
    }


    /**
     * Checks if the coordinates are within the boundaries of the map.
     */
    public boolean inBounds(int x, int y) {
        int maxX = width;
        int maxY = height;
        return x >= 0 && x < maxX
                && y >= 0 && y < maxY;
    }
}



