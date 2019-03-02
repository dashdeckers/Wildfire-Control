package Learning;

import Model.Elements.Element;
import Model.Simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Features {

    public Features(){
    }

    public double[] doubleListToArray(List<Double> input){
        Double[] outputArray = new Double[input.size()];
        input.toArray(outputArray);
        return Stream.of(outputArray).mapToDouble(Double::doubleValue).toArray() ;
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
        return doubleListToArray(output);
    }

    public double[] get200Map(Simulation model){
        if(model == null){
            System.out.println("MODEL IS NULL!");
        }
        List<List<Element>> cells = model.getAllCells();
        List<Double> output = new ArrayList<>();

        for(int x = 0; x < cells.size(); x++){
            for(int y = 0; y < cells.get(x).size(); y++) {
                output.add(cells.get(x).get(y).isBurning() ? 1.0 : 0.0);
                output.add(cells.get(x).get(y).isBurnable() ? 1.0 : 0.0);
                if(model.getAgents().get(0).getX() == x && model.getAgents().get(0).getY() == y){
                    output.add(1.0);
                }else{
                    output.add(0.0);
                }
            }
        }


        return doubleListToArray(output);
    }
}
