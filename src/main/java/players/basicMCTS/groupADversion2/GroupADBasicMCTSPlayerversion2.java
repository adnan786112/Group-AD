package players.basicMCTS.groupADversion2;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IStateHeuristic;
import players.basicMCTS.BasicMCTSParams;
import players.basicMCTS.BasicMCTSPlayer;

import java.util.List;
import java.util.Random;

public class GroupADBasicMCTSPlayerversion2 extends BasicMCTSPlayer {

    private final Random localRnd;

    public GroupADBasicMCTSPlayerversion2() {
        this(System.currentTimeMillis());
    }

    public GroupADBasicMCTSPlayerversion2(long seed) {
        super(seed);
        getParameters().setRandomSeed(seed);
        localRnd = new Random(seed);
        BasicMCTSParams p = getParameters();
        p.K = Math.sqrt(2);
        p.rolloutLength = 12;
        p.maxTreeDepth = 7;
        p.epsilon = 1e-6;
    }

    public GroupADBasicMCTSPlayerversion2(BasicMCTSParams params) {
        super(params);
        localRnd = new Random(params.getRandomSeed());
    }

    public GroupADBasicMCTSPlayerversion2(BasicMCTSParams params, IStateHeuristic heuristic) {
        super(params);
        params.heuristic = heuristic;
        localRnd = new Random(params.getRandomSeed());
    }

    public void setStateHeuristic(IStateHeuristic heuristic) {
        getParameters().heuristic = heuristic;
    }

    @Override
    public String toString() {
        return "GroupAD-BasicMCTS(Heuristic)";
    }

    @Override
    public AbstractAction _getAction(AbstractGameState gameState, List<AbstractAction> actions) {
        BasicTreeNodeXversion2 root = new BasicTreeNodeXversion2(this, null, gameState, new Random(getParameters().getRandomSeed()));
        root.mctsSearch();
        return root.bestAction();
    }

    @Override
    public GroupADBasicMCTSPlayerversion2 copy() {
        BasicMCTSParams cp = (BasicMCTSParams) getParameters().copy();
        if (cp.heuristic == null) return new GroupADBasicMCTSPlayerversion2(cp);
        return new GroupADBasicMCTSPlayerversion2(cp, cp.heuristic);
    }

    @Override
    public BasicMCTSParams getParameters() {
        return (BasicMCTSParams) super.getParameters();
    }
}
