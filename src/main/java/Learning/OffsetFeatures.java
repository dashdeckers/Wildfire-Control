package Learning;

import Model.Agent;
import Model.Elements.Element;
import Model.Simulation;
import Navigation.OrthogonalSubgoals;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;


public class OffsetFeatures implements Serializable {

    private List<Agent> agents;


    public Simulation model;
    public double degree;

    public double xGoal;
    public double yGoal;
    public int nrAgents;



    private double[][] distanceAgenttoSubgoal;






    public OffsetFeatures(Simulation model) {

        this.model = model;
        distanceAgenttoSubgoal = model.getDistAgentToSubgoal();
    }

    /**
     * Tell the features what the degree of this subgoal is (except we normalize it, so the degrees are divide by 360)
     *
     * @param degree
     */
    public void setDegree(double degree) {
        this.degree = degree;
    }

    /**
     * For now we simply return the angle as a feature, but that might be changed to something more meaningful for less trivial situations
     *
     * @return
     */
    public double[] getResult2() {
        double[] out = new double[1];
        out[0] = degree;
        return out;
    }




    public double[] getResult() {

        //System.out.println("in GETRESULT");

        //TODO: fix weird 0-7, 1-8 problem
        xGoal = (double)model.subGoals.xOfGoal((int)degree);
        yGoal = (double)model.subGoals.yOfGoal((int)degree);

        //System.out.println("xGoal = " + xGoal);
        //System.out.println("yGoal = " + yGoal);

        //Add up number of agents to array
        agents = model.getAgents();
        nrAgents = model.getNr_agents();
        distanceAgenttoSubgoal = model.getDistAgentToSubgoal();


        //System.out.println(Arrays.deepToString(distanceAgenttoSubgoal).replace("], ", "]\n"));
        //System.out.println("--------------------------");

        double[] out = new double[3 + nrAgents];

        //TODO: fix weird 0-7, 1-8 problem
        switch ((int)degree) {
            case 0:
                out = inputCosyne("WW", model,  (int)xGoal , (int)yGoal);
                break;
            case 1:
                out = inputCosyne("SW", model,  (int)xGoal , (int)yGoal);
                break;
            case 2:
                out = inputCosyne("SS", model,  (int)xGoal , (int)yGoal);
                break;
            case 3:
                out = inputCosyne("SE", model,  (int)xGoal , (int)yGoal);
                break;
            case 4:
                out = inputCosyne("EE", model,  (int)xGoal , (int)yGoal);
                break;
            case 5:
                out = inputCosyne("NE", model,  (int)xGoal , (int)yGoal);
                break;
            case 6:
                out = inputCosyne("NN", model,  (int)xGoal , (int)yGoal);
                break;
            case 7:
                out = inputCosyne("NW", model,  (int)xGoal , (int)yGoal);
                break;
        }
        //System.out.println("degree = " + degree);
        //System.out.println("out = " + Arrays.toString(out));

//        DecimalFormat df = new DecimalFormat("##.##");
//        for (int i = 0; i < 3+model.getNr_agents(); i++){
//                System.out.print(df.format(out[i]) + " ");
//        }
//        System.out.print("\n");


        return out;

    }



    /**
     * Overarching function that combines the output of the functions
     * -windRelativeToSubgoal (normalized) :    CHECK
     * -distanceToCenterFire :                  CHECK : subgoal sometimes in center fire for 2 & 6?
     * -distanceFromCenterToFireline            CHECK
     * -distancesAgentsToSubs
     * into one array
     * <p>
     * Input: vectors, compassDirection of current subgoal and current coordinates subgoal
     * <p>
     * // TODO:
     */

