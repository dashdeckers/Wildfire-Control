package Learning;

import Model.Agent;
import Model.Elements.Element;
import Model.Simulation;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.core.state.UnknownKeyException;
import burlap.mdp.core.state.annotations.DeepCopyState;
import cern.colt.list.adapter.ObjectListAdapter;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import java.lang.Math;

@DeepCopyState
public class Features implements Serializable {

    public double previousAction = -1;

    public Features() {}

    /**
     * Helper function to convert a list of doubles into an array of doubles.
     *
     * CoSyNe requires arrays of doubles, but if implementations require lists instead it might be nicer to only
     * convert when using CoSyNe (i.e. have CoSyNe convert it).
     *
     * This takes a non-zero time, so if you have lots of Lists which need to be combined
     * it might be nicer to combine the lists first and then convert.
     * @param input
     * @return
     */
    public double[] doubleListToArray(List<Double> input) {
        Double[] outputArray = new Double[input.size()];
        input.toArray(outputArray);
        return Stream.of(outputArray).mapToDouble(Double::doubleValue).toArray() ;
    }


    /**
     * This gets a simple array where each entry represent whether a burning, burnable, or agent value.
     * Each set of three maps to a cell
     * @param model The model from which to generate a map
     * @return  A simplified map
     */
    public double[] get3Map(Simulation model){

        List<List<Element>> cells = model.getAllCells();
        List<Double> output = new ArrayList<>();

        for (int x = 0; x < cells.size(); x++) {
            for (int y = 0; y < cells.get(x).size(); y++) {

                output.add(cells.get(x).get(y).isBurning() ? 1.0 : 0.0);

                output.add(cells.get(x).get(y).isBurnable() ? 1.0 : 0.0);

                if (model.getAgents().get(0).getX() == x && model.getAgents().get(0).getY() == y) {
                    output.add(1.0);
                } else {
                    output.add(0.0);
                }
            }
        }
        return doubleListToArray(output);
    }

