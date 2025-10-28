package players.basicMCTS.groupAD;

import core.AbstractGameState;
import core.interfaces.IStateHeuristic;

public class SushiGoHeuristic implements IStateHeuristic {

    @Override
    public double evaluateState(AbstractGameState state, int playerId) {
        double value = state.getGameScore(playerId);

        int tempura = 0;
        int sashimi = 0;
        int dumpling = 0;
        int maki = 0;
        int puddings = 0;
        int wasabi = 0;
        int nigiri1 = 0;
        int nigiri2 = 0;
        int nigiri3 = 0;

        double tempuraScore = 5.0 * (tempura / 2) + 2.5 * (tempura % 2);
        double sashimiScore = 10.0 * (sashimi / 3) + 3.0 * (sashimi % 3);
        double dumplingScore = (dumpling <= 0) ? 0 :
                (dumpling == 1) ? 1 :
                        (dumpling == 2) ? 3 :
                                (dumpling == 3) ? 6 :
                                        (dumpling == 4) ? 10 : 15;
        double nigiriBase = 1.0 * nigiri1 + 2.0 * nigiri2 + 3.0 * nigiri3;
        double wasabiBonus = Math.min(wasabi, (nigiri1 + nigiri2 + nigiri3)) * 4.0;
        double wastedWasabiPenalty = Math.max(0, wasabi - (nigiri1 + nigiri2 + nigiri3)) * -1.5;
        double makiScore = 0.3 * Math.min(maki, 8) + 0.05 * Math.pow(maki, 2);
        double puddingScore = 0.4 * puddings;

        double diversityBonus = (tempura > 0 ? 0.5 : 0) +
                (sashimi > 0 ? 0.5 : 0) +
                (dumpling > 0 ? 0.5 : 0) +
                (nigiriBase > 0 ? 0.5 : 0) +
                (maki > 0 ? 0.5 : 0);

        value += tempuraScore + sashimiScore + dumplingScore + nigiriBase + wasabiBonus
                + wastedWasabiPenalty + makiScore + puddingScore + diversityBonus;

        return value;
    }
}
