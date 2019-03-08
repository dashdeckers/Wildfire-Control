package Learning;
import Model.Agent;
import Model.Simulation;
import View.MainFrame;
import org.neuroph.core.Connection;
import org.neuroph.core.Layer;
import org.neuroph.core.Neuron;
import org.neuroph.core.Weight;
import org.neuroph.nnet.MultiLayerPerceptron;

import javax.swing.*;
import java.lang.reflect.Array;
import java.util.*;


public class Cosyne implements RLController {


    MultiLayerPerceptron current_mlp;
    Simulation current_model;
    Features features;
    int generation;
    List<Integer> mlpSize;
    public Cosyne(){

        /*Initialize parameters */
        int population = 1000;    //Change this to change number of MLPs
        int inputs = 2;   //Change this to match input size
        int outputs = 6;
        int middle_layer = 50; //Change this for number of neurons in middle layer
        float permutation_chance = 0.5f;   //Chance that a gene is random rather than inhereted
        System.out.println("Inputs = " +inputs);
        System.out.println("Outputs = "+outputs);
        System.out.println("1 middle layer =" +middle_layer);
        System.out.println("Making a population of "+population+" MLPs");

        List<Map.Entry<MultiLayerPerceptron, Double>> mlpList = new ArrayList<>();
        mlpSize = new ArrayList<>();
        mlpSize.add(inputs);
        mlpSize.add(middle_layer);
        mlpSize.add(outputs);

        features = new Features();


        //Initialize subpopulation, takes a while
        for(int i = 0; i<population; i++){
            Map.Entry<MultiLayerPerceptron, Double> entry = new AbstractMap.SimpleEntry<MultiLayerPerceptron, Double>(new MultiLayerPerceptron(mlpSize),0.0);
            mlpList.add(entry);
        }

        System.out.println("Initialized MLPs");
        List<Map.Entry<MultiLayerPerceptron, Double>> mlp_children = new ArrayList<>();
        List<Map.Entry<MultiLayerPerceptron, Double>> mlp_parents= new ArrayList<>();


        generation = 0;
        //The learning loop!
        while(generation < 1000) {
            generation++;

            //Run & evaluate the MLPS
            int[] scores = evaluate(mlpList);
            //Identify the cutoff
            int decision_fitness = scores[scores.length / 2 ];   //Theory says 25% lives, so either /4 or /4*3
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
            if(mlp_parents.size() < mlpList.size() / 2){
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
        System.out.println("Nr children " + mlp_children.size());
        System.out.println("Nr parents " + mlp_parents.size());
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
            if((generation == 1 || generation % 10 == 0) && i < 10){
                current_model.getParameter_manager().changeParameter("Model", "Step Time", 1000f);

                JFrame f = new MainFrame(current_model);
                current_model.start();
                f.dispose();
            }else{
                current_model.start();
            }

            Fitness.SPE_Measure StraightPaths = fitness.new SPE_Measure(current_model);
            entry.setValue(StraightPaths.getFitness(0));
            scores[i] = StraightPaths.getFitness(0);
            if((generation == 1 || generation % 10 == 0) && i < 10) {
                System.out.println("Fitness " + scores[i]);
            }
            i++;
        }
        //Sort the fitnesses to calculate the median
        Arrays.sort(scores);

        return scores;
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
        /* Good idea for when maximizing fitness, but problematic when minimizing
        int parent_total = 0;
        for(Map.Entry parent : mlp_parents){
            parent_total += (int) parent.getValue();
        }
        */
        for(int ib = 0; ib < mlp_children.size(); ib ++){
            MultiLayerPerceptron child = mlp_children.get(ib).getKey();
            //Find corresponding parent (all but the very top (in case of multiple at median) get to breed)

            /*
            int parent_int = rng.nextInt();
            int parent_counter = 0;
            MultiLayerPerceptron parent_1 = null;
            for(Map.Entry parent : mlp_parents){
                if(parent_counter + (int) parent.getValue() <= parent_int){
                    parent_1 = (MultiLayerPerceptron) parent.getKey();
                }
            }

            parent_int = rng.nextInt();
            parent_counter = 0;
            MultiLayerPerceptron parent_2 = null;
            for(Map.Entry parent : mlp_parents){
                if(parent_counter + (int) parent.getValue() <= parent_int){
                    parent_2 = (MultiLayerPerceptron) parent.getKey();
                }
            }
            */
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
                        boolean parent_picker = rng.nextBoolean();
                        MultiLayerPerceptron weight_parent;
                        if(parent_picker){
                            weight_parent = parent_1;
                        }else{
                            weight_parent = parent_2;
                        }

                        //Take the weight from the parent giving the neuron
                        Weight newWeight = new Weight(
                                weight_parent.getLayerAt(il)
                                        .getNeuronAt(in)
                                        .getInputConnections()[ic].getWeight().value
                        );

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

    /**
     * Pick an action for the agent calling this.
     * At this stage the MLP (which initated the simulation) gets to pick an action for the calling agent
     * @param a
     */
    @Override
    public void pickAction(Agent a) {
        //The features are generated with the feature class based on the model
        current_mlp.setInput(features.angleAndDistance(current_model));
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

