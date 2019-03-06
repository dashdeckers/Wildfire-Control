package Learning;
import Model.Elements.Agent;
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
    public Cosyne(){

        /*Initialize parameters */
        int population = 100;
        int inputs = 21*21*3;   //Change this to match input size
        int outputs = 6;
        int middle_layer = 100;
        float permutation_chance = 0.01f;
        System.out.println("Inputs = " +inputs);
        System.out.println("Outputs = "+outputs);
        System.out.println("1 middle layer =" +middle_layer);
        System.out.println("Making a population of "+population+" MLPs");

        List<Map.Entry<MultiLayerPerceptron, Double>> mlpList = new ArrayList<>();
        List<Integer> mlpSize = new ArrayList<>();
        mlpSize.add(inputs);
        mlpSize.add(middle_layer);
        mlpSize.add(outputs);

        features = new Features();


        //Initialize subpopulation, takes a while
        for(int i = 0; i<population; i++){
            Map.Entry<MultiLayerPerceptron, Double> entry = new AbstractMap.SimpleEntry<>(new MultiLayerPerceptron(mlpSize),0.0);
            mlpList.add(entry);
        }

        System.out.println("Initialized MLPs");
        Random rng = new Random();
        List<Map.Entry<MultiLayerPerceptron, Double>> mlp_children = new ArrayList<>();
        List<Map.Entry<MultiLayerPerceptron, Double>> mlp_parents= new ArrayList<>();


        int generation = 0;
        //The learning loop!
        while(generation < 50) {
            generation++;

            //Run & evaluate the MLPS
            int[] scores = evaluate(mlpList);
            //Identify the cutoff
<<<<<<< HEAD
            int decision_fitness = scores[scores.length / 4 * 3 ];
=======
            int decision_fitness = scores[scores.length / 4 * 3];
>>>>>>> f85c717f487e2401114e55ed06bded9c6b936125
            //Split the population between parents and children
            float parent_mean = split(mlp_children, mlp_parents, mlpList, decision_fitness);
            //Print performance measures
            printPerformance(scores, mlp_parents, parent_mean);
            //Update the children with the parents genes
            breed(mlp_children, mlp_parents, permutation_chance);


        }


    }

    private void printPerformance(int[] scores, List<Map.Entry<MultiLayerPerceptron,Double>> mlp_parents, float parent_mean) {
        //System.out.println("Min score: " + scores[0]);
        //System.out.println("Median at: " + median);
        //System.out.println("Max score: " + scores[scores.length -1]);
        //System.out.println("Generation "+ generation);
        System.out.println("Mean parent performance " + parent_mean);
    }

    private float split(List<Map.Entry<MultiLayerPerceptron,Double>> mlp_children,
                       List<Map.Entry<MultiLayerPerceptron,Double>> mlp_parents,
                       List<Map.Entry<MultiLayerPerceptron,Double>> mlpList,
                       int decisionFitness) {
        mlp_children.clear();
        mlp_parents.clear();

        float mean_p  = 0;
        //Sort in winners and losers (50/50 split, with median(s) as winners)
        for (Map.Entry entry : mlpList) {
            if ((int) entry.getValue() < decisionFitness ) {
                //Loser, so needs to be changed
                mlp_children.add(entry);
                //System.out.println("Killing child at " + entry.getValue());
            }else{
                mlp_parents.add(entry);
                //Winner, so gets to reproduce
                mean_p += (Integer) entry.getValue();
            }
        }
        mean_p /= mlp_parents.size();
        return mean_p;
    }

    private int[] evaluate (List<Map.Entry<MultiLayerPerceptron, Double>> mlpList){
        int[] scores = new int[mlpList.size()];

        int i = 0;
        //Have every MLP perform the simulation and fetch their fitness
        for (Map.Entry entry : mlpList) {
            current_mlp = (MultiLayerPerceptron) entry.getKey();
            current_model = new Simulation(this);
            JFrame f = new MainFrame(current_model);
            current_model.start();
            f.dispose();
            entry.setValue((Integer) current_model.getFitness());
            scores[i] = current_model.getFitness();
            i++;
            //System.out.println("Performed simulation with cost " + current_model.getFitness());
        }
        //Sort the fitnesses to calculate the median
        Arrays.sort(scores);

        return scores;
    }

    private void breed( List<Map.Entry<MultiLayerPerceptron, Double>> mlp_children,  List<Map.Entry<MultiLayerPerceptron, Double>> mlp_parents, float permutation_chance){
        Random rng = new Random();

        //The evolution loop:
        //For each child
        for(int ib = 0; ib < mlp_children.size(); ib ++){
            MultiLayerPerceptron child = mlp_children.get(ib).getKey();
            //Find corresponding parent (all but the very top (in case of multiple at median) get to breed)
            int parent_int = rng.nextInt(mlp_parents.size());
            MultiLayerPerceptron parent_1 = mlp_parents.get(parent_int).getKey();
            parent_int = rng.nextInt(mlp_parents.size());
            //Each parent select a random partner to procreate with
            MultiLayerPerceptron parent_2 = mlp_parents.get(parent_int).getKey();


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

    @Override
    public void pickAction(Agent a) {

        current_mlp.setInput(features.get200Map(current_model));
        current_mlp.calculate();
        double[] outputs = current_mlp.getOutput();
        double max_out= 0.0;
        int action = -1;
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

//TODO! Fix IDEA
