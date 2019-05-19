package Learning.CoSyNe;

import org.neuroph.core.Weight;

import java.util.*;

public class WeightBag {

    private List<WeightPair> weights;
    private List<WeightPair> sortedWeights;
    private WeightPair activeWeight;
    private float alpha;
    private int weightSpread;
    public WeightBag(int size, float alpha, int weightSpread){
        this.alpha = alpha;
        this.weightSpread = weightSpread;
        weights = new ArrayList<>();
        sortedWeights = new ArrayList<>();
        for(int i = 0; i< size; i++) {
            weights.add(new WeightPair( (new Random().nextDouble()*2 -1) *weightSpread , alpha));    //Initialized between 2, -2
        }
    }

    /**
     * Pull a random weight from the bag
     * @return
     */
    public Weight randomWeight(){
        Random rng = new Random();
        activeWeight = weights.get(rng.nextInt(weights.size()));
        return activeWeight.getWeight();
    }

    /**
     * Update the fitness of the current weight
     * @param f
     */
    public void updateFitness(double f){
        if(activeWeight.no_trials){
            activeWeight.updateFitness(f);
            sortedWeights.add(activeWeight);
        }else {
            activeWeight.updateFitness(f);
        }
    }

    /**
     * Perform the breeding procedure on all weights in the bags, all fitnessess should be defined by this point
     *
     * @param n_children How many children should be spawned, keep this below half the bagSize
     */
    public void breed(int n_children){
        Collections.sort(sortedWeights);

        Random rng = new Random();
        for(int i = 0; i< n_children; i++){
            int p1 = rng.nextInt(sortedWeights.size() / 4); //fetch parents from 25th percentile
            int p2 = rng.nextInt(sortedWeights.size() / 4);
            WeightPair w1 = sortedWeights.get(p1);
            WeightPair w2 = sortedWeights.get(p2);
            WeightPair child = crossPerm(w1, w2);
            WeightPair kill = sortedWeights.get(sortedWeights.size()-1);    //Kill the worst
            sortedWeights.remove(kill);
            weights.remove(kill);
            weights.add(child);
        }
    }

    /**
     * Create a child based on it's two parents
     * @param w1
     * @param w2
     * @return  A weightpair child at the mean of the parents + 1 random standard deviation
     */
    private WeightPair crossPerm(WeightPair w1, WeightPair w2){
        Random rng = new Random();
        double weight;
        if(rng.nextBoolean()){
            weight = w1.getWeight().getValue();
        }else{
            weight = w2.getWeight().getValue();
        }
        if(rng.nextFloat() < 0.05){
            weight = (rng.nextDouble() * 2 - 1) * weightSpread;
        }
        return new WeightPair(weight, alpha);
    }

    private class WeightPair implements Comparable{
        private Weight weight;
        private double fitness;
        private int fitness_calls = 0;
        private float alpha;
        public boolean no_trials;
        public WeightPair(double w, float alpha){
            weight = new Weight(w);
            this.alpha = alpha;
            no_trials = true;
        }

        public double getFitness() {
            return fitness;
        }

        /**
         * Add a value to the simulated mean, which encourages recency over real mean
         * @param f
         */
        public void updateFitness(double f){
            if(no_trials){
                fitness = f;
                no_trials = false;
                return;
            }

            fitness = fitness + alpha * (f - fitness);
        }

        /**
         * Set the fitness back to 0 for both mean and
         */
        public void resetFitness(){
            fitness = 0;
            fitness_calls = 0;
        }

        /**
         * Use this when using the accurate mean
         * @param f
         */
        public void addFitness(double f){
            fitness += f;
            fitness_calls++;
            no_trials = false;
        }

        /**
         * Use this when using the accurate mean
         * @return
         */
        public double getMeanFitness(){
            return fitness / fitness_calls;
        }

        public Weight getWeight(){
            return weight;
        }

        /**
         * Compare function which is used for sorting
         * @param c
         * @return
         */
        @Override
        public int compareTo(Object c) {
            Double tf = fitness;
            return tf.compareTo(((WeightPair) c).getFitness());
        }

        public String toString(){
            return "(" + Double.toString(weight.value) + "," + Double.toString(getFitness()) + ")";
        }
    }
}
