package Learning;

import Model.Simulation;

public class OffsetFeatures {


    public Simulation model;
    public double degree;

    public OffsetFeatures(Simulation model){
        this.model = model;
    }

    /**
     * Tell the features what the degree of this subgoal is (except we normalize it, so the degrees are divide by 360)
     * @param degree
     */
    public void setDegree(double degree){
        this.degree = degree;
    }

    /**
     * For now we simply return the angle as a feature, but that might be changed to something more meaningful for less trivial situations
     * @return
     */
    public double[] getResult(){
        double[] out = new double[1];
        out[0] = degree;
        return out;
    }
}