    /**
     * Returns a down-sampled version of the fuel values of the map. It combines a 3x3 grid of values into a
     * single value with no overlap. Last two elements of list/array are downSampledWidth, downSampledHeight
     * @param model The simulation
     * @return A down sampled map
     *
     * Example: Map (width=8, height=8, widthN=3, heightN=3 ) -> downsampled map:
     * grids 1 (3x3) evaluated in 1st iteration nested for loop
     * grid 2 (2x3) evaluated after 1st iteration nested for loop (tilesExtraRight > 0 )
     * grids 3 (3x3) evaluated in 2nd iteration nested for loop
     * grid 4 (2x3) evaluated after 2nd iteration nested for loop (tilesExtraRight > 0 )
     * grids 5 (3x2) evaluated after double for loop (tilesExtraBelow > 0)
     * grid 6 (2x2) evaluated at last (tilesExtraRight > 0 && tilesExtraBelow > 0)
     *
     * 1 1 2
     * 3 3 4
     * 5 5 6
     */
    public double[] downSampledFuelMap(Simulation model, int widthN, int heightN, int print) {

        // Alterations testing: Main

        List<List<Element>> cells = model.getAllCells();
        List<Double> output = new ArrayList<>();
        double[] arrayTriples = {0,0,0};

        int width = model.getParameter_manager().getWidth();
        int height = model.getParameter_manager().getHeight();
        int downSampleWidth = width/widthN;
        int downSampleHeight = height/heightN;

        int tilesExtraRight = 0;
        int tilesExtraBelow = 0;
        if (width % widthN != 0){ tilesExtraRight = width % widthN; } // Save amount of tiles that don't fit in square on right side map
        if (height % heightN != 0){ tilesExtraBelow = height % heightN; } // Save amount of tiles that don't fit in square on upper side map

        int saveJ = 0 ;

        /** Go over model in steps of 'widthN-heightN'. The 'checkIfSquareBurnable' function goes over the tiles (in x&y direction)
         * and currently returns '0.0' if NOT burnable, '1.0' if burnable, '2.0' if burning, '3.0' if agent is in square.
         * Starts in left upper corner and moves to the right until it hits the edge. Moves down one row after
         */
        for (int i = 0; i < downSampleHeight ; i+= 1){
            for (int j = 0; j < downSampleWidth; j+=1) {
                arrayTriples = checkIfSquareBurnable (widthN, heightN, j*widthN, height-1 -(i*heightN), cells, model);
                for (int k = 0; k < 3; k++){ output.add(arrayTriples[k]); }
                saveJ = j;
            }
            /** A row has been checked (left->right). If there are extra tiles on the right that did not fit in a square, a square with
             * smaller width (=tilesExtraRight) is evaluated to not go over bounds but still save the info from the map.
             */
            if (tilesExtraRight > 0 ) {
                arrayTriples = checkIfSquareBurnable(tilesExtraRight, height, saveJ * widthN, height-1 -(i*heightN), cells, model);
                for (int k = 0; k < 3; k++){ output.add(arrayTriples[k]); }
            }
        }
        /** All rows (and possible extra tiles on the right) are checked now. It could be that the lowest row with height
         * < heightN is still unchecked below. If so add that row to the list
         */
        if (tilesExtraBelow > 0) {
            for (int i = 0; i < downSampleHeight ; i += 1){
                arrayTriples = checkIfSquareBurnable (widthN, tilesExtraBelow, i * widthN,  height - downSampleHeight * heightN, cells, model);
                for (int k = 0; k < 3; k++){ output.add(arrayTriples[k]); }
            }
        }
        /** Finally it could be the case that all rows (and possible extra tiles to the right of them) + the last row on the
         * bottom are checked but there are still unchecked tiles left in the lower right corner. Add those to the list
         */
        if (tilesExtraRight > 0 && tilesExtraBelow > 0) {
            arrayTriples = checkIfSquareBurnable(tilesExtraRight, tilesExtraBelow, downSampleWidth * widthN, height-1 - downSampleHeight * heightN, cells, model);
            for (int k = 0; k < 3; k++){ output.add(arrayTriples[k]); }
        }

        // Add the downSampledWidth & downSampledHeight to end of list
        if ( tilesExtraRight > 0){  downSampleWidth++;} output.add ( (double)downSampleWidth);
        if ( tilesExtraBelow > 0){  downSampleHeight++;} output.add ( (double)downSampleHeight);

        // If a printed array is wanted, print the array
        double [] doubleArray = doubleListToArray(output);
        if (print == 1) {
            printArray(doubleArray);
        } else if (print == 2){
            printMap(doubleArray);
        } else if (print == 3){
            printArray(doubleArray);
            printMap(doubleArray);
        }

        return doubleArray;
    }

    /** Checks a square with a certain width/height, starting at a coordinate (x,y). Returns:
     * 3.0 if the agent is in a square
     * 2.0 is a square is burning
     * 1.0 if a square is burnable
     * 0.0 if a square is unburnable
     * @param width
     * @param height
     * @param x
     * @param y
     * @param model
     * @return
     */

    public double[] checkIfSquareBurnable(int width, int height, int x, int y, List<List<Element>> cells, Simulation model){

        //TODO: Add parameter isBurned to 'Element' , to retrieve more accurate info map

        int agent = 0;
        int burnable = 0;
        int burning = 0;
        double [] doubleArray = {0,0,0};

        // Loop over the square starting at (x,y) for (width,height)
        for (int i = x; i < (x + width) ; i++) {
            for (int j = y; j < (y + height) ; j++) {

                Element cell = cells.get(i).get(y);
                if (model.getAgents().get(0).getX() == i && model.getAgents().get(0).getY() == j && agent == 0) { // ensure value is binary
                    agent=1;
                }
                if (cell.isBurning() && burning == 0){ // ensure value is binary
                    burning=1;
                }
                if (cell.isBurnable() && burnable == 0) { // ensure value is binary
                    burnable=1;
                }
            }
        }

        doubleArray[0] = (double)agent;
        doubleArray[1] = (double)burning;
        doubleArray[2] = (double)burnable;
        return doubleArray;

    }

