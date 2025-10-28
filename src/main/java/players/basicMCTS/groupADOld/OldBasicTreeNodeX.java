package players.basicMCTS.groupADOld;

import core.AbstractGameState;
import core.actions.AbstractAction;
import players.basicMCTS.BasicMCTSParams;
import players.basicMCTS.BasicTreeNode;

import java.util.*;
import static utilities.Utils.noise;

public class OldBasicTreeNodeX extends BasicTreeNode {

    private final BasicMCTSParams params;
    private final Random rnd;

    public OldBasicTreeNodeX(OldGroupADBasicMCTSPlayer player, OldBasicTreeNodeX parent, AbstractGameState state, Random rnd) {
        super(player, parent, state, rnd);
        this.params = player.getParameters();
        this.rnd = rnd;
    }

    /**
     * Deterministic heuristic rollout (no random actions)
     */

    protected double rollOut() {
        AbstractGameState rolloutState = state.copy();
        int depth = 0;

        while (!finishRollout(rolloutState, depth)) {
            AbstractAction next = bestHeuristicAction(rolloutState);
            advance(rolloutState, next);
            depth++;
        }

        double value = params.getStateHeuristic().evaluateState(rolloutState, player.getPlayerID());
        return normalize(value);
    }

    /**
     * Picks the best action deterministically based on heuristic.
     */
    private AbstractAction bestHeuristicAction(AbstractGameState gs) {
        List<AbstractAction> actions = player.getForwardModel().computeAvailableActions(gs, params.actionSpace);
        if (actions.isEmpty()) return null;
        if (actions.size() == 1) return actions.get(0);

        AbstractAction best = null;
        double bestVal = -Double.MAX_VALUE;

        for (AbstractAction a : actions) {
            AbstractGameState next = gs.copy();
            a.execute(next);
            double val = params.getStateHeuristic().evaluateState(next, player.getPlayerID());
            if (val > bestVal) {
                bestVal = val;
                best = a;
            } else if (Math.abs(val - bestVal) < params.epsilon && best != null) {
                // Deterministic tiebreaker
                if (a.toString().compareTo(best.toString()) < 0) best = a;
            }
        }
        return best;
    }

    /**
     * Adds heuristic-guided bias to UCB.
     */

    protected AbstractAction ucb() {
        AbstractAction bestAction = null;
        double bestValue = -Double.MAX_VALUE;

        for (AbstractAction action : children.keySet()) {
            BasicTreeNode child = children.get(action);
            if (child == null) continue;

            double exploit = child.totValue / (child.nVisits + params.epsilon);
            double explore = params.K * Math.sqrt(Math.log(this.nVisits + 1) / (child.nVisits + params.epsilon));
            double bias = heuristicBias(action);

            // 0.3 exploration => more exploitation (deterministic)
            double uctValue = exploit + 0.3 * explore + bias;
            uctValue = noise(uctValue, params.epsilon, 0.0); // stable small noise

            if (uctValue > bestValue ||
                    (Math.abs(uctValue - bestValue) < params.epsilon &&
                            (bestAction == null || action.toString().compareTo(bestAction.toString()) < 0))) {
                bestValue = uctValue;
                bestAction = action;
            }
        }
        return bestAction;
    }

    /**
     * Bias based on heuristic evaluation of child state.
     */
    private double heuristicBias(AbstractAction action) {
        AbstractGameState copy = state.copy();
        action.execute(copy);
        return 0.15 * params.getStateHeuristic().evaluateState(copy, player.getPlayerID());
    }

    /**
     * Normalizes heuristic values to [-1,1].
     */
    private double normalize(double v) {
        if (Double.isNaN(v)) return 0;
        return Math.max(-1, Math.min(1, v));
    }
}
