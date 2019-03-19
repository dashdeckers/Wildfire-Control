package Learning;
import Model.Agent;
import Model.Simulation;
import View.MainFrame;
import org.neuroph.core.Connection;
import org.neuroph.core.Layer;
import org.neuroph.core.Neuron;
import org.neuroph.core.Weight;
import org.neuroph.nnet.MultiLayerPerceptron;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;


public class Cosyne implements RLController {


    MultiLayerPerceptron current_mlp;
    Simulation current_model;
    Features features;
    int generation;
    int best = -1;
    List<Integer> mlpSize;
    public Cosyne(){

        features = new Features();

        /*Initialize parameters */
        int inputs = getInputs(null).length;   //Fetches input size from getInputs
        int outputs = 6;
        List<Integer> hiddenLayers = new ArrayList<>();

        int population = 250;    //Change this to change number of MLPs

        hiddenLayers.add(20);//Add more hidden layers as you see fit
        float permutation_chance = 0.01f;   //Chance that a gene is random rather than inhereted


        List<Map.Entry<MultiLayerPerceptron, Double>> mlpList = new ArrayList<>();
        mlpSize = new ArrayList<>();
        mlpSize.add(inputs);
        for(Integer size : hiddenLayers){
            mlpSize.add(size);
        }
        mlpSize.add(outputs);

        //Initialize subpopulation, takes a while
        for(int i = 0; i<population; i++){
            Map.Entry<MultiLayerPerceptron, Double> entry = new AbstractMap.SimpleEntry<MultiLayerPerceptron, Double>(new MultiLayerPerceptron(mlpSize),0.0);
            mlpList.add(entry);
        }

        List<Map.Entry<MultiLayerPerceptron, Double>> mlp_children = new ArrayList<>();
        List<Map.Entry<MultiLayerPerceptron, Double>> mlp_parents= new ArrayList<>();


        generation = 0;
        //The learning loop!
        while(generation < 750) {
            generation++;

            //Run & evaluate the MLPS
            int[] scores = evaluate(mlpList);
            //Identify the cutoff
            int decision_fitness = scores[scores.length / 4];   //Theory says 25% lives, so either /4 or /4*3
            //Split the population between parents and children
            split(mlp_children, mlp_parents, mlpList,(double) decision_fitness);
            //Print performance measures
            printPerformance(scores, mlp_parents);
            //Update the children with the parents genes
            breed(mlp_children, mlp_parents, permutation_chance);


        }


    }

    /**
     * Utility to print the performance
     * @param scores    A sorted array of scores of the MLPs
     * @param mlp_parents   A list of MLPs which have survived the sorting
     */
    private void printPerformance(int[] scores, List<Map.Entry<MultiLayerPerceptron,Double>> mlp_parents) {
        if(best == -1 || scores[0] <= best){
            best = scores[0];
        }
        System.out.println("Min score: " + scores[0]);
        //System.out.println("Median at: " + median);
        //System.out.println("Max score: " + scores[scores.length -1]);
        System.out.println("Generation "+ generation);
        float parent_mean = 0;
        for(Map.Entry entry : mlp_parents){
            parent_mean += (Integer) entry.getValue();
        }
        parent_mean /= mlp_parents.size();
        System.out.println("Mean parent performance " + parent_mean);
    }

    /**
     * Split all the mlps into either parents (survivors) or children (dead).
     * Because of pass-by-reference an action taken on mlp_children (i.e. breed()) will influence the full mlpList
     * @param mlp_children  Will hold the list of children which are to be killed
     * @param mlp_parents   Will hold the list of parents which are to survive
     * @param mlpList       Needs to hold the full set of MLPs which are to be split
     * @param decisionFitness   A threshold to determine when MLPs die and when they live
     */
    private void split(List<Map.Entry<MultiLayerPerceptron,Double>> mlp_children,
                       List<Map.Entry<MultiLayerPerceptron,Double>> mlp_parents,
                       List<Map.Entry<MultiLayerPerceptron,Double>> mlpList,
                       Double decisionFitness) {
        mlp_children.clear();
        mlp_parents.clear();

        //Sort in winners and losers where performance is compared to division fitness
        for (Map.Entry entry : mlpList) {
            if ( ((Integer) entry.getValue()).doubleValue() >= (double) decisionFitness ) {    //Change this between >< to switch high/low fitness
                //Loser, so needs to be changed
                mlp_children.add(entry);
            }else{
                mlp_parents.add(entry);
                //Winner, so gets to reproduce
            }
        }

        List<Map.Entry> removeChildren = new ArrayList<>();
        for(Map.Entry entry: mlp_children){
            if(mlp_parents.size() < mlpList.size() / 4 ){
                if  ( ((Integer) entry.getValue()).doubleValue() == (double) decisionFitness ){
                    mlp_parents.add(entry);
                    removeChildren.add(entry);
                }
            }else{
                break;
            }
        }

        for(Map.Entry entry: removeChildren){
            mlp_children.remove(entry);
        }
        //System.out.println("Nr children " + mlp_children.size());
        //System.out.println("Nr parents " + mlp_parents.size());
    }

