package Learning;

import Model.Simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Features {

    public Features(){
    }

    /**
     * This is some default set which just returns 0s. It's simply an example to test things.
     * CoSyNe requires arrays of doubles, but if implementations require lists instead it might be nicer to only
     * convert when using CoSyNe (i.e. have CoSyNe convert it).
     * @param model
     * @return
     */
    public double[] getZeroSet(Simulation model){
        List<Double>  output = new ArrayList<>();
        for (int i = 0; i < 400; i++) {

            output.add(0.0);
        }
        Double[] outputArray = new Double[output.size()];
        output.toArray(outputArray);
        return Stream.of(outputArray).mapToDouble(Double::doubleValue).toArray() ;
    }
}
