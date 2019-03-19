package Learning.Burlap;

import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QProvider;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QLearner extends MDPSolver implements LearningAgent, QProvider {
	Map<HashableState, List<QValue>> qValues;
	QFunction qinit;
	double learningRate;
	Policy learningPolicy;

	public QLearner(SADomain domain, double gamma, HashableStateFactory hashingFactory,
					QFunction qinit, double learningRate, double epsilon) {
		this.solverInit(domain, gamma, hashingFactory);
		this.qinit = qinit;
		this.learningRate = learningRate;
		this.qValues = new HashMap<HashableState, List<QValue>>();
		this.learningPolicy = new EpsilonGreedy(this, epsilon);
	}

	/**
	 * Runs the episode without a max steps parameters (until terminal State
	 * is reached)
	 * @param env
	 * @return
	 */
	@Override
	public Episode runLearningEpisode(Environment env) {
		return this.runLearningEpisode(env, -1);
	}

	/**
	 * Runs the episode until a terminal State is reached, or until the maximum
	 * number of steps is reached.
	 * @param env
	 * @param maxSteps
	 * @return
	 */
	@Override
	public Episode runLearningEpisode(Environment env, int maxSteps) {
		// Record the States, Actions and Rewards in this Episode.
		// Initialize with initial state of environment
		Episode e = new Episode(env.currentObservation());

		// Act until a terminal State, or max steps is reached
		State currentState = env.currentObservation();
		int steps = 0;
		while(!env.isInTerminalState() && (steps < maxSteps || maxSteps == -1)) {
			// Select an action
			Action a = this.learningPolicy.action(currentState);

			// Take the action and observe the outcome
			EnvironmentOutcome outcome = env.executeAction(a);

			// Record the outcome
			e.transition(outcome);

			// Get the max Q-Value of the resulting State, 0 if it is terminal
			double maxQ = outcome.terminated ? 0. : this.value(outcome.op);

			// Update the old Q-Value according to Q-Learning formula
			QValue oldQ = this.storedQ(currentState, a);
			oldQ.q = oldQ.q + this.learningRate * (outcome.r + this.gamma * maxQ - oldQ.q);

			// Current State is now the next State
			currentState = outcome.op;
			steps++;
		}
		return e;
	}

	/**
	 * Resets the learned values.
	 */
	@Override
	public void resetSolver() {
		this.qValues.clear();
	}

	/**
	 * Return the Q-Values we have for the State s. This creates a new
	 * entry with qinit values if we don't already have one.
	 * @param s
	 * @return
	 */
	@Override
	public List<QValue> qValues(State s) {
		// Hash the state for compatibility with HashSets etc
		HashableState hashedState = this.hashingFactory.hashState(s);
		// Check if we already have an entry for this state
		List<QValue> qValuesForState = this.qValues.get(hashedState);
		if (qValuesForState == null) {
			// If we don't, make a new entry by creating an initial Q-value
			// for each possible action within the current state
			List<Action> actions = this.applicableActions(s);
			qValuesForState = new ArrayList<>(actions.size());
			for (Action a : actions) {
				qValuesForState.add(new QValue(s, a, this.qinit.qValue(s, a)));
			}
			// Put the new entry into our set/list
			this.qValues.put(hashedState, qValuesForState);
		}
		return qValuesForState;
	}

	/**
	 * Returns the specific Q-Value for State s and Action a
	 * @param s
	 * @param a
	 * @return
	 */
	@Override
	public double qValue(State s, Action a) {
		return storedQ(s, a).q;
	}

	/**
	 * Returns the Q-Values for State s, if it contains the Q-Value for Action a.
	 * It throws a runtime error if something goes wrong.
	 * @param s
	 * @return
	 */
	protected QValue storedQ(State s, Action a) {
		List<QValue> qValuesForState = this.qValues(s);
		// Return the Q-values for this state if it contains the Q-value for Action a
		for (QValue q : qValuesForState) {
			if (q.a.equals(a)) {
				return q;
			}
		}
		throw new RuntimeException("Couldn't find matching Q-Value.");
	}

	/**
	 * Returns the maximum Q-Value for State s
	 * @param s
	 * @return
	 */
	@Override
	public double value(State s) {
		return QProvider.Helper.maxQ(this, s);
	}
}