    /**
     * Have every MLP make and perform a simulation, calculate it fitness and add it to it's list
     * @param mlpList
     * @return  A sorted array of performances, so we can easily identify a fitness cutoff
     */
    private int[] evaluate (List<Map.Entry<MultiLayerPerceptron, Double>> mlpList){
        int[] scores = new int[mlpList.size()];
        Fitness fitness = new Fitness();

        int i = 0;
        //Have every MLP perform the simulation and fetch their fitness
        for (Map.Entry entry : mlpList) {
            current_mlp = (MultiLayerPerceptron) entry.getKey();
            current_model = new Simulation(this);

            current_model.start();


            Fitness.SPE_Measure StraightPaths = fitness.new SPE_Measure(current_model);
            entry.setValue(StraightPaths.getFitness(2));
            scores[i] = StraightPaths.getFitness(2);

            //If it's a new best run the simulation again and take a screenshot
            if(best != -1 && scores[i] < best){
                current_mlp = (MultiLayerPerceptron) entry.getKey();
                current_model = new Simulation(this);
                current_model.getParameter_manager().changeParameter("Model", "Step Time", 1000f);

                JFrame f = new MainFrame(current_model);
                current_model.start();
                try {
                    Thread.sleep(Math.abs(1000));
                } catch (java.lang.InterruptedException e) {
                    System.out.println(e.getMessage());
                }
                screenshot(generation, scores[i]);

                f.dispose();

            }
            if(best != -1 && scores[i] < best) {
                System.out.println("Fitness " + scores[i]);
            }
            i++;
        }
        //Sort the fitnesses to calculate the median
        Arrays.sort(scores);

        return scores;
    }

    private void screenshot(int generation, int i){
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        try {
            BufferedImage capture = new Robot().createScreenCapture(screenRect);
            ImageIO.write(capture, "bmp", new File("./screenshot_g"+ generation+"_i_"+i+".bmp"));

        }catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Updates the children to get crossover genes from random parents
     * @param mlp_children  The mlps that need to be killed/generated
     * @param mlp_parents   The mlps which hold good genes
     * @param permutation_chance Probability of a random new gene not from either parent
     */
    private void breed( List<Map.Entry<MultiLayerPerceptron, Double>> mlp_children,  List<Map.Entry<MultiLayerPerceptron, Double>> mlp_parents, float permutation_chance){
        Random rng = new Random();

        //The evolution loop:
        //For each child
        for(int ib = 0; ib < mlp_children.size(); ib ++){
            MultiLayerPerceptron child = mlp_children.get(ib).getKey();

            MultiLayerPerceptron parent_1 = mlp_parents.get(rng.nextInt(mlp_parents.size())).getKey();
            MultiLayerPerceptron parent_2 = mlp_parents.get(rng.nextInt(mlp_parents.size())).getKey();



            //For each layer
            for(int il = 0; il < child.getLayersCount(); il++){

                int neurons = child.getLayerAt(il).getNeuronsCount();
                //For each neuron
                for(int in = 0; in < neurons; in++){

                    //IntelliJ refers to the jar as source, which is not up to date with the compiled source at mvn
                    //Therefore IntelliJ expects list, but the compiler knows it's arrays
                    int nconnections = child.getLayerAt(il).getNeuronAt(in).getInputConnections().length;
                    //For each (inbound) connection to a neuron
                    for(int ic = 0; ic < nconnections; ic++){

                        int origin_rate = rng.nextInt(5);
                        //Take some random ratio between the two parents
                        double weight =
                                parent_1.getLayerAt(il).getNeuronAt(in).getInputConnections()[ic].getWeight().value * origin_rate
                                + parent_2.getLayerAt(il).getNeuronAt(in).getInputConnections()[ic].getWeight().value * (1-origin_rate);


                        //Take the weight from the parent giving the neuron
                        Weight newWeight = new Weight(weight);

                        //In case of a permutation set it to something random
                        if(rng.nextFloat() < permutation_chance){

                            child.getLayerAt(il)
                                    .getNeuronAt(in)
                                    .getInputConnections()[ic]
                                    .setWeight(new Weight(rng.nextDouble())
                                    );
                        }else{
                            //Else set it the the parent
                            child.getLayerAt(il)
                                    .getNeuronAt(in)
                                    .getInputConnections()[ic]
                                    .setWeight(newWeight
                                    );
                        }
                    }
                }


            }

        }
    }

    public double[] getInputs(Simulation model){
        if(model == null){
           model = new Simulation(false);
        }
        return features.appendArrays(features.previousAction(), features.cornerVectors(model, false));
    }

    /**
     * Pick an action for the agent calling this.
     * At this stage the MLP (which initated the simulation) gets to pick an action for the calling agent
     * @param a
     */
    @Override
    public void pickAction(Agent a) {
        //The features are generated with the feature class based on the model
        current_mlp.setInput(getInputs(current_model));
        current_mlp.calculate();
        double[] outputs = current_mlp.getOutput();
        double max_out= 0.0;
        int action = -1;
        //We simply apply the maximum action, since we already have a doNothing action
        for(int i = 0; i<outputs.length; i++){
            if(action == -1 || outputs[i] > max_out){
                max_out = outputs[i];
                action = i;
            }
        }

        //Tell the features what our current action is, so next time it'll know the previous action
        features.previousAction = action;
        switch (action){
            case 0:
                a.moveDown();
                break;
            case 1:
                a.moveUp();
                break;
            case 2:
                a.moveLeft();
                break;
            case 3:
                a.moveRight();
                break;
            case 4:
                a.makeDirt();
                break;
            case 5:
                a.doNothing();
                break;
            default:
                System.out.println("NO ACTION FOR OUTPUT at Cosyne.pickAction");
        }
    }
}