    private static void printArray(double [] doubleArray) {

        int downSampleWidth = (int)doubleArray[doubleArray.length-2];
        int newline = 0;
        int separate = 0;

        for(int i=0; i< doubleArray.length-2; i++){
            if (newline == 3*downSampleWidth) {
                System.out.printf("%n");
                newline = 0;
                separate = 0;
            } else if (separate == 3){ // else if so no separates on right side map
                System.out.printf("| ");
                separate = 0;
            }
            System.out.print(doubleArray[i] + " ");
            newline++;
            separate++;
        }
        System.out.printf("%n%n");

    }

    private static void printMap(double [] doubleArray) {

        int downSampleWidth = (int)doubleArray[doubleArray.length-2];
        int newline = 0;
        int step = 1;
        int print = 0;

        for(int i=0; i< doubleArray.length-2; i++){

            if (newline == 3*downSampleWidth) {
                System.out.printf("%n");
                newline = 0;
            }
            if (step == 1 && doubleArray[i] == 1){ // first value from triplets = agent
                System.out.print("A  ");
                print = 1;
            }
            else if (step == 2 && doubleArray[i] == 1 && print != 1){ // second value from triplets = burning
                System.out.print("F  ");
                print = 1;
            }
            else if (step == 3 && doubleArray[i] == 1 && print != 1) { // third value from triplets = burnable
                System.out.print(".  ");
            }
            if (step == 3){ // else if so no separates on right side map
                step = 0;
                print = 0;
            }
            newline++;
            step++;
        }
        System.out.printf("%n%n");

    }

    /** Overarching function that combines the output of the functions
     * -windRelativeToSubgoal
     * -distanceToCenterFire
     * -distanceFromCenterToFireline
     * into one array
     *
     * Input: vectors, compassDirection of current subgoal and current coordinates subgoal
     *
     * // TODO:
     */

    public double[] inputCosyne (String compassDirection, Simulation model, int x, int y) {
        //windRelativeToSubgoal (int windVectorX, int windVectorY, String compassDirection)
        //distanceToCenterFire(Simulation model, int x, int y)
        //distanceFromCenterToFireline(Simulation model, String compassDirection)

        double[] input = {0,0,0};
        // TODO: Extract windvectors automatically
        input[0] = windRelativeToSubgoal (-1, 0, compassDirection);
        input[1] = distanceToCenterFire(model, x, y);
        input[2] = distanceFromCenterToFireline(model, compassDirection);

        System.out.println(input[0] + " " + input[1] + " " + input[2]);
        return input;
    }

    /** Wind direction
     * New approach:
     * V1: Wind
     * V2: Subgoal Vector
     * A = projection of wind on compass direction (degrees as with unit circle)
     *
     * 1) angle alpha = acos ( V1 . V2 ) : Dotproduct
     * 2) A = V1 * cos(alpha)
     */

    //public double windRelativeToSubgoal( int windVectorX, int windVectorY, int windSpeed, String compassDirection){
    public double windRelativeToSubgoal (float windVectorX, float windVectorY, String compassDirection) {

        double alpha = 0;
        double A = 0;
        double[] subGoalVector = {0,0};
        double[] windVector = { (double)windVectorX , (double)windVectorY };

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
        alpha = Math.acos( dotProduct(windVector, subGoalVector) );

        // Step 2 : Calculate A
        A = Math.sqrt(windVector[0] * windVector[0] + windVector[1] * windVector[1]) * Math.cos(alpha);


        if ( A == 6.123233995736766E-17){ // Needed in order to account for well known rounding error with cosine
            A = 0;
        }
        //System.out.println(A);
        return A;


    }

    public static double dotProduct(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }


    /** Distance to center (from subgoal)
     * Needs the coordinates of the point
     */
    public double distanceToCenterFire(Simulation model, int x, int y){
        double distance;
        double[] centerFireAndMinMax = {0,0,0,0,0,0}; // 0 = centerX, 1 = centerY, 2 = minX, 3 = maxX, 4 = minY, 5 = maxY
        centerFireAndMinMax = locationCenterFireAndMinMax(model);

        double deltaX;
        double deltaY;

        deltaX = Math.abs((double)x - centerFireAndMinMax[0]);
        deltaY = Math.abs((double)y - centerFireAndMinMax[1]);

        // Given that deltaX or deltaY is always positive
        if (deltaX == 0){
            distance = deltaY;
        }
        if (deltaY == 0){
            distance = deltaX;
        }
        else { // Use pythogaros
            double c = (deltaX*deltaX)+(deltaY*deltaY);
            distance = Math.sqrt(c);
        }
        //System.out.print(distance + "%n");
        return distance;
    }

