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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

@DeepCopyState
public class Features implements MutableState {

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
                output.add ( checkIfSquareBurnable (widthN, heightN, j*widthN, height-1 -(i*heightN), cells, model) );
                saveJ = j;
            }
            /** A row has been checked (left->right). If there are extra tiles on the right that did not fit in a square, a square with
             * smaller width (=tilesExtraRight) is evaluated to not go over bounds but still save the info from the map.
             */
            if (tilesExtraRight > 0 ) {
                output.add ( checkIfSquareBurnable(tilesExtraRight, height, saveJ * widthN, height-1 -(i*heightN), cells, model) );
            }
        }
        /** All rows (and possible extra tiles on the right) are checked now. It could be that the lowest row with height
         * < heightN is still unchecked below. If so add that row to the list
         */
        if (tilesExtraBelow > 0) {
            for (int i = 0; i < downSampleHeight ; i += 1){
                output.add ( checkIfSquareBurnable (widthN, tilesExtraBelow, i * widthN,  height - downSampleHeight * heightN, cells, model) );
            }
        }
        /** Finally it could be the case that all rows (and possible extra tiles to the right of them) + the last row on the
         * bottom are checked but there are still unchecked tiles left in the lower right corner. Add those to the list
         */
        if (tilesExtraRight > 0 && tilesExtraBelow > 0) {
            output.add ( checkIfSquareBurnable(tilesExtraRight, tilesExtraBelow, downSampleWidth * widthN, height-1 - downSampleHeight * heightN, cells, model) );
        }

        // Add the downSampledWidth & downSampledHeight to end of list
        if ( tilesExtraRight > 0){  downSampleWidth++;} output.add ( (double)downSampleWidth);
        if ( tilesExtraBelow > 0){  downSampleHeight++;} output.add ( (double)downSampleHeight);

        // If a printed array is wanted, print the array
        double [] doubleArray = doubleListToArray(output);
        if (print == 1){  printArray(doubleArray); }

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

    public double checkIfSquareBurnable(int width, int height, int x, int y, List<List<Element>> cells, Simulation model){

        int burnable = 0;
        int burning = 0;
        // Loop over the square starting at (x,y) for (width,height)
        for (int i = x; i < (x + width) ; i++) {
            for (int j = y; j < (y + height) ; j++) {

                Element cell = cells.get(i).get(y);
                if (model.getAgents().get(0).getX() == i && model.getAgents().get(0).getY() == j) {
                    return 3.0;
                }
                if (cell.isBurning() ){
                    burning++;
                }
                if (cell.isBurnable()) {
                    burnable++;
                }
            }
        }
        if (burning > 0) {
            return 2.0;
        }
        if (burnable > 0) {
            return 1.0;
        } else {
            return 0.0;
        }
    }

    private static void printArray(double [] doubleArray) {

        int downSampleWidth = (int)doubleArray[doubleArray.length-2];
        int newline = 0;

        for(int i=0; i< doubleArray.length-2; i++){
            if (newline == downSampleWidth) {
                System.out.printf("%n");
                newline = 0;
            }
            System.out.print(doubleArray[i] + " ");
            newline++;
        }
        System.out.printf("%n");
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
        double[] out = {previousAction};
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
            int y = 0;
            while(y < model.getParameter_manager().getHeight()){
                if(model.getElementAt(c, y).isBurning()){
                    return false;
                }
                y++;
            }
            return true;
        }else{
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
     * Returns two points which reprsent a rectangle which matches the fire (fire can be on border of this rectangle
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
     * @return
     */
    public double[] cornerVectors(Simulation model, boolean use_octagon){
        //Retrieve two corners of a rectangle to fit the fire
        double[] square_c = fit_square(model);
        //Turn those two corners into four corners (appears to improve performance but maybe it shouldn't)
        double[] square_expanded = {square_c[0], square_c[1], square_c[0], square_c[3], square_c[2], square_c[1], square_c[2], square_c[3]};
        for(int i = 0; i< square_expanded.length; i+=2){
            //Turn the locations into vectors relative to agent
            square_expanded[i] = square_expanded[i] - model.getAgents().get(0).getX();
            square_expanded[i+1] = square_expanded[i+1] - model.getAgents().get(0).getY();
        }


        //Normalize the vectors and register the distances in different nodes
        return extractNormalize(square_expanded);
    }


    /**
     *  Methods and fields related to BURLAP
     */

    public double A_N, A_S, A_E, A_W, D_N, D_S, D_E, D_W, X, Y;
    private static String AN_K = "AN", AS_K = "AS", AE_K = "AE", AW_K = "AW", DN_K = "DN", DS_K = "DS", DE_K = "DE", DW_K = "DW", X_K = "X", Y_K = "Y";
    private final static List<Object> keys = Arrays.asList(AN_K, AS_K, AE_K, AW_K, DN_K, DS_K, DE_K, DW_K, X_K, Y_K);

    // somewhere here, we need to call a function to get all these values from the simulation
    public Features(double A_N, double A_S, double A_E, double A_W, double D_N, double D_S, double D_E, double D_W, double X, double Y) {
        this.A_N = A_N;
        this.A_S = A_S;
        this.A_E = A_E;
        this.A_W = A_W;
        this.D_N = D_N;
        this.D_S = D_S;
        this.D_E = D_E;
        this.D_W = D_W;
        this.X = X;
        this.Y = Y;
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        if (variableKey.equals(A_N)) {
            this.A_N = StateUtilities.stringOrNumber(value).doubleValue();
        } else if (variableKey.equals(A_S)) {
            this.A_S = StateUtilities.stringOrNumber(value).doubleValue();
        } else if (variableKey.equals(A_E)) {
            this.A_E = StateUtilities.stringOrNumber(value).doubleValue();
        } else if (variableKey.equals(A_W)) {
            this.A_W = StateUtilities.stringOrNumber(value).doubleValue();
        } else if (variableKey.equals(D_N)) {
            this.D_N = StateUtilities.stringOrNumber(value).doubleValue();
        } else if (variableKey.equals(D_S)) {
            this.D_S = StateUtilities.stringOrNumber(value).doubleValue();
        } else if (variableKey.equals(D_E)) {
            this.D_E = StateUtilities.stringOrNumber(value).doubleValue();
        } else if (variableKey.equals(D_W)) {
            this.D_W = StateUtilities.stringOrNumber(value).doubleValue();
        } else if (variableKey.equals(X)) {
            this.X = StateUtilities.stringOrNumber(value).doubleValue();
        } else if (variableKey.equals(Y)) {
            this.Y = StateUtilities.stringOrNumber(value).doubleValue();
        } else {
            throw new UnknownKeyException(variableKey);
        }
        return this;
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object variableKey) {
        if (variableKey.equals(A_N)) {
            return A_N;
        } else if (variableKey.equals(A_S)) {
            return A_S;
        } else if (variableKey.equals(A_E)) {
            return A_E;
        } else if (variableKey.equals(A_W)) {
            return A_W;
        } else if (variableKey.equals(D_N)) {
            return D_N;
        } else if (variableKey.equals(D_S)) {
            return D_S;
        } else if (variableKey.equals(D_E)) {
            return D_E;
        } else if (variableKey.equals(D_W)) {
            return D_W;
        } else if (variableKey.equals(X)) {
            return X;
        } else if (variableKey.equals(Y)) {
            return Y;
        }
        throw new UnknownKeyException(variableKey);
    }

    @Override
    public Features copy() {
        return new Features(A_N, A_S, A_E, A_W, D_N, D_S, D_E, D_W, X, Y);
    }

    @Override
    public String toString() {
        return StateUtilities.stateToString(this);
    }
}
