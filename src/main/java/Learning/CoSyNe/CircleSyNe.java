package Learning.CoSyNe;

import Learning.Features;
import Learning.Fitness;
import Model.Agent;

import java.io.Serializable;
import java.util.Arrays;

public class CircleSyNe extends CoSyNe implements Serializable {

    private Features features;

    public CircleSyNe(){
        super();
        performLearning();
    }

    @Override
    protected void testMLP() {
        super.testMLP();
        features.previousAction = -1;
    }

    @Override
    void performAction(int action, Agent a) {
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
                System.out.println("NO ACTION FOR OUTPUT at GA.pickAction");
        }
    }

    @Override
    int defN_generations() {
        return 200;
    }

    @Override
    int[] defHiddenLayers() {
        int[] hl = {20};
        return hl;
    }

    @Override
    int defN_outputs() {
        return 6;
    }

    @Override
    int defBagSize() {
        return 50;
    }

    @Override
    int defGenerationSize() {
        return defBagSize()*10;
    }

    @Override
    float defAlpha() {
        return 0.05f;
    }

    @Override
    int defN_children() {
        return 30;
    }

    @Override
    double[] getInput() {
        if(features == null){
            features = new Features();
        }
        return features.appendArrays(features.previousAction(), features.cornerVectors(model, false));
    }

    @Override
    double getFitness() {
        Fitness fit = new Fitness();

        Fitness.SPE_Measure StraightPaths = fit.new SPE_Measure(model);

        return StraightPaths.getFitness(2);
    }

    @Override
    protected int defWeightSpread(){
        return 3;
    }
}
