package players.basicMCTS.groupADversion1;


import core.AbstractGameState;
import core.actions.AbstractAction;
import players.basicMCTS.BasicMCTSParams;
import players.basicMCTS.BasicTreeNode;

import java.util.List;
import java.util.Random;

public class BasicTreeNodeXversion1 extends BasicTreeNode {

    private final BasicMCTSParams params;
    private final Random rnd;

    public BasicTreeNodeXversion1(GroupADBasicMCTSPlayerversion1 player, BasicTreeNodeXversion1 parent, AbstractGameState state, Random rnd) {
        super(player, parent, state, rnd);
        this.params = player.getParameters();
        this.rnd = rnd;
    }

    protected AbstractAction rolloutPolicy(AbstractGameState gs, List<AbstractAction> acts, int playerId) {
        if (params.heuristic == null || acts.size() <= 1) return acts.get(rnd.nextInt(acts.size()));
        double epsilon = 0.15;
        if (rnd.nextDouble() < epsilon) return acts.get(rnd.nextInt(acts.size()));
        AbstractAction best = null;
        double bestV = -1e9;
        for (AbstractAction a : acts) {
            AbstractGameState copy = gs.copy();
            a.execute(copy);
            double v = params.heuristic.evaluateState(copy, playerId);
            if (v > bestV) {
                bestV = v;
                best = a;
            }
        }
        return best;
    }

    protected double priorFor(AbstractGameState state, AbstractAction action, int playerId) {
        if (params.heuristic == null) return 0.0;
        AbstractGameState gs = state.copy();
        action.execute(gs);
        double v = params.heuristic.evaluateState(gs, playerId);
        return v;
    }

    protected double lambdaPrior() {
        return 0.05;
    }
}
