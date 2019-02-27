package Learning;

import Model.Simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Features {

    public Features(){
    }

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
