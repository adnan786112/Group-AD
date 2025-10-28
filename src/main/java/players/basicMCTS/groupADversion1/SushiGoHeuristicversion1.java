package players.basicMCTS.groupADversion1;

import core.AbstractGameState;
import core.interfaces.IStateHeuristic;

public class SushiGoHeuristicversion1 implements IStateHeuristic {

    @Override
    public double evaluateState(AbstractGameState state, int playerId) {
        double value = state.getGameScore(playerId);

        // ------------ TODO: map these to your actual Sushi Go state ------------
        // SushiGoGameState sgs = (SushiGoGameState) state;
        // PlayerBoard me = sgs.getPlayerBoard(playerId);

        int tempura = /* me.getTempuraCount() */ 0;
        int sashimi = /* me.getSashimiCount() */ 0;
        int dumpling = /* me.getDumplingCount() */ 0;
        int maki = /* me.getMakiIcons() */ 0;
        int puddings = /* me.getPuddingCount() */ 0;
        int wasabiUnused = /* me.getUnusedWasabiCount() */ 0;
        int nigiri1 = /* me.getNigiri1Count() */ 0;
        int nigiri2 = /* me.getNigiri2Count() */ 0;
        int nigiri3 =  0;


        value += 5.0 * (tempura / 2) + 2.0 * (tempura % 2);


        int sashTrip = sashimi / 3;
        int sashRem  = sashimi % 3;
        value += 10.0 * sashTrip + (sashRem == 1 ? 2.0 : sashRem == 2 ? 6.0 : 0.0);


        value += (dumpling <= 0) ? 0 :
                (dumpling == 1) ? 1 :
                        (dumpling == 2) ? 3 :
                                (dumpling == 3) ? 6 :
                                        (dumpling == 4) ? 10 : 15;


        double nigiriBase = 1.0 * nigiri1 + 2.0 * nigiri2 + 3.0 * nigiri3;
        value += nigiriBase;
        value += Math.min(wasabiUnused, (nigiri1 + nigiri2 + nigiri3)) * 2.0;

        value += 0.2 * maki;

        value += 0.3 * puddings;



        return value;
    }
}