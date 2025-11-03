package players.basicMCTS.groupADversion4;

import core.AbstractGameState;
import core.actions.AbstractAction;
import players.basicMCTS.BasicTreeNode;
import players.basicMCTS.BasicMCTSParams;
import utilities.ElapsedCpuTimer;

import java.util.*;

import static utilities.Utils.noise;

public class BasicTreeNodeXversion4 extends BasicTreeNode {

    private final BasicMCTSParams params;
    private final Random rnd;
    private final Map<AbstractAction, Double> actionValues = new HashMap<>();

    public BasicTreeNodeXversion4(players.basicMCTS.BasicMCTSPlayer player, BasicTreeNodeXversion4 parent, AbstractGameState state, Random rnd) {
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

            BasicTreeNodeXversion4 selected = (BasicTreeNodeXversion4) treePolicy();
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
        while (((BasicTreeNodeXversion4) cur).state.isNotTerminal() && ((BasicTreeNodeXversion4) cur).depth < params.maxTreeDepth) {
            if (!cur.unexpandedActions().isEmpty()) {
                return ((BasicTreeNodeXversion4) cur).expand();
            } else {
                AbstractAction actionChosen = ((BasicTreeNodeXversion4) cur).ucb();
                if (actionChosen == null) {
                    return cur;
                }
                BasicTreeNode next = ((BasicTreeNodeXversion4) cur).children.get(actionChosen);
                if (next == null) {
                    return cur;
                }
                cur = next;
            }
        }
        return cur;
    }


    protected BasicTreeNode expand() {
        List<AbstractAction> notChosen = unexpandedActions();
        if (notChosen == null || notChosen.isEmpty()) return this;

        // Select action based on heuristic with some randomness
        AbstractAction chosen;

        if (notChosen.size() == 1) {
            chosen = notChosen.get(0);
        } else {
            // Evaluate all unexpanded actions
            double[] scores = new double[notChosen.size()];
            double maxScore = -Double.MAX_VALUE;

            for (int i = 0; i < notChosen.size(); i++) {
                AbstractAction a = notChosen.get(i);

                // Get cached value or compute it
                if (!actionValues.containsKey(a)) {
                    AbstractGameState nextState = this.state.copy();
                    a.copy().execute(nextState);
                    double value = params.getStateHeuristic().evaluateState(nextState, player.getPlayerID());
                    actionValues.put(a, value);
                }

                scores[i] = actionValues.get(a);
                maxScore = Math.max(maxScore, scores[i]);
            }

            // Use softmax-like selection with temperature
            double temperature = 2.0;
            double[] probs = new double[notChosen.size()];
            double sum = 0;

            for (int i = 0; i < scores.length; i++) {
                probs[i] = Math.exp((scores[i] - maxScore) / temperature);
                sum += probs[i];
            }

            // Normalize and select
            double rand = rnd.nextDouble() * sum;
            double cumSum = 0;
            int selectedIdx = 0;

            for (int i = 0; i < probs.length; i++) {
                cumSum += probs[i];
                if (rand <= cumSum) {
                    selectedIdx = i;
                    break;
                }
            }

            chosen = notChosen.get(selectedIdx);
        }

        // Create child node
        AbstractGameState nextState = this.state.copy();
        advance(nextState, chosen.copy());
        BasicTreeNode tn = new BasicTreeNodeXversion4(this.player, this, nextState, rnd);
        children.put(chosen, tn);

        return tn;
    }


    protected AbstractAction ucb() {
        AbstractAction bestAction = null;
        double bestValue = -Double.MAX_VALUE;

        for (AbstractAction action : children.keySet()) {
            BasicTreeNode child = children.get(action);
            if (child == null) continue;

            // Standard UCB1 formula
            double exploit = child.totValue / (child.nVisits + params.epsilon);
            double explore = params.K * Math.sqrt(Math.log(this.nVisits + 1) / (child.nVisits + params.epsilon));

            double uctValue = exploit + explore;

            // Add small noise to break ties
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
        double bestValue = -Double.MAX_VALUE;

        for (AbstractAction action : children.keySet()) {
            BasicTreeNode child = children.get(action);
            if (child == null) continue;

            // Use robust child selection: visit count with win rate tiebreaker
            double value = child.nVisits + (child.totValue / (child.nVisits + params.epsilon)) * 0.1;

            if (value > bestValue) {
                bestValue = value;
                best = action;
            }
        }

        return best;
    }
}