package players.basicMCTS.groupADversion4;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IStateHeuristic;
import players.basicMCTS.BasicMCTSParams;
import players.basicMCTS.BasicMCTSPlayer;

import java.util.List;
import java.util.Random;

public class GroupADBasicMCTSPlayerversion4 extends BasicMCTSPlayer {

    private final Random localRnd;

    public GroupADBasicMCTSPlayerversion4() {
        this(System.currentTimeMillis());
    }

    public GroupADBasicMCTSPlayerversion4(long seed) {
        super(seed);
        getParameters().setRandomSeed(seed);
        localRnd = new Random(seed);
        initializeWinningParameters();
    }

    public GroupADBasicMCTSPlayerversion4(BasicMCTSParams params) {
        super(params);
        localRnd = new Random(params.getRandomSeed());
        initializeWinningParameters();
    }

    public GroupADBasicMCTSPlayerversion4(BasicMCTSParams params, IStateHeuristic heuristic) {
        super(params);
        params.heuristic = heuristic;
        localRnd = new Random(params.getRandomSeed());
        initializeWinningParameters();
    }

    private void initializeWinningParameters() {
        BasicMCTSParams p = getParameters();

        // Balanced MCTS settings
        p.K = 1.414; // Standard UCB exploration constant
        p.rolloutLength = 15; // Moderate rollout length
        p.maxTreeDepth = 20; // Allow deeper exploration
        p.epsilon = 1e-6; // Small epsilon for numerical stability

        if (p.budgetType == null) {
            p.budgetType = players.PlayerConstants.BUDGET_ITERATIONS;
            p.budget = 3000; // Reasonable budget
        }

        if (p.heuristic == null) {
            p.heuristic = new SushiGoHeuristicversion4();
        }
    }

    public void setStateHeuristic(IStateHeuristic heuristic) {
        getParameters().heuristic = heuristic;
    }

    @Override
    public String toString() {
        return "GroupAD-Dominator-v4";
    }

    @Override
    public AbstractAction _getAction(AbstractGameState gameState, List<AbstractAction> actions) {
        if (actions == null || actions.isEmpty()) {
            return null;
        }

        if (actions.size() == 1) {
            return actions.get(0);
        }

        // For small action spaces, use heuristic
        if (actions.size() <= 3) {
            return selectBestByHeuristic(gameState, actions);
        }

        // MCTS Search
        BasicTreeNodeXversion4 root = new BasicTreeNodeXversion4(
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
            return actions.get(localRnd.nextInt(actions.size()));
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
    public GroupADBasicMCTSPlayerversion4 copy() {
        BasicMCTSParams cp = (BasicMCTSParams) getParameters().copy();

        if (cp.heuristic != null) {
            return new GroupADBasicMCTSPlayerversion4(cp, cp.heuristic);
        } else {
            return new GroupADBasicMCTSPlayerversion4(cp);
        }
    }
}