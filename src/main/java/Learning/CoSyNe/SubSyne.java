package Learning.CoSyNe;

import Learning.Fitness;
import Model.Agent;
import Model.Elements.Element;
import Model.Simulation;
import View.MainFrame;
import org.neuroph.util.TransferFunctionType;

import javax.swing.*;

/**
 * SubSyne class navigates between subgoals. This class is used by the ActionLearner, so changes made here may result in changes in the HRL approach.
 */
public class SubSyne extends CoSyNe{


    protected boolean previousaction =true; //Switch for ensuring every other action is a dig

    public SubSyne(){
        super();
        performLearning();
    }

    /**
     * We need to add subgoals, which the original testMLP didn't do, so we override that.
     */
    @Override
    protected void testMLP(){
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
        mean_perfomance += getFitness();
        if(best_performance == null || getFitness() < best_performance){
            best_performance = getFitness();
        }
        if(ultimate_performance == null || getFitness() < ultimate_performance){    //take screenshot
            ultimate_performance = getFitness();

            model = new Simulation(this);
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
    protected void performAction(int action, Agent a) {
        if(previousaction){ //every other action is a dig
            a.makeDirt();
            previousaction = false;
        }else {
            switch (action) {
                case 0:
                    a.moveRight();
                    break;
                case 1:
                    a.moveLeft();
                    break;
                case 2:
                    a.moveUp();
                    break;
                case 3:
                    a.moveDown();
                    break;
                default:
                    System.out.println("WARNING, no action defined for action in SubSyne.performAction()");
            }
            previousaction = true;
        }
    }

    @Override
    protected int defN_generations() {
        return 1000;
    }

    @Override
    protected int[] defHiddenLayers() {
        int[] hl = {4};
        return hl;
    }

    @Override
    protected int defN_outputs() {
        return 4;
    }

    @Override
    protected int defBagSize() {
        return 20;
    }

    @Override
    /**
     * Large generation size grants a more accurate representation of how good a weight is.
     * Though 30 x might be a bit much
     */
    protected int defGenerationSize() {
        return defBagSize() * 30;
    }

    @Override
    protected float defAlpha() {
        return 0.05f;
    }

    @Override
    protected int defN_children() {
        return 5;
    }

    @Override
    /**
     * Input is the scaled x&y difference to the next subgoal
     */
    protected double[] getInput() {
        if(model == null){
            model = new Simulation(this);
            model.applySubgoals();
        }
        Agent agent = model.getAgents().get(0);
        Element goal = agent.goal.goal;


        double[] output = new double[4];

        if(agent.getX() > goal.getX()){
            output[0] = 0;
            output[1] = (agent.getX() - goal.getX() )/ (double) model.getParameter_manager().getWidth();
        }else{
            output[0] = (goal.getX() - agent.getX()) / (double) model.getParameter_manager().getWidth();
            output[1] = 0;
        }

        if(agent.getY() > goal.getY()){
            output[2] = 0;
            output[3] = (agent.getY() - goal.getY()) / (double) model.getParameter_manager().getHeight();
        }else{
            output[2] = (goal.getY() - agent.getY()) / (double) model.getParameter_manager().getHeight();
            output[3] = 0;
        }
        return output;
    }

    @Override
    /**
     * Fitness is derived from distance to next subgoal, number of subgoals reached, and the area of map burned
     * Due to the stochastic behavior it might be possible to remove the distance to next subgoal
     */
    protected double getFitness() {
        Fitness fit = new Fitness();
        return  10 * (
                (model.getAgents().get(0).goal.goal.getX() - model.getAgents().get(0).getX()) * (model.getAgents().get(0).goal.goal.getX() - model.getAgents().get(0).getX()) +
                (model.getAgents().get(0).goal.goal.getY() - model.getAgents().get(0).getY()) * (model.getAgents().get(0).goal.goal.getY() - model.getAgents().get(0).getY()))
                - 1000 *model.goalsHit  +
                fit.totalFuelBurnt(model);
    }

    @Override
    protected int defWeightSpread(){
        return 3;
    }

    @Override
    protected TransferFunctionType defTransferFunction() {
        return TransferFunctionType.RECTIFIED;
    }

    @Override
    /**
     * Certainty is inverse, so 0.05 grants the ability to be very certain.
     * A too small number (0.01) creates NaNs.
     */
    protected double defCertainty(){
        return 0.05;
    }
}
