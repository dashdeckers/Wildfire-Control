package Learning;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldTerminalFunction;
import burlap.domain.singleagent.gridworld.GridWorldVisualizer;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.statehashing.simple.SimpleHashableStateFactory;

import java.util.ArrayList;
import java.util.List;

public class BURLAP {

    /**
     *  This is example code from the BURLAP library, just to have something working. This needs a lot
     *  of fitting before it is compatible with our domain/model.
     */
    public void example() {
        //define the problem
        GridWorldDomain gwd = new GridWorldDomain(11, 11);
        gwd.setMapToFourRooms();
        gwd.setTf(new GridWorldTerminalFunction(10, 10));
        SADomain domain = gwd.generateDomain();
        Environment env = new SimulatedEnvironment(domain, new GridWorldState(0, 0));

        //create a Q-learning agent
        QLearning agent = new QLearning(domain, 0.99, new SimpleHashableStateFactory(), 1.0, 1.0);

        //run 100 learning episode and save the episode results
        List<Episode> episodes = new ArrayList<>();
        for(int i = 0; i < 100; i++){
            episodes.add(agent.runLearningEpisode(env));
            env.resetEnvironment();
        }

        //visualize the completed learning episodes
        new EpisodeSequenceVisualizer(GridWorldVisualizer.getVisualizer(gwd.getMap()), domain, episodes);
    }
}
