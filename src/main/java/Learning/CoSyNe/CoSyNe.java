package Learning.CoSyNe;

import Learning.RLController;
import Model.Agent;
import Model.Simulation;
import View.MainFrame;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.util.TransferFunctionType;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

abstract class CoSyNe implements RLController {
    private List<Integer> MLP_shape;
    //layer, neuron, weight
    protected List<List<List<WeightBag>>> weightBags;
    protected MultiLayerPerceptron mlp;
    protected Simulation model;
    protected Double best_performance = null;
    protected Double ultimate_performance = null;
    protected double mean_perfomance;

    public CoSyNe(){
        MLP_shape = new ArrayList<>();
        model = new Simulation(this);
        MLP_shape.add(getInput().length);
        for(int i = 0; i < defHiddenLayers().length; i++){
            MLP_shape.add(defHiddenLayers()[i]);
        }
        MLP_shape.add(defN_outputs());
        model = new Simulation(this);

        initializeBags();

    }

    /**
     * The overall generation loop including creating MLPs, testing them, and breeding them
     */
    protected void performLearning(){
        for(int generation = 0; generation < defN_generations(); generation++){
            mean_perfomance = 0;
            for(int test = 0; test < defGenerationSize(); test++){
                createMLP();

                testMLP();
            }
            mean_perfomance /= defGenerationSize();
            System.out.println("Best performance: " + best_performance);
            System.out.println("Mean perforamcne: " + mean_perfomance);
            best_performance = null;
            breed();
        }
    }

    /**
     * Form an MLP by pulling random weights out of the weightbags.
     */
    private void createMLP(){
        mlp = new MultiLayerPerceptron(MLP_shape, TransferFunctionType.SIGMOID);
        for (int layer = 0; layer < mlp.getLayersCount(); layer ++) {
            for (int neuron = 0; neuron < mlp.getLayerAt(layer).getNeuronsCount(); neuron++) {
                for (int weight = 0; weight < mlp.getLayerAt(layer).getNeuronAt(neuron).getWeights().length; weight++) {
                    mlp.getLayerAt(layer).getNeuronAt(neuron).getInputConnections()[weight].setWeight(weightBags.get(layer).get(neuron).get(weight).randomWeight());
                }
            }
        }
    }

    /**
     * Subject the MLP to the simulation so we can establish its fitness.
     */
    protected void testMLP(){
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
            model = new Simulation(this);

            JFrame f = new MainFrame(model);
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

    /**
     * Takes a screenshot. Generation and i are only used to define the name of the file.
     * @param generation
     * @param i
     */
    protected void screenshot(int generation, int i){
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        try {
            BufferedImage capture = new Robot().createScreenCapture(screenRect);
            ImageIO.write(capture, "bmp", new File("./screenshot_g"+ generation+"_i_"+i+".bmp"));

        }catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Tell all bags to breed
     */
    private void breed(){
        for(int layer = 0; layer < weightBags.size(); layer++){
            for(int neuron = 0; neuron < weightBags.get(layer).size(); neuron++) {
                for (int weight = 0; weight < weightBags.get(layer).get(neuron).size(); weight++) {
                    WeightBag bag = weightBags.get(layer).get(neuron).get(weight);
                    bag.breed(defN_children());
                }
            }
        }
    }

    /**
     * Creates all the weight bags.
     */
    private void initializeBags(){
        weightBags = new ArrayList<>();
        mlp = new MultiLayerPerceptron(MLP_shape);
        int bagSize = defBagSize();
        for (int layer = 0; layer < mlp.getLayersCount(); layer ++) {
            weightBags.add(new ArrayList<>());
            for(int neuron = 0; neuron < mlp.getLayerAt(layer).getNeuronsCount(); neuron++){
                weightBags.get(layer).add(new ArrayList<>());
                for(int weight = 0; weight < mlp.getLayerAt(layer).getNeuronAt(neuron).getWeights().length; weight++){
                    weightBags.get(layer).get(neuron).add(new WeightBag(bagSize, defAlpha()));
                }
            }
        }
    }

    /**
     * Extracts an action integer from the MLPs output.
     * @param a
     */
    @Override
    public void pickAction(Agent a) {
        mlp.setInput(getInput());
        mlp.calculate();
        double[] outputs = mlp.getOutput();
        double max_out= 0.0;
        int action = -1;
        //We simply apply the maximum action, since we already have a doNothing action
        for(int i = 0; i<outputs.length; i++){
            if(action == -1 || outputs[i] > max_out){
                max_out = outputs[i];
                action = i;
            }
        }

        performAction(action, a);
    }

    /**
     * Define how an action i should be performed
     * @param action
     */
    abstract void performAction(int action, Agent a);

    /**
     * Define the number of generations the CoSyNe needs to learn for
     * @return the number of generations wanted
     */
    abstract int defN_generations();

    /**
     * Define the shape of hidden layer(s).
     * @return An array of ints representing the number of neurons in each hidden layer
     */
    abstract int[] defHiddenLayers();

    /**
     * Define the number of outputs the MLP should be able to produce. Make sure this corresponds with pickAction
     * @return The number of possible actions the MLP should be able to take
     */
    abstract int defN_outputs();

    /**
     * Specify the number of weights in each bag
     * @return Number of weights in each bag
     */
    abstract int defBagSize();

    /**
     * Specify the number of MLPs which should be created&tested in each generation.
     * A larger value will more accuractely assess the performance of each weight in a generation, but take more time
     * A value of 1-5x bagsize seems reasonable
     * @return Number of MLPs created in each generation
     */
    abstract int defGenerationSize();

    /**
     * Specify the learning rate at which the fitness of a weight is evaluated. This mimics the discount rate discussed in the Reinforcement Learning book.
     * Alpha at 1 will make the performance of a weight based only on the most recent trial.
     * Alpha at 0 will make the performance of a weight based only on the first trial.
     * Alpha at 0.05 is common, where the last 20 trials mostly determine the performance.
     * @return
     */
    abstract float defAlpha();

    /**
     * Specify the number of children which should be generated on each generation
     * @return
     */
    abstract int defN_children();

    /**
     * Get the inputs to the MLP from a model
     * @return A list of doubles extracted as meaningful features from the model
     */
    abstract double[] getInput();

    /**
     * Get the performance the MLP delivered from the model
     * @return A double representing the model's performance
     */
    abstract double getFitness();

}
