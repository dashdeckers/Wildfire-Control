package Learning;
import Model.Elements.Agent;
import Model.Simulation;
import org.neuroph.nnet.MultiLayerPerceptron;

import java.util.*;


public class Cosyne implements RLController {


    MultiLayerPerceptron current_mlp;
    Simulation current_model;
    Features features;
    public Cosyne(){
        int population = 20;
        int inputs = 10*10*4;
        int outputs = 6;
        int middle_layer = 200;
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


        //Initialize subpopulation
        for(int i = 0; i<population; i++){
            Map.Entry<MultiLayerPerceptron, Double> entry = new AbstractMap.SimpleEntry<>(new MultiLayerPerceptron(mlpSize),0.0);
            mlpList.add(entry);
        }

        System.out.println("Initialized MLPs");
        for (Map.Entry entry: mlpList) {
            current_mlp = (MultiLayerPerceptron) entry.getKey();
            current_model = new Simulation(this);
            entry.setValue(current_model.getCost());
            System.out.println("Performed simulaiton with cost " + current_model.getCost());
        }
    }

    @Override
    public void pickAction(Agent a) {

        current_mlp.setInput(features.getZeroSet(current_model));
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

//TODO! Fix mvn with https://sourceforge.net/p/neuroph/discussion/862857/thread/94190255/