    /** Distance fireline to center
     * => A Feature needed for the NN to determine the placement of the subgoal
     * 1) Center of the fire is calculated
     * 2) minX, maxX, minY, maxY are determined
     * 3) Depending on the compass direction the subgoal is placed the distance is calculated
     * Compass directions:
     *              NN
     *         NW        NE
     *     WW                EE
     *         SW        SE
     *              SS
     */
    public double distanceFromCenterToFireline(Simulation model, String compassDirection){
        double distanceCenterToFire = 0;
        double[] centerFireAndMinMax = {0,0,0,0,0,0}; // 0 = centerX, 1 = centerY, 2 = minX, 3 = maxX, 4 = minY, 5 = maxY
        centerFireAndMinMax = locationCenterFireAndMinMax(model);

        //System.out.println(Arrays.toString(centerFireAndMinMax));

        double centerX = centerFireAndMinMax[0];
        double centerY = centerFireAndMinMax[1];
        double minX = centerFireAndMinMax[2];
        double maxX = centerFireAndMinMax[3];
        double minY = centerFireAndMinMax[4];
        double maxY = centerFireAndMinMax[5];

        switch (compassDirection) {
            case "NN":
                distanceCenterToFire = maxY - centerY;                break;
            case "NE":
                // Assumption: NE = (NN + EE)/2
                distanceCenterToFire = ((maxY - centerY) + (maxX - centerX))/2;                break;
            case "EE":
                distanceCenterToFire = maxX - centerX;                break;
            case "SE":
                // Assumption: SE = (SS + EE)/2
                distanceCenterToFire = ((centerY - minY) + (maxX - centerX))/2;                break;
            case "SS":
                distanceCenterToFire = centerY - minY;                break;
            case "SW":
                // Assumption: SW = (SS + WW)/2
                distanceCenterToFire = ((centerY - minY) + (centerX - minX))/2;                break;
            case "WW":
                distanceCenterToFire = centerX - minX;                break;
            case "NW":
                // Assumption: NW = (NN + WW)/2
                distanceCenterToFire = ((maxY - centerY) + (centerX - minX))/2;                break;
        }

        //System.out.print(distanceCenterToFire + "%n");
        return distanceCenterToFire;

    }

    /** Helper function for distanceCenterFireline
     * - Goes over whole map and stores all cells on fire. Calculates meanX and meanY as center fire
     * - Simultaneously stores minX, maxX, minY, maxY
     *
     * Also returns values if no fire
     */
    public double[] locationCenterFireAndMinMax (Simulation model){
        // blablablablablablablabla
        double width = model.getParameter_manager().getWidth();
        double height = model.getParameter_manager().getHeight();
        List<List<Element>> cells = model.getAllCells();

        double centerX = 0, centerY = 0, minX = 0, maxX = 0, minY = 0, maxY = 0;
        double numberOfTilesBurning = 0;

        int firstTile = 0;

        // Loop over the grid
        for (double i = 0; i < width ; i++) {
            for (double j = 0; j < height ; j++) {

                Element cell = cells.get((int)i).get((int)j);

                if ( cell.isBurning() ){
                    numberOfTilesBurning++;
                    centerX += i; // add x location burning tile, later divide by total tiles
                    centerY += j; // add y location burning tile, later divide by total tiles

                    if (firstTile == 0){
                        minX = i;
                        minY = j;
                        firstTile = 1;
                    }
                    if (i < minX ){
                        minX = i;
                    }
                    if (i > maxX) { // always true first time
                        maxX = i;
                    }
                    if (j < minY ) {
                        minY = j;
                    }
                    if (j > maxY) { // always true first time
                        maxY = j;
                    }



                }

            }
        }
        centerX = centerX/numberOfTilesBurning;
        centerY = centerY/numberOfTilesBurning;
        double[] locationCenterFireAndMinMax = {0,0,0,0,0,0};
        locationCenterFireAndMinMax[0] = centerX;
        locationCenterFireAndMinMax[1] = centerY;
        locationCenterFireAndMinMax[2] = minX;
        locationCenterFireAndMinMax[3] = maxX;
        locationCenterFireAndMinMax[4] = minY;
        locationCenterFireAndMinMax[5] = maxY;

        return locationCenterFireAndMinMax;
    }

