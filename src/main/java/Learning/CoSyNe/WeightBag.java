package Learning.CoSyNe;

import org.neuroph.core.Weight;

import java.util.*;

public class WeightBag {

    private List<WeightPair> weights;
    private List<WeightPair> sortedWeights;
    private WeightPair activeWeight;
    private float alpha;
    public WeightBag(int size, float alpha){
        this.alpha = alpha;
        weights = new ArrayList<>();
        sortedWeights = new ArrayList<>();
        for(int i = 0; i< size; i++) {
            weights.add(new WeightPair(new Random().nextGaussian(), alpha));
        }
    }

    public Weight randomWeight(){
        Random rng = new Random();
        //Collections.sort(weights);
        activeWeight = weights.get(rng.nextInt(weights.size()));
        //activeWeight = weights.get(Math.min((int) Math.round(Math.abs(rng.nextGaussian()) * weights.size()/4), weights.size()-1));
        return activeWeight.getWeight();
    }

    public void updateFitness(double f){
        if(activeWeight.no_trials){
            activeWeight.updateFitness(f);
            //activeWeight.addFitness(f);
            sortedWeights.add(activeWeight);
        }else {
            activeWeight.updateFitness(f);
            //activeWeight.addFitness(f);
        }
    }

    public void breed(int n_children){
        Collections.sort(sortedWeights);
        Random rng = new Random();
        for(int i = 0; i< n_children; i++){
            int p1 = rng.nextInt(sortedWeights.size() / 4);
            int p2 = rng.nextInt(sortedWeights.size() / 4);
            WeightPair w1 = sortedWeights.get(p1);
            WeightPair w2 = sortedWeights.get(p2);
            WeightPair child = crossPerm(w1, w2);
            WeightPair kill = sortedWeights.get(sortedWeights.size()-1);
            //System.out.println("Parent1 " + w1 + " parent2 " + w2 + " kill " + kill + " for " + child);
            sortedWeights.remove(kill);
            weights.remove(kill);
            weights.add(child);
        }
        //System.out.println("New = " + Arrays.toString(weights.toArray()));

    }

    private WeightPair crossPerm(WeightPair w1, WeightPair w2){
        Random rng = new Random();
        double weight = w1.getWeight().getValue() * w1.getFitness() + w2.getWeight().getValue() * w2.getFitness();
        weight /= w1.getFitness() + w2.getFitness();
        //weight = 1000;
        weight += rng.nextGaussian();
        //if(Math.random() < 1){
          //  weight = rng.nextGaussian();
        //}
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

        public void updateFitness(double f){
            if(no_trials){
                fitness = f;
                no_trials = false;
                return;
            }

            fitness = fitness + alpha * (f - fitness);
        }

        public void resetFitness(){
            fitness = 0;
            fitness_calls = 0;
        }

        public void addFitness(double f){
            fitness += f;
            fitness_calls++;
            no_trials = false;
        }

        public double getMeanFitness(){
            return fitness / fitness_calls;
        }

        public Weight getWeight(){
            return weight;
        }

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
