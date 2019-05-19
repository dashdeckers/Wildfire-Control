package Learning.CoSyNe;

import Learning.CoSyNe.*;
import Learning.Features;
import Learning.Fitness;
import Model.Agent;
import Model.Simulation;
import Learning.OffsetFeatures;
import View.MainFrame;
import org.neuroph.util.TransferFunctionType;

import javax.swing.*;
import java.io.Serializable;
import java.util.Arrays;

import static java.lang.Double.NaN;

/**
 * SubGoalLearning is the class which learns at what distances the subgoals should be placed.
 * While it is a RLcontroller, we will not be using it as such, since we're not picking actions but defining subgoals
 */
public class SubGoalLearning extends CoSyNe  {

    OffsetFeatures features;

    public SubGoalLearning(){
        super();
        model = new Simulation(false);  //Not Simulation(this), since we don't pick the individual moves
        performLearning();
    }

    /**
     * This does the picking of an offset. The MLP has 1 output neuron, which we scale to be between 1-width/2
     * @param iterator
     * @param max
     * @return
     */
    protected double determineOffset(int iterator, int max){
        double deg = (double)iterator / (double)max;    //Not actually degree, but normalized to 0-1
        if(deg == NaN){
            System.out.println("NaN degrees!");
        }
        features = new OffsetFeatures(model);
        features.setDegree(deg);
        mlp.setInput(getInput());
        mlp.calculate();
        return (mlp.getOutput()[0]*model.getParameter_manager().getWidth() +2)/2;

    }

    /**
     * The original testMLP assume that this is the RL controller, but that's not the case.
     * We copied that code and changed some things around to fit the task.
     */
    @Override
    protected void testMLP(){
        double[] dist = model.getSubGoals();
        for(int i = 0; i < dist.length; i++){
            dist[i] = determineOffset(i, dist.length);
        }
        model.setSubGoals(dist);
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
        mean_perfomance += getFitness();
        if(best_performance == null || getFitness() < best_performance){
            best_performance = getFitness();
        }
        if(ultimate_performance == null || getFitness() < ultimate_performance){    //take screenshot
            model = new Simulation(false);
            model.getParameter_manager().changeParameter("Model", "Step Time", 1000f);
            JFrame f = new MainFrame(model);
            dist = model.getSubGoals();
            for(int i = 0; i < dist.length; i++){
                dist[i] = determineOffset(i, dist.length);
            }
            model.setSubGoals(dist);
            model.start();
            try {
                Thread.sleep(Math.abs(1000));
            } catch (java.lang.InterruptedException e) {
                System.out.println(e.getMessage());
            }
            screenshot(0, (int) getFitness());
            ultimate_performance = getFitness();
            f.dispose();
        }
        model = new Simulation(false);
    }

    /**
     * we dont use this, since we're not acting as the RLcontroller
     * @param action
     * @param a
     */
    @Override
    protected void performAction(int action, Agent a) {

    }

    @Override
    protected int defN_generations() {
        return 20;
    }

    /**
     * Since inputs are only defined by angle for now, complexity it minimal
     * @return
     */
    @Override
    protected int[] defHiddenLayers() {
        int[] hl = {3};
        return hl;
    }

    /**
     * Only 1 output, from which the value is translated to an offset
     */
    @Override
    protected int defN_outputs() {
        return 1;
    }

    @Override
    protected int defBagSize() {
        return 30;
    }

    @Override
    protected int defGenerationSize() {
        return defBagSize()*10;
    }

    @Override
    protected float defAlpha() {
        return 0.05f;
    }

    @Override
    protected int defN_children() {
        return 10;
    }

    @Override
    protected double[] getInput() {
        if(features == null){
            features = new OffsetFeatures(model);
        }
        return features.getResult();
    }

    @Override
    protected double getFitness() {
        Fitness fit = new Fitness();

        return fit.totalFuelBurnt(model);
    }

    @Override
    protected int defWeightSpread(){
        return 3;
    }

    @Override
    /**
     * Use a sigmoid, since the output from 0-1 is scaled to be center-border.
     */
    protected TransferFunctionType defTransferFunction() {
        return TransferFunctionType.SIGMOID;
    }


    @Override
    protected double defCertainty(){
        return 1;
    }
}