    /**
     * Returns an array of doubles containing the distances of each agent to the burning cell nearest to it.
     * Because the agents container is a list, it should be consistent in the order
     * @param model The simulation
     * @return An array of distances to fire for each agent
     */
    public double[] distancesToFire(Simulation model) {
        List<Double> output = new ArrayList<>();
        for (Agent a : model.getAgents()) {
            int ax = a.getX();
            int ay = a.getY();
            Element nearestFire = model.getNearestFireTo(ax, ay);
            int fx = nearestFire.getX();
            int fy = nearestFire.getY();
            output.add(Math.sqrt(Math.pow(ax - fx, 2) + Math.pow(ay - fy, 2)));
        }
        return doubleListToArray(output);
    }

    /**
     * Returns an array of doubles containing the angles of each agent to the burning cell nearest to it.
     * This uses a reference vector (0, 1) as a "compass" and computes the angle between that and the
     * vector (agent, fire).
     * Because the agents container is a list, it should be consistent in the order
     * @param model The simulation
     * @return An array of angles to fire for each agent
     */
    public double[] anglesToFire(Simulation model) {
        List<Double> output = new ArrayList<>();
        for (Agent a : model.getAgents()) {
            int refVecX = 0;
            int refVecY = 1;
            if(a == null){
                System.out.println("No agent!");
            }
            Element nearestFire = model.getNearestFireTo(a.getX(), a.getY());
            if(nearestFire == null){
                System.out.println("NO nearest fire");
            }
            int afVecX = a.getX() - nearestFire.getX();
            int afVecY = a.getY() - nearestFire.getY();
            output.add(Math.atan2(afVecX*refVecY - afVecY*refVecX, afVecX*refVecX + afVecY*refVecY));
        }
        return doubleListToArray(output);
    }

    public double[] previousAction(){
        double val;
        if(previousAction == 4){
            val = 1;
        }else{
            val = 0;
        }
        double[] out = {val};//TODO
        return out;
    }

    public double[] fireVectors(Simulation model){
        List<Double> output = new ArrayList<>();
        for (Agent a : model.getAgents()) {
            int refVecX = 0;
            int refVecY = 1;
            if(a == null){
                System.out.println("No agent!");
            }
            Element nearestFire = model.getNearestFireTo(a.getX(), a.getY());
            if(nearestFire == null){
                System.out.println("NO nearest fire");
            }
            double afVecX = a.getX() - nearestFire.getX();
            double afVecY = a.getY() - nearestFire.getY();
            output.add(afVecX);
            output.add(afVecY);
        }
        return doubleListToArray(output);

    }

    /**
     * Used to combine any set of arrays in sequence, so you can easily have multiple features combined
     * @param arrays
     * @return
     */
    public double[] appendArrays(double[]... arrays){
        double[] output = new double[0];
        for(double[] array : arrays){
            output = DoubleStream.concat(Arrays.stream(output), Arrays.stream(array)).toArray();
        }

        return output;
    }

    public double[] appendArrays(double[] arr, double val){
        double[] valArr = {val};
        return appendArrays(arr, valArr);

    }

    public double[] anglesAndDistances(Simulation model) {
        List<Double> output = new ArrayList<>();
        for (Agent a : model.getAgents()) {
            int ax = a.getX();
            int ay = a.getY();
            int refVecX = 0;
            int refVecY = 1;
            Element nearestFire = model.getNearestFireTo(ax, ay);
            if(nearestFire == null){
                System.out.println("No nearest fire!");
            }
            int fx = nearestFire.getX();
            int fy = nearestFire.getY();
            int afVecX = ax - fx;
            int afVecY = ay - fy;
            output.add(Math.atan2(afVecX*refVecY - afVecY*refVecX, afVecX*refVecX + afVecY*refVecY));
            output.add(Math.sqrt(Math.pow(ax - fx, 2) + Math.pow(ay - fy, 2)));
        }
        return doubleListToArray(output);
    }

