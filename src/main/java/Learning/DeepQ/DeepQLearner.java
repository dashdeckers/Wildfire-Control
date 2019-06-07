package Learning.DeepQ;

import Learning.Features;
import Learning.Fitness;
import Learning.RLController;
import Model.Agent;
import Model.Simulation;
import Navigation.OrthogonalSubGoals;
import Navigation.SubGoal;
import View.MainFrame;

import java.util.Arrays;
import java.util.Random;

public class DeepQLearner implements RLController {
    protected int iter = 5000;
    protected float explorationRate = 1.0f;
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
    private SubGoal subGoal;
    private Simulation model;

    //variables needed for debugging:
    final static boolean use_gui = true;
    private double dist[] = {4,4,4,4,4,4,4,4};
    private String algorithm = "Bresenham";
    private Fitness fitt;


    public DeepQLearner(){
        model = new Simulation(this, use_gui);

        if(use_gui){
            new MainFrame(model);
        }

        Features f = new Features();

        sizeInput = f.appendArrays(f.cornerVectors(model, false)).length;
        sizeOutput = Math.min(model.getAllCells().size(),model.getAllCells().get(0).size())/2;

        initNN();

        for (int i = 0; i<dist.length; i++){
            dist[i]= getDistance(f.appendArrays(f.cornerVectors(model, false)));

        }

        double fireLocation[] = f.locationCenterFireAndMinMax(model);
        subGoals = new OrthogonalSubGoals((int)fireLocation[0],(int)fireLocation[1], dist, algorithm, model.getAllCells());
        System.out.println("Distance Array: " + Arrays.toString(dist));
        System.out.println("Length of feature vector:" + Arrays.toString(f.appendArrays(f.cornerVectors(model,false))));

        fitt = new Fitness();


        subGoals.selectClosestSubGoal(model.getAgents().get(0)); //Again, only works for single agent solution
        //model.start();

    }

    @Override
    public void pickAction(Agent a) {
        while(a.isAlive()&&a.getEnergyLevel()>0){
            if (a.onGoal()){
                subGoals.setNextGoal(a);
                //goalsHit++;//TODO: TRIGGER REWARD FUNCTION
            }
            String nextAction = a.subGoal.getNextAction();
            a.takeAction(nextAction);
            model.applyUpdates();
        }
        Fitness.SPE_Measure StraightPaths = fitt.new SPE_Measure(model);
        System.out.println("current fitness: " + StraightPaths.getFitness(2));

    }

    private void initNN(){
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

    private void train(double[] oldState, double[] newState, String action, int reward){

        double[] oldValue = getQ(oldState);

        System.out.println(Arrays.toString(oldState)+" -> " +Arrays.toString(oldValue));

        double[] newValue = getQ(newState); //TODO: Need to predict new state?
        int actionInt = (action.equals("FORWARD")? 0 :1);

        oldValue[actionInt] = reward + gamma*maxValue(newValue);

        double[] trainInput = oldState;
        double[] trainOutput = oldValue;

        addToInputBatch(trainInput);
        addToOutputBatch(trainOutput);

        batchNr++;

        if (batchNr%batchSize==0){
            System.out.println("Updating MLP");
            mlp.updateMLP(inputBatch, outputBatch);
            batchNr = 0;
        }

        oldValue = getQ(oldState);
        System.out.println(Arrays.toString(oldState)+" -> "+Arrays.toString(oldValue));
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

    public void update(int oldState, int newState, String action, int reward){

        train(getInputSet(oldState),getInputSet(newState), action, reward);

        if (explorationRate>0){
            explorationRate-=explorDiscount;
            //System.out.println("Updated exploration: " + exploration);
        }
    }

    public int getDistance(double in[]){
        float randFloat = rand.nextFloat();
        System.out.println("choosing greedy action: " + (randFloat>explorationRate) + " exploration: " + explorationRate + " randFloat: " + randFloat);
        if (randFloat> explorationRate){
            return greedyLocation(in);
        } else {
            return randomLocation();
        }
    }

    /**
     * transform state to array of binary inputs.
     * @param state
     * @return
     */
    private double[] getInputSet(int state){
        double[] set = new double[sizeInput];
        for (int i = 0; i<sizeInput; i++){
            if (i==state){
                set[i]=1;
            } else {
                set[i]=0;
            }
        }
        return set;
    }


    public static double maxValue(double[] numbers){
        double max = Double.MIN_VALUE;
        for(int i = 1; i < numbers.length;i++)
        {
            if(numbers[i] > max)
            {
                max = numbers[i];
            }
        }
        return max;
    }
}
