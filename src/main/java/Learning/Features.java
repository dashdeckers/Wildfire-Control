package Learning;

import Model.Agent;
import Model.Elements.Element;
import Model.Simulation;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class Features {


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
     * grid 2 evaluated after 1st iteration nested for loop (tilesExtraAbove > 0 )
     * grids 3 evaluated in 2nd iteration nested for loop
     * grid 4 evaluated after 2nd iteration nested for loop (tilesExtraAbove > 0 )
     * grids 5 evaluated after double for loop (tilesExtraRight > 0)
     * grid 6 evaluated at last (tilesExtraRight > 0 && tilesExtraAbove > 0)
     * 1 1 2
     * 3 3 4
     * 5 5 6
     */
    public double[] downSampledFuelMap(Simulation model, int widthN, int heightN, int print) {

        // Alterations testing: Main and Generator (Plainmap)

        List<List<Element>> cells = model.getAllCells();
        List<Double> output = new ArrayList<>();

        int width = model.getParameter_manager().getWidth();
        int height = model.getParameter_manager().getHeight();

        int downSampleWidth = width/widthN;
        int downSampleHeight = height/heightN;

        int tilesExtraRight = 0;
        int tilesExtraAbove = 0;
        if (width % widthN != 0){ tilesExtraRight = width % widthN; } // Save amount of tiles that don't fit in square on right side map
        if (height % heightN != 0){ tilesExtraAbove = height % heightN; } // Save amount of tiles that don't fit in square on upper side map

        int saveJ = 0 ;

        /** Go over model in steps of 'widthN-heightN'. The 'checkIfSquareBurnable' function goes over the tiles (in x&y direction)
         * and returns '0.0' if NOT burnable, '1.0' if burnable, '2.0' if burning, '3.0' if agent is in square
         */
        for (int i = 0; i < downSampleHeight ; i+= 1){
            for (int j = 0; j < downSampleHeight; j+=1) {
                output.add ( checkIfSquareBurnable(widthN, heightN, (i*widthN), (j*heightN), cells, model) );
                saveJ = j;
            }
            /** A column has been checked (bottom->top). If there are extra tiles above that did not fit in a square, a square with
             * smaller height (=tilesExtraBelow) is evaluated to not go over bounds but still save the info from the map.
             */
            if (tilesExtraAbove > 0 ) {
                output.add ( checkIfSquareBurnable(widthN, tilesExtraAbove, (i * widthN), ( (saveJ+1) * heightN), cells, model) );
            }
        }
        /** All columns (and possible extra tiles above them) are checked now. It could be that a column with width
         * < widthN is still unchecked on the right. If so add that column to the list
         */
        if (tilesExtraRight > 0) {
            for (int i = 0; i < downSampleHeight ; i += 1){
                output.add ( checkIfSquareBurnable(tilesExtraRight, heightN, (downSampleWidth * widthN), ( i * heightN), cells, model) );
            }
        }
        /** Finally it could be the case that all columns (and possible extra tiles above them) + the last column on the
         * right are checked but there are still unchecked tiles left in the upper right corner. Add those to the list
         */
        if (tilesExtraRight > 0 && tilesExtraAbove > 0) {
            output.add ( checkIfSquareBurnable(tilesExtraRight, tilesExtraAbove, (downSampleWidth * widthN), (downSampleHeight * heightN), cells, model) );
        }

        // Add the downSampledWidth & downSampledHeight to end of list
        if ( tilesExtraRight > 0){  downSampleWidth++;}
        if ( tilesExtraAbove > 0){  downSampleHeight++;}
        output.add ( (double)downSampleWidth);
        output.add ( (double)downSampleHeight);

        double [] doubleArray = doubleListToArray(output);
        if (print == 1){
            printArray(model, doubleArray);
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
        int width = (int)doubleArray[doubleArray.length-2];
        int height = (int)doubleArray[doubleArray.length-1];
        int downSampleWidth = width/3;
        int downSampleHeight = height/3;
        if (width % 3 != 0){ downSampleWidth++; }
        if (height % 3 != 0){ downSampleHeight++; }

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
        //System.out.println(Arrays.toString(doubleArray));
    }

    public void flipArrayMinusNinetyDegrees(double[] doubleArray){
        double width = doubleArray[doubleArray.length-2];
        double height = doubleArray[doubleArray.length-1];
        doubleArray = ArrayUtils.removeElement(doubleArray, doubleArray.length-1);
        doubleArray = ArrayUtils.removeElement(doubleArray, doubleArray.length-1);

        double array2d[][] = new double[(int)width][(int) height];

        for(int i=0; i<10;i++) {
            for (int j = 0; j < 3; j++) {
                array2d[i][j] = doubleArray[(j * (int) width) + i];
            }
        }
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
}
