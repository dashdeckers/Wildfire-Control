package Learning;

import Model.Agent;
import Model.Elements.Element;
import Model.Simulation;

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
     * single value with no overlap.
     * @param model The simulation
     * @return A down sampled map
     */
    public double[] downSampledFuelMap(Simulation model) {
        if (! (model.getParameter_manager().getWidth() % 3 == 0
            && model.getParameter_manager().getHeight() % 3 == 0) ) {
            System.out.println("Map dimensions incompatible with down-sampling (not divisible by 3)");
        }
        List<Double> output = new ArrayList<>();

        //TODO: implement downsampling

        return doubleListToArray(output);
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
