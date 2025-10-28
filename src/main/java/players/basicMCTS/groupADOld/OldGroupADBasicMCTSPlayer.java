package players.basicMCTS.groupADOld;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IStateHeuristic;
import players.basicMCTS.BasicMCTSParams;
import players.basicMCTS.BasicMCTSPlayer;

import java.util.Random;

public class OldGroupADBasicMCTSPlayer extends BasicMCTSPlayer {

    private final Random localRnd;

    public OldGroupADBasicMCTSPlayer() {
        this(System.currentTimeMillis());
    }

    public OldGroupADBasicMCTSPlayer(long seed) {
        super(seed);
        getParameters().setRandomSeed(seed);
        localRnd = new Random(seed);
        BasicMCTSParams p = getParameters();
        p.K = Math.sqrt(2);
        p.rolloutLength = 12;
        p.maxTreeDepth = 7;
        p.epsilon = 1e-6;
    }

    public OldGroupADBasicMCTSPlayer(BasicMCTSParams params) {
        super(params);
        localRnd = new Random(params.getRandomSeed());
    }

    public OldGroupADBasicMCTSPlayer(BasicMCTSParams params, IStateHeuristic heuristic) {
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
        BasicTreeNodeX root = new BasicTreeNodeX(this, null, gameState, new Random(getParameters().getRandomSeed()));
        root.mctsSearch();
        return root.bestAction();
    }

    @Override
    public OldGroupADBasicMCTSPlayer copy() {
        BasicMCTSParams cp = (BasicMCTSParams) getParameters().copy();
        if (cp.heuristic == null) return new OldGroupADBasicMCTSPlayer(cp);
        return new OldGroupADBasicMCTSPlayer(cp, cp.heuristic);
    }

    @Override
    public BasicMCTSParams getParameters() {
        return (BasicMCTSParams) super.getParameters();
    }
}