    public double[] inputCosyne(String compassDirection, Simulation model, int x, int y) {
        //windRelativeToSubgoal (int windVectorX, int windVectorY, String compassDirection)
        //distanceToCenterFire(Simulation model, int x, int y)
        //distanceFromCenterToFireline(Simulation model, String compassDirection)

        int lengthArray = 3 + nrAgents;

        double[] input = new double[lengthArray];


        // TODO: Extract windvectors automatically
        input[0] = windRelativeToSubgoal(-1, 0, compassDirection);
        input[1] = distanceToCenterFire(model, x, y);
        input[2] = distanceFromCenterToFireline(model, compassDirection);

        //System.out.println("input = " + Arrays.toString(input));

        for (int i = 0; i < nrAgents; i++){
            input[i+3] = distanceFromAgentToSubgoal(agents.get(i), x, y, compassDirection);
        }



        //System.out.println("Nragents = " + nrAgents);
//        System.out.print(compassDirection + " ");
//        for (int i = 0 ; i < 3 + nrAgents ; i++){
//            System.out.print(input[i] + " ");
//        }
//        System.out.println();

        //System.out.println("Compass = " + compassDirection);

        return input;
    }

    /**
     * DistanceFromAgentToSubgoal
     */
    public double distanceFromAgentToSubgoal(Agent agent, int xGoal, int yGoal, String compassDirection){
        double distance = 0;
        double deltaX = Math.abs( (double)agent.getX()  - ((double)xGoal));
        double deltaY = Math.abs( (double)agent.getY() - ((double)yGoal));

        //System.out.println("Coordinates subgoal  = " + xGoal + ", " + yGoal);
        //System.out.println("Coordinates agent    = " + (double)agent.getX() + ", " + (double)agent.getY());


        // Given that deltaX or deltaY is always positive
        if ((int)deltaX == 0 ) {
            distance = deltaY;
            //System.out.println("Distance = deltaY");
        }
        if ((int)deltaY == 0) {
            distance = deltaX;
            //System.out.println("Distance = deltaX");
        }
        if (deltaX != 0 && deltaY != 0) { // Use pythogaros
            double c = (deltaX * deltaX) + (deltaY * deltaY);
            distance = Math.sqrt(c);
            //System.out.println("Distance = sqrt");
        }
        //System.out.println("distance          ==== " + distance);

        // Normalize: max distance (if fire in center = Math.sqrt( (width/2)*(width/2) + (height/2)*(height/2) )
        double width = (double)model.getParameter_manager().getWidth();
        double height = (double)model.getParameter_manager().getHeight();
        distance = distance / (Math.sqrt( (height*height) + (width * width) ) );

        // Save the distances from the agents to the subgoals:
        int nrGoal = 0;
        switch (compassDirection) {
            case "WW":   nrGoal = 1;   break;
            case "SW":   nrGoal = 2;   break;
            case "SS":   nrGoal = 3;   break;
            case "SE":   nrGoal = 4;   break;
            case "EE":   nrGoal = 5;   break;
            case "NE":   nrGoal = 6;   break;
            case "NN":   nrGoal = 7;   break;
            case "NW":   nrGoal = 8;   break;
        }

        //System.out.println("nrAgents = " + nrAgents);
        //System.out.println("agent.getId() = " + agent.getId());
        //System.out.println("nrGoal-1 = " + (nrGoal-1));
        //System.out.println("distance = " + distance);


        //System.out.println(Arrays.deepToString(distanceAgenttoSubgoal).replace("], ", "]\n"));
        //System.out.println("--------------------------");
        distanceAgenttoSubgoal[agent.getId()][nrGoal-1] = distance;
        model.setDistAgentToSubgoal(distanceAgenttoSubgoal);


        //System.out.println("distance = " + distance);
        return distance;

    }



