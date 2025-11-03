package players.basicMCTS.groupADversion3;

import core.AbstractGameState;
import core.actions.AbstractAction;
import players.basicMCTS.BasicTreeNode;
import players.basicMCTS.BasicMCTSParams;
import utilities.ElapsedCpuTimer;

import java.util.*;

import static utilities.Utils.noise;

public class BasicTreeNodeXversion3 extends BasicTreeNode {

    private final BasicMCTSParams params;
    private final Random rnd;
    private final Map<AbstractAction, Double> childHeuristicCache = new HashMap<>();

    public BasicTreeNodeXversion3(players.basicMCTS.BasicMCTSPlayer player, BasicTreeNodeXversion3 parent, AbstractGameState state, Random rnd) {
        super(player, parent, state, rnd);
        this.params = player.getParameters();
        this.rnd = rnd;
    }

    @Override
    public void mctsSearch() {
        long remaining;
        int remainingLimit = params.breakMS;
        ElapsedCpuTimer elapsedTimer = new ElapsedCpuTimer();
        if (params.budgetType == players.PlayerConstants.BUDGET_TIME) {
            elapsedTimer.setMaxTimeMillis(params.budget);
        }

        int numIters = 0;
        boolean stop = false;

        while (!stop) {
            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();

            BasicTreeNodeXversion3 selected = (BasicTreeNodeXversion3) treePolicy();
            double delta = selected.rollOut();
            selected.backUp(delta);

            numIters++;

            players.PlayerConstants budgetType = params.budgetType;
            if (budgetType == players.PlayerConstants.BUDGET_TIME) {
                remaining = elapsedTimer.remainingTimeMillis();
                stop = remaining <= 2 * elapsedTimerIteration.elapsedMillis() || remaining <= remainingLimit;
            } else if (budgetType == players.PlayerConstants.BUDGET_ITERATIONS) {
                stop = numIters >= params.budget;
            } else if (budgetType == players.PlayerConstants.BUDGET_FM_CALLS) {
                stop = numIters >= params.budget;
            }
        }
    }

    protected BasicTreeNode treePolicy() {
        BasicTreeNode cur = this;
        while (((BasicTreeNodeXversion3) cur).state.isNotTerminal() && ((BasicTreeNodeXversion3) cur).depth < params.maxTreeDepth) {
            if (!cur.unexpandedActions().isEmpty()) {
                return expandBestAction((BasicTreeNodeXversion3) cur);
            } else {
                AbstractAction actionChosen = ((BasicTreeNodeXversion3) cur).ucb();
                BasicTreeNode next = ((BasicTreeNodeXversion3) cur).children.get(actionChosen);
                if (next == null) {
                    return expandBestAction((BasicTreeNodeXversion3) cur);
                }
                cur = next;
            }
        }
        return cur;
    }

    private BasicTreeNode expandBestAction(BasicTreeNodeXversion3 node) {
        List<AbstractAction> notChosen = node.unexpandedActions();
        if (notChosen == null || notChosen.isEmpty()) return node;

        List<ActionScore> actionScores = new ArrayList<>();

        for (AbstractAction a : notChosen) {
            double score = evaluateActionDeep(a, node.state);
            childHeuristicCache.put(a, score);
            actionScores.add(new ActionScore(a, score));
        }

        actionScores.sort((a, b) -> Double.compare(b.score, a.score));

        AbstractAction chosen;
        if (rnd.nextDouble() < 0.05) { // 5% random exploration
            int idx = Math.min(rnd.nextInt(2), actionScores.size() - 1);
            chosen = actionScores.get(idx).action;
        } else {
            chosen = actionScores.get(0).action;
        }

        AbstractGameState nextState = node.state.copy();
        node.advance(nextState, chosen.copy());
        BasicTreeNode tn = new BasicTreeNodeXversion3(this.player, this, nextState, rnd);
        node.children.put(chosen, tn);

        return tn;
    }

    private double evaluateActionDeep(AbstractAction a, AbstractGameState currentState) {
        AbstractGameState nextState = currentState.copy();
        AbstractAction ac = a.copy();
        ac.execute(nextState);

        double nextValue = params.getStateHeuristic().evaluateState(nextState, player.getPlayerID());
        double currentValue = params.getStateHeuristic().evaluateState(currentState, player.getPlayerID());

        double improvement = nextValue - currentValue;

        return nextValue + improvement * 3.0;
    }

    protected AbstractAction ucb() {
        AbstractAction bestAction = null;
        double bestValue = -Double.MAX_VALUE;

        double parentHeuristic = params.getStateHeuristic().evaluateState(this.state, player.getPlayerID());

        for (AbstractAction action : children.keySet()) {
            BasicTreeNode child = children.get(action);
            if (child == null) continue;

            double exploit = child.totValue / (child.nVisits + params.epsilon);
            double explore = params.K * Math.sqrt(Math.log(this.nVisits + 1) / (child.nVisits + params.epsilon));

            double childHeur = childHeuristicCache.getOrDefault(action, 0.0);
            double heuristicDelta = childHeur - parentHeuristic;
            double bias = 20.0 * heuristicDelta / (1.0 + Math.abs(parentHeuristic));

            double biasDecay = 1.0 / (1.0 + 0.1 * Math.log(child.nVisits + 1));
            bias *= biasDecay;

            double uctValue = exploit + 0.3 * explore + bias;

            uctValue = noise(uctValue, params.epsilon, 0.0);

            if (uctValue > bestValue) {
                bestValue = uctValue;
                bestAction = action;
            }
        }
        return bestAction;
    }

    @Override
    public AbstractAction bestAction() {
        AbstractAction best = null;
        double bestScore = -Double.MAX_VALUE;

        for (AbstractAction action : children.keySet()) {
            BasicTreeNode child = children.get(action);
            if (child == null) continue;

            double winRate = child.totValue / (child.nVisits + params.epsilon);
            double heuristicScore = childHeuristicCache.getOrDefault(action, 0.0);

            double combinedScore = winRate * 1000 + Math.log(child.nVisits + 1) * 10 + heuristicScore;

            if (combinedScore > bestScore) {
                bestScore = combinedScore;
                best = action;
            }
        }

        return best;
    }

    private static class ActionScore {
        AbstractAction action;
        double score;

        ActionScore(AbstractAction a, double s) {
            action = a;
            score = s;
        }
    }
}
