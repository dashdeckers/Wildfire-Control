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
     * @param input
     * @return
     */
    public double[] doubleListToArray(List<Double> input) {
        Double[] outputArray = new Double[input.size()];
        input.toArray(outputArray);
        return Stream.of(outputArray).mapToDouble(Double::doubleValue).toArray() ;
    }

    /**
     * This is some default set which just returns 0s. It's simply an example to test things.
     * @param model
     * @return
     */
    public double[] getZeroSet(Simulation model) {
        List<Double>  output = new ArrayList<>();
        for (int i = 0; i < 400; i++) {
            output.add(0.0);
        }
        return doubleListToArray(output);
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
     * Map (width=8, height=8, widthN=3, heightN=3 ) -> downsampled map:
     * grids 1 evaluated in 1st iteration nested for loop
     * grid 2 evaluated after 1st iteration nested for loop (tilesExtraRight > 0 )
     * grids 3 evaluated in 2nd iteration nested for loop
     * grid 4 evaluated after 2nd iteration nested for loop (tilesExtraRight > 0 )
     * grids 5 evaluated after double for loop (tilesExtraBelow > 0)
     * grid 6 evaluated at last (tilesExtraRight > 0 && tilesExtraBelow > 0)
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
        if (print == 1){  printArray(model, doubleArray); }

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

    private static void printArray(Simulation model, double [] doubleArray) {

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
     * Methods and fields related to BURLAP
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