    /**
     * Wind direction
     * New approach:
     * V1: Wind
     * V2: Subgoal Vector
     * A = projection of wind on compass direction (degrees as with unit circle)
     * <p>
     * 1) angle alpha = acos ( V1 . V2 ) : Dotproduct
     * 2) A = V1 * cos(alpha)
     */

//public double windRelativeToSubgoal( int windVectorX, int windVectorY, int windSpeed, String compassDirection){
    public double windRelativeToSubgoal(int windVectorX, int windVectorY, String compassDirection) {

        double alpha = 0;
        double A = 0;
        double[] subGoalVector = {0, 0};
        double[] windVector = {(double) windVectorX, (double) windVectorY};

        // Set Subgoal Vector
        switch (compassDirection) {
            case "EE":
                subGoalVector[0] = 1;
                subGoalVector[1] = 0;
                break;
            case "NE":
                subGoalVector[0] = Math.sqrt(0.5);
                subGoalVector[1] = Math.sqrt(0.5);
                break;
            case "NN":
                subGoalVector[0] = 0;
                subGoalVector[1] = 1;
                break;
            case "NW":
                subGoalVector[0] = -Math.sqrt(0.5);
                subGoalVector[1] = Math.sqrt(0.5);
                break;
            case "WW":
                subGoalVector[0] = -1;
                subGoalVector[1] = 0;
                break;
            case "SW":
                subGoalVector[0] = -Math.sqrt(0.5);
                subGoalVector[1] = -Math.sqrt(0.5);
                break;
            case "SS":
                subGoalVector[0] = 0;
                subGoalVector[1] = -1;
                break;
            case "SE":
                subGoalVector[0] = Math.sqrt(0.5);
                subGoalVector[1] = -Math.sqrt(0.5);
                break;
        }

        // Step 1 : Calculate angle alpha
        alpha = Math.acos(dotProduct(windVector, subGoalVector));

        // Step 2 : Calculate A
        A = Math.sqrt(windVector[0] * windVector[0] + windVector[1] * windVector[1]) * Math.cos(alpha);

        //System.out.print(A + "%n");
        // Normalize:
        A = (A + 1)/2;
        return A;


    }

    public static double dotProduct(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }


    /**
     * Distance to center (from subgoal)
     * Needs the coordinates of the point
     */
    public double distanceToCenterFire(Simulation model, int x, int y) {
        double distance = 0;
        double[] centerFireAndMinMax = locationCenterFireAndMinMax(model); // 0 = centerX, 1 = centerY, 2 = minX, 3 = maxX, 4 = minY, 5 = maxY

        double deltaX;
        double deltaY;

        deltaX = Math.abs( (double) x - centerFireAndMinMax[0]);
        deltaY = Math.abs((double) y - centerFireAndMinMax[1]);

//        System.out.println("Coordinates subgoal = " + x + ", " + y);
//        System.out.println("Coordinates fire    = " + centerFireAndMinMax[0] + ", " + centerFireAndMinMax[1]);



        // Given that deltaX or deltaY is always positive
        if ((int)deltaX == 0 ) {
            distance = deltaY;
            //System.out.println("Distance = deltaY");
        }
        if ((int)deltaY == 0) {
            distance = deltaX;
            //System.out.println("Distance = deltaX");
        }
        if (deltaX != 0 && deltaY != 0) { // Use pythogaros
            double c = (deltaX * deltaX) + (deltaY * deltaY);
            distance = Math.sqrt(c);
            //System.out.println("Distance = sqrt");
        }
        //System.out.print(distance + "%n");

        // Normalize:
        //System.out.println("Distance            = " + distance);

        distance = distance / ((double)model.getParameter_manager().getWidth()/2);
        //System.out.println("Normalized Distance = " + distance);
        return distance;
    }

