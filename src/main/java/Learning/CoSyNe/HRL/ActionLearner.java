package Learning.CoSyNe.HRL;

import Learning.CoSyNe.CoSyNe;
import Learning.CoSyNe.SubSyne;
import Learning.CoSyNe.WeightBag;
import Learning.Fitness;
import Model.Agent;
import Model.Simulation;
import View.MainFrame;

import javax.swing.*;

public class ActionLearner extends SubSyne {

    private Double bestGoalFitness;
    private Double meanGoalFitness = new Double(0);
    private Double meanGoalDist;

    private GoalLearner goalLearner;
    public ActionLearner(){
        super();
    }

    @Override
    protected void testMLP(){
        if(goalLearner == null){
            goalLearner = new GoalLearner();
        }
        double[] dist = goalLearner.generateGoals(model);
        if(meanGoalDist == null){
            meanGoalDist = new Double(0);
        }
        for(int i =0; i< dist.length; i++){
            meanGoalDist += dist[i];
        }
        model.setSubGoals(dist);
        model.applySubgoals();
        //System.out.println(Arrays.toString( model.getSubGoals()));


        model.start();
        for(int layer = 0; layer < weightBags.size(); layer++){
            for(int neuron = 0; neuron < weightBags.get(layer).size(); neuron++){
                for(int weight = 0; weight < weightBags.get(layer).get(neuron).size(); weight++){
                    WeightBag bag = weightBags.get(layer).get(neuron).get(weight);
                    bag.updateFitness(getFitness());
                }
            }
        }
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
    protected void printPerformance(){
        System.out.println("Best performance: " + best_performance + " , " + bestGoalFitness);
        System.out.println("Mean performance: " + mean_perfomance + " , " + meanGoalFitness/defGenerationSize());
        System.out.println("Mean goalDist: " + meanGoalDist/(defGenerationSize()*model.getSubGoals().length));
        bestGoalFitness = null;
        meanGoalFitness = null;
        meanGoalDist = null;

    }

    @Override
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
