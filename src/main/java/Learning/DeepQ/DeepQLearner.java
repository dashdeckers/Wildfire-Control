package Learning.DeepQ;

import Learning.Features;
import Learning.Fitness;
import Learning.RLController;
import Model.Agent;
import Model.Simulation;
import Navigation.OrthogonalSubGoals;
import View.MainFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

public class DeepQLearner implements RLController, Serializable {
    protected int iter = 100;
    protected float explorationRate = 0.10f;
    protected float exploreDiscount = explorationRate/iter;
    protected float gamma = 0.99f;
    protected float alpha = 0.1f;


    //parameters for neural network construction.
    private int sizeInput;
    private int sizeOutput;
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
    private String algorithm = "Dijkstra";
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
        f = new Features();
        fit = new Fitness();
        lowestCost = Integer.MAX_VALUE;

        initNN();

        for (int i = 0; i< iter; i++){
            showActionFor = timeActionShown;
            trainMLP(i);
            if (explorationRate>0){
                explorationRate-= exploreDiscount;
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
            System.out.println("number of agents: " + model.getAgents().size() + ". XY of first agent: " + model.getAgents().get(0).getX() + model.getAgents().get(0).getY());
        }

        double fireLocation[] = f.locationCenterFireAndMinMax(model);
        subGoals = new OrthogonalSubGoals((int)fireLocation[0],(int)fireLocation[1], distMap, algorithm, model.getAllCells());
        updateDistMap();
        if (debugging&&use_gui){
            model.applyUpdates();
            sleep(500);
        }
        double[] oldState = getInputSet("WW");
        double action = distMap.get("WW");
        if (debugging) {
            System.out.println("Distance Map: " + Collections.singletonList(distMap));
            System.out.println("Length of feature vector:" + getInputSet("WW").length);
        }

        for (Agent a:model.getAgents()){
            if (debugging){
                System.out.println("assigning goal to agent: " + a.getId());
            }
            subGoals.selectClosestSubGoal(a);
        }
        //subGoals.selectClosestSubGoal(model.getAgents().get(0)); //TODO: Again, only works for single agent solution

        model.start();
        int cost = getCost();

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
            setDistance(getInputSet(key), key);
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

        // TODO: This piece of code is ugly as hell, come up with better solution
        if (model.getAllCells().get(a.getX()).get(a.getY()).isBurning()){
            backup = a;
            if(debugging){
                System.out.println("Nr of Agents: " + model.getAgents().size());
            }
        }

        if (use_gui){
            if (showActionFor>0) {
                sleep(showActionFor);
                showActionFor -=0;
            }
        }

        // TODO: Check if other RL techniques have similar kill command
        if (model.getActiveCells().size()==0){
            model.stop("No more active cells");
        }

    }

    private void initNN(){
        model = new Simulation(this);

        double[] fire=f.locationCenterFireAndMinMax(model);
        int minY=(int)Math.min(fire[1], (model.getAllCells().get(0).size()-fire[1]));
        int minX=(int)Math.min(fire[0], (model.getAllCells().size()-fire[0]));
        sizeOutput = Math.min(minX,minY);
        sizeInput = getInputSet("WW").length;


        rand = new Random();
        batchNr = 0;
        inputBatch = new double[batchSize][sizeInput];
        outputBatch = new double[batchSize][sizeOutput];

        mlp = new MLP(sizeInput, sizeHidden, sizeOutput, alpha, batchSize);
    }



    protected List<IndexActLink> greedyLocation(double[] state){
        double[] outputSet = getQ(state);
        double minOut = Double.MAX_VALUE;
        int actionIndex = -1;
        List<IndexActLink> outputList = new LinkedList<>();

        for (int i = 0; i<outputSet.length; i++){
            outputList.add(new IndexActLink(i, outputSet[i]));
        }

        outputList.sort(Comparator.comparing(IndexActLink::getActivation, Comparator.nullsLast(Comparator.naturalOrder())));

//        for (int i = 0; i<outputSet.length; i++){
//            // if the current action has the same activation as the highest value, choose randomly between them
//            if (outputSet[i]==minOut){
//                actionIndex = (new Random().nextBoolean()?i:actionIndex);
//            }
//            else if (outputSet[i]<minOut){
//                minOut=outputSet[i];
//                actionIndex = i;
//            }
//        }
        return outputList;
    }

    protected int randomLocation(){
        return rand.nextInt(sizeOutput);
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
//            explorationRate-=exploreDiscount;
//            //System.out.println("Updated exploration: " + exploration);
//        }
//    }

    public void setDistance(double in[], String key) {
        float randFloat = rand.nextFloat();
        int i = 0;
//        if(debugging) {
//            System.out.println("choosing greedy action: " + (randFloat > explorationRate) + " exploration: " + explorationRate + " randFloat: " + randFloat);
//        }
        if (randFloat > explorationRate) {
            List<IndexActLink> activationList = greedyLocation(in);
            do {
//                if (debugging){
//                    System.out.println("Currently looking at goal #" + i + ", with activation: " + activationList.get(i).activation+ " and index: " + activationList.get(i).index);
//                }
                subGoals.updateSubGoal(key, activationList.get(i).index);
                i++;
            } while (!subGoals.checkSubGoal(key, model.getAgents().get(0))); //TODO: Again, only possible in single agent environment.
        } else {
            do {
                subGoals.updateSubGoal(key, randomLocation());
            } while (!subGoals.checkSubGoal(key, model.getAgents().get(0))); //TODO: Again, only possible in single agent environment.
        }
        if (use_gui && debugging) {
            subGoals.paintGoal(key);
        }
    }

    private int getCost(){
        int cost=0;
        cost+=fit.totalFuelBurnt(model);
        cost+=fit.totalMoveCost(model);
        if (debugging){
            System.out.println("Total fuel burnt: " + fit.totalFuelBurnt(model) + ", Total moveCost: " + fit.totalMoveCost(model) + ", Total cost: " + cost);
        }
        return cost;
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

    /**
     * Class needed to order a list containing the index of the distance and the activation of that distance.
     */
    private class IndexActLink{
        private int index;
        private double activation;

        public IndexActLink(int i, double a){
            index=i;
            activation=a;
        }

        public double getActivation() {
            return activation;
        }

        public int getIndex() {
            return index;
        }
    }
}
