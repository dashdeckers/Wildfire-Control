package Learning.CoSyNe.HRL;

import Learning.CoSyNe.SubSyne;
import Learning.CoSyNe.WeightBag;
import Learning.Fitness;
import Model.Simulation;
import View.MainFrame;
import javax.swing.*;

/**
 * We build on SubSyne, since this already knows how to navigate to a subgoal, and use GoalLearner to pick the subgoals.
 */
public class ActionLearner extends SubSyne {

    private Double bestGoalFitness;
    private Double meanGoalFitness;
    private GoalLearner goalLearner;
    public ActionLearner(){
        super();
    }

    @Override
    /**
     * Change the testMLP to have goalLearner pick subGoals
     */
    protected void testMLP(){
        //Goal learner does not run its own generation, so testMLP asks for inputs, and grants fitness
        if(goalLearner == null){
            goalLearner = new GoalLearner();
        }
        double[] dist = goalLearner.generateGoals(model);
        model.setSubGoals(dist);
        model.applySubgoals();


        model.start();
        for(int layer = 0; layer < weightBags.size(); layer++){
            for(int neuron = 0; neuron < weightBags.get(layer).size(); neuron++){
                for(int weight = 0; weight < weightBags.get(layer).get(neuron).size(); weight++){
                    WeightBag bag = weightBags.get(layer).get(neuron).get(weight);
                    bag.updateFitness(getFitness());
                }
            }
        }
        //We grant a fitness to goalLearner
        goalLearner.setFitness(getGoalFitness());

        mean_perfomance += getFitness();
        if(best_performance == null || getFitness() < best_performance){
            best_performance = getFitness();
        }
        if(ultimate_performance == null || getFitness() < ultimate_performance){    //take screenshot
            ultimate_performance = getFitness();

            model = new Simulation(this);
            model.getParameter_manager().changeParameter("Model", "Step Time", 1000f);
            JFrame f = new MainFrame(model);
            model.applySubgoals();
            model.start();
            try {
                Thread.sleep(Math.abs(1000));
            } catch (java.lang.InterruptedException e) {
                System.out.println(e.getMessage());
            }
            screenshot(0, (int) getFitness());
            f.dispose();
        }
        model = new Simulation(this);
    }

    @Override
    /**
     * Changed printPerformance to add some more insights relevant to the HRL
     */
    protected void printPerformance(){
        System.out.println("Best performance: " + best_performance + " , " + bestGoalFitness);
        System.out.println("Mean performance: " + mean_perfomance + " , " + meanGoalFitness/defGenerationSize());
        System.out.println("Mean confidence: " + mean_confidence / conf_counter);
        bestGoalFitness = null;
        meanGoalFitness = null;

    }

    @Override
    /**
     * Override the breeding step to inform GoalLearner that it needs to breed
     */
    protected void breed(){
        for(int layer = 0; layer < weightBags.size(); layer++){
            for(int neuron = 0; neuron < weightBags.get(layer).size(); neuron++) {
                for (int weight = 0; weight < weightBags.get(layer).get(neuron).size(); weight++) {
                    WeightBag bag = weightBags.get(layer).get(neuron).get(weight);
                    bag.breed(defN_children());
                }
            }
        }
        goalLearner.breed();
    }

    /**
     * We have an extra function to determine the fitness of the goal.
     * The goal does not need to care whether the agent is able to reach it, only whether the map burns.
     * @return
     */
    private double getGoalFitness(){
        Fitness fit = new Fitness();
        if(bestGoalFitness == null || fit.totalFuelBurnt(model) < bestGoalFitness){
            bestGoalFitness = (double) fit.totalFuelBurnt(model);
        }
        if(meanGoalFitness == null){
            meanGoalFitness = new Double(0);
        }
        meanGoalFitness += fit.totalFuelBurnt(model);
        return fit.totalFuelBurnt(model);
    }

}
