package Learning.DeepQ;

import Learning.Features;
import Learning.Fitness;
import Learning.RLController;
import Model.Agent;
import Model.Simulation;
import Navigation.OrthogonalSubGoals;
import Navigation.SubGoal;
import View.MainFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeepQLearner implements RLController, Serializable {
    protected int iter = 100;
    protected float explorationRate = 0.10f;
    protected float explorDiscount = explorationRate/iter;
    protected float gamma = 0.99f;
    protected float alpha = 0.1f;


    //parameters for neural network construction.
    private int sizeInput = 5;
    private int sizeOutput = 2;
    private int nrHidden; //TODO: create compatibility for dynamic number of hidden layers
    private int sizeHidden = 50;
    private int batchSize = 1;


    private int batchNr;
    private double inputBatch[][];
    private double outputBatch[][];

    private MLP mlp;
    private Random rand;
    private OrthogonalSubGoals subGoals;
    private Simulation model;

    //Variables needed for debugging:
    final static boolean use_gui = true;
    final static boolean debugging = true;
    private final static int timeActionShown = 10;
    private int showActionFor;

    private Agent backup;

    //Fields for functionality of navigation and fitness
    private String algorithm = "Bresenham";
    private Fitness fit;
    private Features f;
    private int lowestCost;

    private Map<String,Double> distMap = Stream.of(
            new AbstractMap.SimpleEntry<>("WW", 0.0),
            new AbstractMap.SimpleEntry<>("SW", 0.0),
            new AbstractMap.SimpleEntry<>("SS", 0.0),
            new AbstractMap.SimpleEntry<>("SE", 0.0),
            new AbstractMap.SimpleEntry<>("EE", 0.0),
            new AbstractMap.SimpleEntry<>("NE", 0.0),
            new AbstractMap.SimpleEntry<>("NN", 0.0),
            new AbstractMap.SimpleEntry<>("NW", 0.0))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    private HashSet<String> directions = new HashSet<>(Arrays.asList("WW", "SW", "SS", "SE", "EE", "NE", "NN", "NW"));

    public DeepQLearner(){
        model = new Simulation(this, false);

//        if(use_gui){
//            new MainFrame(model);
//        }

        f = new Features();
        fit = new Fitness();
        lowestCost = Integer.MAX_VALUE;

        initNN();

        for (int i = 0; i< iter; i++){
            showActionFor = timeActionShown;
            trainMLP(i);
            if (explorationRate>0){
                explorationRate-=explorDiscount;
                //System.out.println("Updated exploration: " + exploration);
            }
        }

        //model.start();

    }

    private void trainMLP(int i){
        model = new Simulation(this, use_gui);

        JFrame frame;
        if(use_gui){
            frame = createMainFrame();

        }

        if (debugging){
            System.out.println("number of agents: " + model.getAgents().size() + ". X of first agent: " + model.getAgents().get(0).getX());
        }
        double[] oldState = getInputSet("WW");

        updateDistMap();
        double action = distMap.get("WW");

        double fireLocation[] = f.locationCenterFireAndMinMax(model);
        subGoals = new OrthogonalSubGoals((int)fireLocation[0],(int)fireLocation[1], distMap, algorithm, model.getAllCells());
        if (debugging) {
            System.out.println("Distance Map: " + Collections.singletonList(distMap));
            System.out.println("Length of feature vector:" + getInputSet("WW").length);
        }

        subGoals.selectClosestSubGoal(model.getAgents().get(0)); //Again, only works for single agent solution

        model.start();
        int cost = fit.totalFuelBurnt(model);

        if (model.getAgents().isEmpty()){ //TODO: Again, should really try to come up with better solution.
            model.getAgents().add(backup);
            if(debugging){
                System.out.println("Nr of Agents: " + model.getAgents().size());
            }
        }
        double[] newState = getInputSet("WW");
        if (cost<lowestCost){
            lowestCost = cost;
            System.out.println("In iteration: " + i + " a solution was found with cost: " + lowestCost);
            takeScreenShot();
        }

        train(oldState,newState,Math.toIntExact(Math.round(action)), cost);
        if (use_gui){
            disposeMainFrame(frame);
        }
    }

    private void train(double[] oldState, double[] newState, int action, int reward){

        double[] oldValue = getQ(oldState);

        System.out.println(Arrays.toString(oldState)+" -> " +Arrays.toString(oldValue));

        double[] newValue = getQ(newState); //TODO: Need to predict new state?
//        int actionInt = (action.equals("FORWARD")? 0 :1);

        oldValue[action] = reward + gamma* minValue(newValue);

        double[] trainInput = oldState;
        double[] trainOutput = oldValue;

        addToInputBatch(trainInput);
        addToOutputBatch(trainOutput);

        batchNr++;

        if (batchNr%batchSize==0){
            System.out.println("Updating MLP");
            batchNr = 0;
            mlp.updateMLP(inputBatch, outputBatch);
        }

        oldValue = getQ(oldState);
        System.out.println(Arrays.toString(oldState)+" -> "+Arrays.toString(oldValue));
    }

    private void updateDistMap(){
        for (String key : distMap.keySet()){
            Double oldValue = distMap.replace(key, getDistance(getInputSet(key)));
            if (oldValue == null){
                System.out.println("distMap value not updated successfully!! Selected key : " + key + ", complete keySet: " + Arrays.toString(distMap.keySet().toArray()));
            }
        }
    }

    @Override
    public void pickAction(Agent a) {
        if (a.onGoal()){
            subGoals.setNextGoal(a);
            //goalsHit++;//TODO: TRIGGER REWARD FUNCTION
        }
        String nextAction = a.subGoal.getNextAction();
        a.takeAction(nextAction);

        // TODO: This pice of code is ugly as hell, come up with better solution
        if (model.getAllCells().get(a.getX()).get(a.getY()).isBurning()){
            backup = a;
            if(debugging){
                System.out.println("Nr of Agents: " + model.getAgents().size());
            }
        }

        Fitness.SPE_Measure StraightPaths = fit.new SPE_Measure(model);
        if (use_gui){
            if (showActionFor>0) {
                sleep(showActionFor);
                showActionFor -=5;
            }
        }
        if (debugging) {
            //System.out.println("current SPE_fitness: " + StraightPaths.getFitness(5));
            System.out.println("Current fuelBurnt fitness: " + fit.totalFuelBurnt(model));
            System.out.println("Active cells: " + model.getActiveCells().size());
            if (model.getActiveCells().size()==0){
                model.stop("No more active cells");
            }
        }

    }

    private void initNN(){
        sizeInput = getInputSet("WW").length;
        sizeOutput = Math.min(model.getAllCells().size(),model.getAllCells().get(0).size())/2;


        rand = new Random();
        batchNr = 0;
        inputBatch = new double[batchSize][sizeInput];
        outputBatch = new double[batchSize][sizeOutput];

        mlp = new MLP(sizeInput, sizeHidden, sizeOutput, alpha, batchSize);
    }



    protected int greedyLocation(double[] state){
        double[] outputSet = getQ(state);
        double minOut = Double.MAX_VALUE;
        int actionIndex = -1;

        for (int i = 0; i<outputSet.length; i++){
            // if the current action has the same activation as the highest value, choose randomly between them
            if (outputSet[i]==minOut){
                actionIndex = (new Random().nextBoolean()?i:actionIndex);
            }
            else if (outputSet[i]<minOut){
                minOut=outputSet[i];
                actionIndex = i;
            }
        }
        return actionIndex;
    }

    protected int randomLocation(){
        return (int) rand.nextInt(sizeOutput);
    }

    private double[] getQ(double[] in){
        double input[][] = new double[1][in.length];
        for (int i = 0; i<in.length; i++){
            input[0][i] = in[i];
        }
        double activation[][] = mlp.getOutput(input);
        double output[]= new double[activation.length];
        for (int i = 0; i<activation.length; i++){
            output[i] = activation[i][0];
        }
        return output;
    }

    private double[][] getQ(double[][] input){
        return mlp.getOutput(input);
    }

    private void addToInputBatch(double in[]){
        for (int i = 0; i<in.length; i++){
            inputBatch[batchNr][i] = in[i];
        }
    }
    private void addToOutputBatch(double out[]){
        for (int i = 0; i<out.length; i++){
            outputBatch[batchNr][i] = out[i];
        }
    }

//    public void update(int oldState, int newState, String action, int reward){
//
//        train(getInputSet(oldState),getInputSet(newState), action, reward);
//
//        if (explorationRate>0){
//            explorationRate-=explorDiscount;
//            //System.out.println("Updated exploration: " + exploration);
//        }
//    }

    public double getDistance(double in[]){
        float randFloat = rand.nextFloat();
        if(debugging) {
            System.out.println("choosing greedy action: " + (randFloat > explorationRate) + " exploration: " + explorationRate + " randFloat: " + randFloat);
        }if (randFloat> explorationRate){
            return greedyLocation(in);
        } else {
            return randomLocation();
        }
    }

    /**
     * Transform state to input vector.
     * @param subGoal: expressed as an integer to allow for use of for loops.
     * @return
     */
//    private double[] getInputSet(int subGoal){
//        float windX = model.getParameters().get("Wind x");
//        float windY = model.getParameters().get("Wind y");
//        double[] set = f.appendArrays(f.cornerVectors(model, false), f.windRelativeToSubgoal(windX, windY, indexMap.get(subGoal)));
//        return set;
//    }

    private double[] getInputSet(String subGoal){
        float windX = model.getParameters().get("Wind x");
        float windY = model.getParameters().get("Wind y");
        double[] set = f.appendArrays(f.cornerVectors(model, false), f.windRelativeToSubgoal(windX, windY, subGoal));
        //double[] set = new double[]{f.windRelativeToSubgoal(windX, windY, subGoal)};
        return set;
    }


    public static double minValue(double[] numbers){
        double min = Double.MAX_VALUE;
        for(int i = 1; i < numbers.length;i++)
        {
            if(numbers[i] < min)
            {
                min = numbers[i];
            }
        }
        return min;
    }

    protected void takeScreenShot(){
        JFrame f = createMainFrame();
        screenshot(0, lowestCost);
        f.dispose();
    }

    protected JFrame createMainFrame(){
        JFrame f = new MainFrame(model);
        sleep(1000);
        return f;
    }

    protected void disposeMainFrame(JFrame f){
        sleep(500);
        f.dispose();
    }

    protected void sleep(int t){
        try {
            Thread.sleep(Math.abs(t));
        } catch (java.lang.InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    protected void screenshot(int generation, int i){
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        try {
            BufferedImage capture = new Robot().createScreenCapture(screenRect);
            ImageIO.write(capture, "bmp", new File("./screenshot_g"+ generation+"_i_"+i+".bmp"));

        }catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
