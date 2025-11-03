package players.basicMCTS.groupADversion3;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IStateHeuristic;
import players.basicMCTS.BasicMCTSParams;
import players.basicMCTS.BasicMCTSPlayer;

import java.util.List;
import java.util.Random;

public class GroupADBasicMCTSPlayerversion3 extends BasicMCTSPlayer {

    private final Random localRnd;

    public GroupADBasicMCTSPlayerversion3() {
        this(System.currentTimeMillis());
    }

    public GroupADBasicMCTSPlayerversion3(long seed) {
        super(seed);
        getParameters().setRandomSeed(seed);
        localRnd = new Random(seed);
        initializeWinningParameters();
    }

    public GroupADBasicMCTSPlayerversion3(BasicMCTSParams params) {
        super(params);
        localRnd = new Random(params.getRandomSeed());
        initializeWinningParameters();
    }

    public GroupADBasicMCTSPlayerversion3(BasicMCTSParams params, IStateHeuristic heuristic) {
        super(params);
        params.heuristic = heuristic;
        localRnd = new Random(params.getRandomSeed());
        initializeWinningParameters();
    }

    private void initializeWinningParameters() {
        BasicMCTSParams p = getParameters();

        // Adjusted MCTS settings
        p.K = 1.3; // Increased exploration factor
        p.rolloutLength = 20; // Increased rollout length for more data in simulations
        p.maxTreeDepth = 10; // Reduced max tree depth to avoid excessive exploration
        p.epsilon = 1e-5; // Slightly higher epsilon to reduce noise

        if (p.budgetType == null) {
            p.budgetType = players.PlayerConstants.BUDGET_ITERATIONS;
            p.budget = 9000; // Increased budget for more iterations
        }

        if (p.heuristic == null) {
            p.heuristic = new SushiGoHeuristicversion3(); // Use the improved heuristic
        }
    }

    public void setStateHeuristic(IStateHeuristic heuristic) {
        getParameters().heuristic = heuristic;
    }

    @Override
    public String toString() {
        return "GroupAD-Dominator-v3";
    }

    @Override
    public AbstractAction _getAction(AbstractGameState gameState, List<AbstractAction> actions) {
        if (actions == null || actions.isEmpty()) {
            return null;
        }

        if (actions.size() == 1) {
            return actions.get(0);
        }

        if (actions.size() <= 2) {
            return selectBestByHeuristic(gameState, actions);
        }

        BasicTreeNodeXversion3 root = new BasicTreeNodeXversion3(
                this,
                null,
                gameState,
                new Random(getParameters().getRandomSeed() + gameState.getTurnCounter())
        );

        root.mctsSearch();

        AbstractAction bestAction = root.bestAction();

        if (bestAction == null) {
            bestAction = selectBestByHeuristic(gameState, actions);
        }

        return bestAction;
    }

    private AbstractAction selectBestByHeuristic(AbstractGameState gameState, List<AbstractAction> actions) {
        AbstractAction best = null;
        double bestValue = -Double.MAX_VALUE;

        IStateHeuristic heuristic = getParameters().heuristic;
        if (heuristic == null) {
            return actions.get(0);
        }

        for (AbstractAction action : actions) {
            AbstractGameState nextState = gameState.copy();
            AbstractAction actionCopy = action.copy();
            actionCopy.execute(nextState);

            double value = heuristic.evaluateState(nextState, getPlayerID());

            if (value > bestValue) {
                bestValue = value;
                best = action;
            }
        }

        return best != null ? best : actions.get(0);
    }

    @Override
    public GroupADBasicMCTSPlayerversion3 copy() {
        BasicMCTSParams cp = (BasicMCTSParams) getParameters().copy();

        if (cp.heuristic != null) {
            return new GroupADBasicMCTSPlayerversion3(cp, cp.heuristic);
        } else {
            return new GroupADBasicMCTSPlayerversion3(cp);
        }
    }
}
