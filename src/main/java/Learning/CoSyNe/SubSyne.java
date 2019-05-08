package Learning.CoSyNe;

import Learning.Fitness;
import Model.Agent;
import Model.Elements.Element;
import Model.Simulation;
import Navigation.SubGoal;
import View.MainFrame;

import javax.swing.*;
import java.util.Arrays;

public class SubSyne extends CoSyNe{

    Simulation model;
    boolean previousaction =true;
    public SubSyne(){
        super();
        performLearning();
    }

    /**
     * The original testMLP assume that this is the RL controller, but that's not the case.
     * We copied that code and changed some things around to fit the task.
     */
    @Override
    protected void testMLP(){
        //double[] dist = {4,4,4,4,4,4,4,4};
        //model.setSubGoals(dist);
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
    void performAction(int action, Agent a) {


        if(previousaction){
            a.makeDirt();
            previousaction = false;
        }else {
            /*
            System.out.println(Arrays.toString(getInput()));
            if(getInput()[0] > 0){
                a.moveRight();
            }else if(getInput()[1] > 0){
                a.moveLeft();
            }else if(getInput()[2] > 0){
                a.moveUp();
            }else{
                a.moveDown();
            }
            */


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
    int defN_generations() {
        return 1000;
    }

    @Override
    int[] defHiddenLayers() {
        int[] hl = {4};
        return hl;
    }

    @Override
    int defN_outputs() {
        return 4;
    }

    @Override
    int defBagSize() {
        return 20;
    }

    @Override
    int defGenerationSize() {
        return defBagSize() * 30;
    }

    @Override
    float defAlpha() {
        return 0.05f;
    }

    @Override
    int defN_children() {
        return 1;
    }

    @Override
    double[] getInput() {
        if(model == null){
            model = new Simulation(this);
            model.applySubgoals();
        }
        Agent agent = model.getAgents().get(0);
        Element goal = agent.goal.goal;
        /*
        double[] output = {
                (((double) goal.getX() - (double) agent.getX() )/ (double) model.getParameter_manager().getWidth() + 1) / 2,
                (((double) goal.getY() - (double) agent.getY() )/ (double) model.getParameter_manager().getHeight() + 1) / 2
        };*/


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
        //System.out.println("Agent at " + model.getAgents().get(0).getX() + " " + model.getAgents().get(0).getY()
        //+ "While target at " + model.getAgents().get(0).goal.goal.getX() + " " + model.getAgents().get(0).goal.goal.getY()
        //);
        //System.out.println(Arrays.toString(output));
        return output;
    }

    @Override
    double getFitness() {
        Fitness fit = new Fitness();

        Fitness.SPE_Measure StraightPaths = fit.new SPE_Measure(model);

        //return StraightPaths.getFitness(2);
        return /*fit.totalFuelBurnt(model) +*/ 10 * (
                (model.getAgents().get(0).goal.goal.getX() - model.getAgents().get(0).getX()) * (model.getAgents().get(0).goal.goal.getX() - model.getAgents().get(0).getX()) +
                (model.getAgents().get(0).goal.goal.getY() - model.getAgents().get(0).getY()) * (model.getAgents().get(0).goal.goal.getY() - model.getAgents().get(0).getY()))
                - 1000 *model.goalsHit  +
                fit.totalFuelBurnt(model);
    }

    @Override
    protected int defWeightSpread(){
        return 3;
    }
}