    /**
     * Distance fireline to center
     * => A Feature needed for the NN to determine the placement of the subgoal
     * 1) Center of the fire is calculated
     * 2) minX, maxX, minY, maxY are determined
     * 3) Depending on the compass direction the subgoal is placed the distance is calculated
     * Compass directions:
     *          NN
     *      NW        NE
     *   WW                EE
     *      SW        SE
     *          SS
     */
    public double distanceFromCenterToFireline(Simulation model, String compassDirection) {
        double distanceCenterToFire = 0;
        double[] centerFireAndMinMax = locationCenterFireAndMinMax(model);


        //System.out.println(Arrays.toString(centerFireAndMinMax));

        double centerX = centerFireAndMinMax[0];
        double centerY = centerFireAndMinMax[1];
        double minX = centerFireAndMinMax[2];
        double maxX = centerFireAndMinMax[3];
        double minY = centerFireAndMinMax[4];
        double maxY = centerFireAndMinMax[5];

        switch (compassDirection) {
            case "NN":
                distanceCenterToFire = maxY - centerY;
                break;
            case "NE":
                // Assumption: NE = (NN + EE)/2
                distanceCenterToFire = ((maxY - centerY) + (maxX - centerX)) / 2;
                break;
            case "EE":
                distanceCenterToFire = maxX - centerX;
                break;
            case "SE":
                // Assumption: SE = (SS + EE)/2
                distanceCenterToFire = ((centerY - minY) + (maxX - centerX)) / 2;
                break;
            case "SS":
                distanceCenterToFire = centerY - minY;
                break;
            case "SW":
                // Assumption: SW = (SS + WW)/2
                distanceCenterToFire = ((centerY - minY) + (centerX - minX)) / 2;
                break;
            case "WW":
                distanceCenterToFire = centerX - minX;
                break;
            case "NW":
                // Assumption: NW = (NN + WW)/2
                distanceCenterToFire = ((maxY - centerY) + (centerX - minX)) / 2;
                break;
        }

        //System.out.print(distanceCenterToFire + "%n");

        // Normalize:
        //System.out.println(distanceCenterToFire);
        distanceCenterToFire = distanceCenterToFire / ((double)model.getParameter_manager().getWidth()/2);
        //System.out.println(distanceCenterToFire);
        return distanceCenterToFire;

    }

    /**
     * Helper function for distanceCenterFireline
     * - Goes over whole map and stores all cells on fire. Calculates meanX and meanY as center fire
     * - Simultaneously stores minX, maxX, minY, maxY
     * <p>
     * Also returns values if no fire
     */
    public double[] locationCenterFireAndMinMax(Simulation model) {
        // blablablablablablablabla
        double width = model.getParameter_manager().getWidth();
        double height = model.getParameter_manager().getHeight();
        List<List<Element>> cells = model.getAllCells();

        double centerX = 0, centerY = 0, minX = 0, maxX = 0, minY = 0, maxY = 0;
        double numberOfTilesBurning = 0;

        int firstTile = 0;

        // Loop over the grid
        for (double i = 0; i < width; i++) {
            for (double j = 0; j < height; j++) {

                Element cell = cells.get((int) i).get((int) j);

                if (cell.isBurning()) {
                    numberOfTilesBurning++;
                    centerX += i; // add x location burning tile, later divide by total tiles
                    centerY += j; // add y location burning tile, later divide by total tiles

                    if (firstTile == 0) {
                        minX = i;
                        minY = j;
                        firstTile = 1;
                    }
                    if (i < minX) {
                        minX = i;
                    }
                    if (i > maxX) { // always true first time
                        maxX = i;
                    }
                    if (j < minY) {
                        minY = j;
                    }
                    if (j > maxY) { // always true first time
                        maxY = j;
                    }


                }

            }
        }
        centerX = centerX / numberOfTilesBurning;
        centerY = centerY / numberOfTilesBurning;
        double[] locationCenterFireAndMinMax = {0, 0, 0, 0, 0, 0};
        locationCenterFireAndMinMax[0] = centerX;
        locationCenterFireAndMinMax[1] = centerY;
        locationCenterFireAndMinMax[2] = minX;
        locationCenterFireAndMinMax[3] = maxX;
        locationCenterFireAndMinMax[4] = minY;
        locationCenterFireAndMinMax[5] = maxY;

        return locationCenterFireAndMinMax;
    }


    //public double[][] getDistanceAgentToSubgoal() { return distanceAgenttoSubgoal; }
}