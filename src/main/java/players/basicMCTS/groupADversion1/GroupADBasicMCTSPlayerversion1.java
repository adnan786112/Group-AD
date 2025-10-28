package players.basicMCTS.groupADversion1;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IStateHeuristic;
import players.basicMCTS.BasicMCTSParams;
import players.basicMCTS.BasicMCTSPlayer;

import java.util.List;
import java.util.Random;

public class GroupADBasicMCTSPlayerversion1 extends BasicMCTSPlayer {

    private final Random localRnd;

    public GroupADBasicMCTSPlayerversion1() {
        this(System.currentTimeMillis());
    }

    public GroupADBasicMCTSPlayerversion1(long seed) {
        super(seed);
        getParameters().setRandomSeed(seed);
        localRnd = new Random(seed);
        BasicMCTSParams p = getParameters();
        p.K = Math.sqrt(2);
        p.rolloutLength = 10;
        p.maxTreeDepth = 6;
        p.epsilon = 1e-6;
    }

    public GroupADBasicMCTSPlayerversion1(BasicMCTSParams params) {
        super(params);
        localRnd = new Random(params.getRandomSeed());
    }

    public GroupADBasicMCTSPlayerversion1(BasicMCTSParams params, IStateHeuristic heuristic) {
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
        BasicTreeNodeXversion1 root = new BasicTreeNodeXversion1(this, null, gameState, new Random(getParameters().getRandomSeed()));
        root.mctsSearch();
        return root.bestAction();
    }

    @Override
    public GroupADBasicMCTSPlayerversion1 copy() {
        BasicMCTSParams cp = (BasicMCTSParams) getParameters().copy();
        if (cp.heuristic == null) return new GroupADBasicMCTSPlayerversion1(cp);
        return new GroupADBasicMCTSPlayerversion1(cp, cp.heuristic);
    }

    @Override
    public BasicMCTSParams getParameters() {
        return (BasicMCTSParams) super.getParameters();
    }
}