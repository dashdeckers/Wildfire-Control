package Learning.CoSyNe.HRL;

import Learning.CoSyNe.SubGoalLearning;
import Learning.CoSyNe.WeightBag;
import Model.Simulation;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Since the goalLearner fundamentally runs on SubGoalLearning we extend on that.
 * A key difference is that GoalLearner does not run its own generations, but is a puppet to ActionLearner
 */
public class GoalLearner extends SubGoalLearning {

    public GoalLearner(){
        super();
    }

    /**
     * We make our pick, always from a fresh MLP
     * @param model
     * @return
     */
    public double[] generateGoals(Simulation model){
        createMLP();    //renew our MLP
        double[] dist = model.getSubGoals();
        for(int i = 0; i < dist.length; i++){
            dist[i] = determineOffset(i, dist.length);
        }
        //System.out.println(Arrays.toString(dist));
        return dist;
    }

    /**
     * The master ActionLearner class determines a fitness for us.
     * @param fitness
     */
    public void setFitness(double fitness){
        for(int layer = 0; layer < weightBags.size(); layer++){
            for(int neuron = 0; neuron < weightBags.get(layer).size(); neuron++){
                for(int weight = 0; weight < weightBags.get(layer).get(neuron).size(); weight++){
                    WeightBag bag = weightBags.get(layer).get(neuron).get(weight);
                    bag.updateFitness(fitness);
                }
            }
        }
    }

    /**
     * Allow access to breeding from the ActionLearner
     */
    public void breed(){
        super.breed();
    }


    /**
     * We cancel the performLearning, since we don't iterate over generations
     */
    @Override
    protected void performLearning(){
        return;
    }
}