    /**
     * The combined set of angle, distance and previousAction features
     * @param model
     * @return
     */
    public double[] angleDistAct(Simulation model){
        return  appendArrays(anglesAndDistances(model), previousAction());
    }


    /**
     * Check whether a horizontal or vertical line contains any fire.
     * This is useful when trying to identify the borders of the fire
     * @param model
     * @param c
     * @param vertical
     * @return
     */
    public boolean isClearLine (Simulation model, int c, boolean vertical){
        if(vertical){
            if(c < 0 || c >= model.getParameter_manager().getWidth()){
                return false;
            }
            int y = 0;
            while(y < model.getParameter_manager().getHeight()){
                if(model.getElementAt(c, y).isBurning()){
                    return false;
                }
                y++;
            }
            return true;
        }else{
            if(c < 0 || c >= model.getParameter_manager().getHeight()){
                return false;
            }
            int x = 0;
            while(x < model.getParameter_manager().getWidth()){
                if(model.getElementAt(x, c).isBurning()){
                    return false;
                }
                x++;
            }
            return true;
        }
    }

    /**
     * Returns two points which represent a rectangle which matches the fire (fire can be on border of this rectangle
     * @param model
     * @return
     */
    public double[] fit_square(Simulation model){
        int x1, y1, x2, y2;
        x1 = y1 = 0;

        //Move lower left corner inward until you can't
        while(isClearLine(model, x1, true)){
            x1++;
        }
        while(isClearLine(model, y1, false)){
            y1++;
        }
        x2 = model.getParameter_manager().getWidth() -1 ;
        y2 = model.getParameter_manager().getHeight() -1 ;
        //Move up right corner inward until you can't
        while(isClearLine(model, x2, true)){
            x2--;
        }
        while(isClearLine(model, y2, false)){
            y2--;
        }

        double[] output = {x1, y1, x2, y2};
        return output;
    }

    /**
     * Extracts the length of vectors in seq x1,y1,x2,y2,x3,y3...
     * Into l1, x1, y1, l2, x2, y2, l3, x3, y3
     * Where li is the length of the vector, and xi & yi are the normalized vector
     * @param vec2s
     * @return
     */
    public double[] extractNormalize(double[] vec2s){
        double[] output = new double[vec2s.length + vec2s.length/2];
        int slot = 0;
        for(int i = 0; i < vec2s.length; i+= 2){
            //A^2 + B^C = C^2
            double length = Math.sqrt(vec2s[i] * vec2s[i] + vec2s[i+1] * vec2s[i+1]);
            output[slot] = length;
            slot++;
            //normalized vector = vector/vector_length
            output[slot] = vec2s[i]/length;
            if(length ==0){
                output[slot] = 0;
            }
            slot++;
            output[slot] = vec2s[i+1]/length;
            if(length == 0){
                output[slot] = 0;
            }
            slot++;
        }
        return output;
    }

    /**
     * Retreives the distances and angles (in unit vector) of the agent to the corners of the fire.
     * Currently only a rectangle is fitted, but an implementation which supports octagons can be implemnted.
     * @param model
     * @param use_octagon
     * @param a
     * @return
     */
    public double[] cornerVectors(Simulation model, Agent a, boolean use_octagon){
        //Retrieve two corners of a rectangle to fit the fire
        double[] square_c = fit_square(model);
        //Turn those two corners into four corners (appears to improve performance but maybe it shouldn't)
        double[] square_expanded = {square_c[0], square_c[1], square_c[0], square_c[3], square_c[2], square_c[1], square_c[2], square_c[3]};
        for(int i = 0; i< square_expanded.length; i+=2){
            //Turn the locations into vectors relative to agent
            square_expanded[i] = square_expanded[i] - a.getX();
            square_expanded[i+1] = square_expanded[i+1] - a.getY();
        }


        //Normalize the vectors and register the distances in different nodes
        return extractNormalize(square_expanded);
    }

    public double[] cornerVectors(Simulation model, boolean use_octagon){
        return cornerVectors(model, model.getAgents().get(0), use_octagon);
    }

}
