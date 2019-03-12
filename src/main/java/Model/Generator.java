package Model;

import Model.Elements.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Generator implements Serializable {
    private List<List<Element>> cells;
    private List<Agent> agents;
    private ParameterManager parameter_manager;
    private Simulation model;
    private int width, height, area, nr_agents;
    private Random rand;

    Generator(Simulation model) {
        this.model = model;
        refreshParameters();
    }

    /**
     * Probably not the prettiest way of doing this, but this makes sure Generator.java always
     * has access to the latest variables (e.g. when they are changed via UI)
     */
    private void refreshParameters() {
        rand = model.getRand();
        parameter_manager = model.getParameter_manager();
        cells = model.getAllCells();
        agents = model.getAgents();
        width = parameter_manager.getWidth();
        height = parameter_manager.getHeight();
        area = width * height;
        nr_agents = model.getNr_agents();
    }

    /**
     * Initializes a map of grass so the other generation methods have something to work on.
     */
    private void initializeMap() {
        cells = new ArrayList<>();
        agents = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            List<Element> col = new ArrayList<>();
            for (int j = 0; j < height; j++) {
                col.add(new Grass(i, j, parameter_manager));
            }
            cells.add(col);
        }
        model.setCells(cells);
        model.setAgents(agents);
    }

    /**
     * Generates a small map with a static fire block in the middle and one agent to the left of it.
     * No fire spread.
     */
    void plainMap() {
        refreshParameters();
        initializeMap();
        //Place StaticFire in the middle
        model.getAllCells().get(width/2).set(height/2, new StaticFire(width/2, height/2, model.getParameter_manager()));
        model.getAllCells().get(width/2 +1).set(height/2, new StaticFire(width/2 +1, height/2, model.getParameter_manager()));
        model.getAllCells().get(width/2 -1).set(height/2, new StaticFire(width/2 -1, height/2, model.getParameter_manager()));
        model.getAllCells().get(width/2).set(height/2 + 1, new StaticFire(width/2, height/2 + 1, model.getParameter_manager()));
        model.getAllCells().get(width/2).set(height/2 -1, new StaticFire(width/2, height/2 -1 , model.getParameter_manager()));

        //Place Agent to the left of the static fire
        agents.add(new Agent(width/4,height/2,model, parameter_manager,0));
        //Update model
        model.setNr_agents(1);
        model.setCells(cells);
        model.setAgents(agents);
        //model.printCells();
    }

    /**
     * This is the magical method that generates the oh-so-beautiful maps we don't need.
     */
    void randomMap() {
        refreshParameters();
        initializeMap();

        int wetlands = 2; // Variable (1-10) that influences dirt (dry) and rivers/lakes (wet)
        int urban = 1; // Variable (1-10) that influences bushes/grass (rural) and houses/roads (urban)

        // (added zero before everything to test parameters wetlands/urban)
        int numberDirt = rand.nextInt((int) (0.01 * area)) * (10-wetlands);
        int numberBushes = rand.nextInt((int) (0.01 * area)) * (10-urban);
        int numberHouses = rand.nextInt((int) (0.02 * area)) * urban;
        int numberLakes = rand.nextInt((int) (0.02 * area)) * (wetlands / 2);
        int numberBridges = rand.nextInt(3);
        int numberRivers = wetlands / 2;
        int numberRoads = urban / 2;
        /* OLD SETTINGS :
        int numberDirt = rand.nextInt((int) (0.01 * area));
        int numberBushes = rand.nextInt((int) (0.1 * area));
        int numberHouses = rand.nextInt((int) (0.05 * area));
        int numberRivers = 1;
        int numberBridges = rand.nextInt(3);
        int numberLakes = rand.nextInt((int) (0.002 * area));
        int numberRoads = 1; */

        // DIRT
        // Add dirt blobs at random points
        for (int i = 0; i < width; i++) {
            List<Element> row = cells.get(i);
            for (int j = 0; j < height; j++) {
                // chance = numberDirt/area that a tree is placed
                if (rand.nextInt(area) < numberDirt) {
                    row.set(j, new Dirt(i, j, parameter_manager));
                    placeBlob(i, j, "Dirt", 75);
                }
            }
        }

        // TREES
        // Add tree blobs at random points
        for (int i = 0; i < width; i++) {
            List<Element> row = cells.get(i);
            for (int j = 0; j < height; j++) {
                // chance = numberBushes/area that a tree is placed
                if (rand.nextInt(area) < numberBushes) {
                    row.set(j, new Tree(i, j, parameter_manager));
                    placeBlob(i, j, "Tree", 75);
                }
            }
        }

        // HOUSES
        // Add houses at random points
        for (int i = 0; i < width; i++) {
            List<Element> row = cells.get(i);
            for (int j = 0; j < height; j++) {
                // chance = numberBushes/area that a tree is placed
                if (rand.nextInt(area) < numberHouses) {
                    row.set(j, new House(i, j, parameter_manager));
                    // Small chance (2/width) that a lake is placed
                    //if (rand.nextInt(width) < 1) {
                        //placeSquare(i, j, "House");
                    //}
                    //TODO: realistic villages
                }
            }
        }

        // RIVER
        // Places a random amount (numberBridges) of bridges over River
        for (int river = 0; river < numberRivers; river++) // numberBridges determined at top
        {
            // Add a meandering river, either starting at the left or at the top
            int chooseXY = rand.nextInt(2);
            // VERTICAL river
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
                        placeBlob(riverX, riverY, "Water", 80);
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
                // Places a random amount (numberBridges) of bridges over River
                for (int i = 0; i < numberBridges; i++) // numberBridges determined at top
                {
                    // Determine random Y at which a bridge is placed over the river
                    int bridgeY = rand.nextInt(height);
                    for (int j = 0; j < width; j++) {
                        List<Element> row = cells.get(j);
                        Element cell = cells.get(j).get(bridgeY);
                        if (cell.getType() == "Water") {
                            row.set(bridgeY, new Road(j, bridgeY, parameter_manager));
                        }

                    }
                }

            } else {
                // HORIZONTAL river (Starts at the left)
                // Ensure the East direction is implemented first
                int riverY = rand.nextInt(height);
                int riverX = 0;

                // Then let the river meander with a tendency to go East
                while (riverX >= 0 && riverX < width && riverY > 0 && riverY < height) {
                    List<Element> row = cells.get(riverX);
                    row.set(riverY, new Water(riverX, riverY, parameter_manager));
                    // Small chance (2/height) that a lake is placed
                    if (rand.nextInt(height) < 2) {
                        placeBlob(riverX, riverY, "Water", 85);
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

                // Places a random amount (numberBridges) of bridges over River
                for (int i = 0; i < numberBridges; i++) // numberBridges determined at top
                {
                    // Determine random X at which a bridge is placed over the river
                    int bridgeX = rand.nextInt(width);
                    for (int j = 0; j < width; j++) {
                        List<Element> row = cells.get(bridgeX);
                        Element cell = cells.get(bridgeX).get(j);
                        if (cell.getType() == "Water") {
                            row.set(j, new Road(bridgeX, j, parameter_manager));
                        }
                    }
                }
            }
        }

        // Lakes
        // Add lakes at random points in the rivers
        for (int i = 0; i < width; i++) {
            List<Element> row = cells.get(i);
            for (int j = 0; j < height; j++) {
                // chance = numberDirt/area that a tree is placed
                if (rand.nextInt(area) < numberLakes) {
                    row.set(j, new Water(i, j, parameter_manager));
                    placeBlob(i, j, "Water", 90);
                }
            }
        }

        // ROAD
        // Places a random amount (numberBridges) of bridges over River
        for (int roads = 0; roads < numberRoads; roads++) // numberBridges determined at top
        {
            // Add either a vertical or a horizontal road
            int chooseXY = rand.nextInt(2);
            // Make vertical road (Starts at the top)
            if (chooseXY == 0) {
                int randomX = rand.nextInt(width);
                List<Element> row = cells.get(randomX);
                for (int i = 0; i < height; i++) {
                    row.set(i, new Road(randomX, i, parameter_manager));
                }
                // Make horizontal road (Starts at the left)
            } else {
                int randomY = rand.nextInt(height);
                for (int i = 0; i < width; i++) {
                    List<Element> row = cells.get(i);
                    row.set(randomY, new Road(i, randomY, parameter_manager));
                }
            }
        }

        // FIRE
        // Imagine the map as a 3x3 grid, the fire will always spawn in this cell:
        // X X X
        // X F X
        // X X X
        boolean fireStarted = false;
        while (!fireStarted) {
            int rand_x = rand.nextInt(width);
            int rand_y = rand.nextInt(height);
            if (rand_x > width/3 && rand_x < 2*width/3 && rand_y > height/3 && rand_y < 2*height/3) {
                Element cell = cells.get(rand_x).get(rand_y);
                if (cell.isBurnable()) {
                    cell.setBurning();
                    fireStarted = true;
                }
            }
        }

        // AGENTS
        // This function manually makes sure agents can only spawn on grass or tree tiles that are not on fire.
        for (int i = 0; i < nr_agents; i++) {
            boolean agentPlaced = false;
            while (!agentPlaced) {
                int rand_x = rand.nextInt(width);
                int rand_y = rand.nextInt(height);
                Element cell = cells.get(rand_x).get(rand_y);
                if ((cell.getType().equals("Grass") || cell.getType().equals("Tree")) && !cell.isBurning()) {
                    Agent agent = new Agent(rand_x, rand_y, model, parameter_manager, i);
                    agents.add(agent);
                    agentPlaced = true;
                }
            }
        }

        model.setCells(cells);
        model.setAgents(agents);
    }

    /**
     * Places tiles in a circle with a random radius around a XY-coordinate
     */
    private void placeBlob(int originX, int originY, String element, int chancePlacement) {
        int radius = rand.nextInt(5);
        for (int x = 0; x <= radius; x++) {
            for (int y = 0; y + x <= radius; y++) {

                if (x == 0 && y == 0) {
                    continue;
                }
                // 75 % chance of placement to create a naturalistic feel map
                if (inBounds(originX + x, originY + y)) {
                    if(rand.nextInt(100) < chancePlacement) { placeElementBlob((originX + x), (originY + y), element); }
                }
                if (inBounds(originX + x, originY - y)) {
                    if(rand.nextInt(100) < chancePlacement) { placeElementBlob((originX + x), (originY - y), element); }
                }
                if (inBounds(originX - x, originY + y)) {
                    if(rand.nextInt(100) < chancePlacement) { placeElementBlob((originX - x), (originY + y), element); }
                }
                if (inBounds(originX - x, originY - y)) {
                    if(rand.nextInt(100) < chancePlacement) { placeElementBlob((originX - x), (originY - y), element); }
                }
            }
        }
    }

    /**
     * Places tiles in a circle with a random radius around a XY-coordinate
     */
    void placeSquare(int originX, int originY, String element) {
        int radius = rand.nextInt(5);
        for (int x = 0; x <= radius; x++) {
            for (int y = 0; y + x <= radius; y++) {
                if (x == 0 && y == 0) {
                    continue;
                }
                if (inBounds(originX + x, originY + y)) {
                    placeElementBlob((originX + x), (originY + y), element);
                }
                /*

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
                }*/
            }
        }
    }

    private void placeElementBlob(int x, int y, String element) {
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
    private boolean inBounds(int x, int y) {
        int maxX = width;
        int maxY = height;
        return x >= 0 && x < maxX
                && y >= 0 && y < maxY;
    }
}