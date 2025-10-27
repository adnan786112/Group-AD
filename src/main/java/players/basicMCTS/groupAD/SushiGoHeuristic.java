package players.basicMCTS.groupAD;

import core.AbstractGameState;
import core.interfaces.IStateHeuristic;

public class SushiGoHeuristic implements IStateHeuristic {

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
        int nigiri3 = /* me.getNigiri3Count() */ 0;

        // ------------------ Sets & partial credit ------------------
        // Tempura: pairs = 5; partial for 1/2
        value += 5.0 * (tempura / 2) + 2.0 * (tempura % 2);

        // Sashimi: triplets = 10; partial for 1/3, 2/3
        int sashTrip = sashimi / 3;
        int sashRem  = sashimi % 3;
        value += 10.0 * sashTrip + (sashRem == 1 ? 2.0 : sashRem == 2 ? 6.0 : 0.0);

        // ------------------ Ramps ------------------
        // Dumplings: 1,3,6,10,15
        value += (dumpling <= 0) ? 0 :
                (dumpling == 1) ? 1 :
                        (dumpling == 2) ? 3 :
                                (dumpling == 3) ? 6 :
                                        (dumpling == 4) ? 10 : 15;

        // ------------------ Synergy ------------------
        // Nigiri base + Wasabi potential (rough future triple bonus)
        double nigiriBase = 1.0 * nigiri1 + 2.0 * nigiri2 + 3.0 * nigiri3;
        value += nigiriBase;
        value += Math.min(wasabiUnused, (nigiri1 + nigiri2 + nigiri3)) * 2.0;

        // ------------------ Contests ------------------
        // Maki: light pressure (deeper treatment in selection/denial is optional)
        value += 0.2 * maki;

        // Puddings: small smoothing (the big swing still happens at game end)
        value += 0.3 * puddings;

        // ------------------ Optional tiny denial ------------------
        // If you can access opponents' near-complete sets cheaply, add +/- 0.5–1.0 here.

        return value;
    }
}