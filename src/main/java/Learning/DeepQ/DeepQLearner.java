package Learning.DeepQ;

import Learning.RLController;
import Model.Agent;
import Navigation.OrthogonalSubgoals;
import Navigation.SubGoal;

import java.util.Arrays;
import java.util.Random;

public class DeepQLearner implements RLController {
    protected float exploration;
    protected float explorDiscount;
    protected float gamma;
    protected float alpha;


    //parameters for neural network construction.
    private int sizeInput = 5;
    private int sizeOutput = 2;
    private int nrHidden; //TODO: create compatibility for dynamic number of hidden layers
    private int sizeHidden = 10;
    private int batchSize = 1;


    private int batchNr;
    private double inputBatch[][];
    private double outputBatch[][];

    private MLP mlp;
    private Random rand;
    private OrthogonalSubgoals goal;


    public DeepQLearner(float alpha, float gamma, int iter, float exploration, int length){
        this.exploration = exploration;
        this.explorDiscount = exploration/iter;
        this.gamma = gamma;
        this.alpha = alpha;
        initNN();

    }

    @Override
    public void pickAction(Agent a) {
        if (!a.onGoal()){
            goal.setNextGoal(a);
            //goalsHit++;//TODO: TRIGGER REWARD FUNCTION
        }
    }

    private void initNN(){

        rand = new Random();
        batchNr = 0;
        inputBatch = new double[batchSize][sizeInput];
        outputBatch = new double[batchSize][sizeOutput];

        mlp = new MLP(sizeInput, sizeHidden, sizeOutput, alpha, batchSize);
    }



    protected String greedyAction(double[] state){
        double[] outputSet = getQ(state);
        double maxOut = -1.0;
        int actionIndex = -1;

        for (int i = 0; i<outputSet.length; i++){
            // if the current action has the same activation as the highest value, choose randomly between them
            if (outputSet[i]==maxOut){
                actionIndex = (new Random().nextBoolean()?i:actionIndex);
            }
            else if (outputSet[i]>maxOut){
                maxOut=outputSet[i];
                actionIndex = i;
            }
        }
        switch (actionIndex){
            case 0:
                return "FORWARD";
            case 1:
                return "BACKWARD";
            default:
                System.out.println("No suitable action found, choosing at random");
                return (new Random().nextBoolean()? "FORWARD":"BACKWARD");
        }
    }

    protected String randomAction(){
        return (new Random().nextBoolean()? "FORWARD" : "BACKWARD");
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

        double[] newValue = getQ(newState);
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

        if (exploration>0){
            exploration-=explorDiscount;
            //System.out.println("Updated exploration: " + exploration);
        }
    }

    public String getNextAction(int state){
        float randFloat = rand.nextFloat();
        System.out.println("choosing greedy action: " + (randFloat>exploration) + " exploration: " + exploration + " randFloat: " + randFloat);
        if (randFloat> exploration){
            return greedyAction(getInputSet(state));
        } else {
            return randomAction();
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
