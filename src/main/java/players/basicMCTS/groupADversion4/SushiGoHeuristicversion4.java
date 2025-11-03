package players.basicMCTS.groupADversion4;

import core.AbstractGameState;
import core.components.Component;
import core.components.Deck;
import core.interfaces.IStateHeuristic;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SushiGoHeuristicversion4 implements IStateHeuristic {

    @Override
    public double evaluateState(AbstractGameState state, int playerId) {
        // Start with actual game score
        double value = state.getGameScore(playerId);

        // Get all components and find player's deck
        Deck<Component> playerDeck = null;

        List<Component> allComponents = Collections.singletonList(state.getAllComponents());
        for (Component c : allComponents) {
            if (c instanceof Deck) {
                Deck<Component> deck = (Deck<Component>) c;
                if (deck.getOwnerId() == playerId) {
                    playerDeck = deck;
                    break;
                }
            }
        }

        if (playerDeck == null || playerDeck.getSize() == 0) {
            return value;
        }

        // Count card types
        Map<String, Integer> cardCounts = new HashMap<>();
        int makiTotal = 0;

        for (Component card : playerDeck.getComponents()) {
            String cardName = card.toString().toLowerCase();

            // Normalize card names
            if (cardName.contains("tempura")) {
                cardCounts.put("tempura", cardCounts.getOrDefault("tempura", 0) + 1);
            } else if (cardName.contains("sashimi")) {
                cardCounts.put("sashimi", cardCounts.getOrDefault("sashimi", 0) + 1);
            } else if (cardName.contains("dumpling")) {
                cardCounts.put("dumpling", cardCounts.getOrDefault("dumpling", 0) + 1);
            } else if (cardName.contains("pudding")) {
                cardCounts.put("pudding", cardCounts.getOrDefault("pudding", 0) + 1);
            } else if (cardName.contains("wasabi")) {
                cardCounts.put("wasabi", cardCounts.getOrDefault("wasabi", 0) + 1);
            } else if (cardName.contains("egg nigiri")) {
                cardCounts.put("egg", cardCounts.getOrDefault("egg", 0) + 1);
            } else if (cardName.contains("salmon nigiri")) {
                cardCounts.put("salmon", cardCounts.getOrDefault("salmon", 0) + 1);
            } else if (cardName.contains("squid nigiri")) {
                cardCounts.put("squid", cardCounts.getOrDefault("squid", 0) + 1);
            } else if (cardName.contains("maki")) {
                cardCounts.put("maki", cardCounts.getOrDefault("maki", 0) + 1);
                // Extract maki value from card name
                if (cardName.contains("3")) {
                    makiTotal += 3;
                } else if (cardName.contains("2")) {
                    makiTotal += 2;
                } else {
                    makiTotal += 1;
                }
            }
        }

        // Extract counts
        int tempura = cardCounts.getOrDefault("tempura", 0);
        int sashimi = cardCounts.getOrDefault("sashimi", 0);
        int dumpling = cardCounts.getOrDefault("dumpling", 0);
        int pudding = cardCounts.getOrDefault("pudding", 0);
        int wasabi = cardCounts.getOrDefault("wasabi", 0);
        int eggNigiri = cardCounts.getOrDefault("egg", 0);
        int salmonNigiri = cardCounts.getOrDefault("salmon", 0);
        int squidNigiri = cardCounts.getOrDefault("squid", 0);

        // Calculate potential scores

        // Tempura: pairs worth 5 points each
        double tempuraScore = (tempura / 2) * 5.0;
        double tempuraPotential = (tempura % 2) * 2.5; // Half value for incomplete pair

        // Sashimi: sets of 3 worth 10 points each
        double sashimiScore = (sashimi / 3) * 10.0;
        double sashimiPotential = (sashimi % 3) * 2.0; // Partial value for incomplete set

        // Dumpling: increasing value (1,3,6,10,15 for 1-5+)
        double dumplingScore = 0;
        if (dumpling == 1) dumplingScore = 1;
        else if (dumpling == 2) dumplingScore = 3;
        else if (dumpling == 3) dumplingScore = 6;
        else if (dumpling == 4) dumplingScore = 10;
        else if (dumpling >= 5) dumplingScore = 15;

        // Nigiri with wasabi multiplier
        int totalNigiri = eggNigiri + salmonNigiri + squidNigiri;
        int wasabiUsed = Math.min(wasabi, totalNigiri);

        // Base nigiri values: egg=1, salmon=2, squid=3
        double nigiriBase = eggNigiri * 1.0 + salmonNigiri * 2.0 + squidNigiri * 3.0;

        // Wasabi triples nigiri value, so we add 2x the base value for wasabi'd nigiri
        double avgNigiriValue = totalNigiri > 0 ? nigiriBase / totalNigiri : 0;
        double wasabiBonus = wasabiUsed * 2.0 * avgNigiriValue;

        // Penalty for unused wasabi
        double unusedWasabiPenalty = Math.max(0, wasabi - totalNigiri) * -1.0;

        // Maki scoring (competitive - depends on relative position)
        double makiScore = Math.sqrt(makiTotal) * 0.8;

        // Pudding scoring (end game bonus)
        double puddingScore = pudding * 0.6;

        // Synergy bonuses - reward having multiple scoring strategies
        int activeStrategies = 0;
        if (tempura >= 2) activeStrategies++;
        if (sashimi >= 3) activeStrategies++;
        if (dumpling >= 2) activeStrategies++;
        if (totalNigiri >= 2) activeStrategies++;
        if (makiTotal >= 3) activeStrategies++;

        double synergyBonus = activeStrategies * 1.0;

        // Total heuristic value
        value += tempuraScore + tempuraPotential +
                sashimiScore + sashimiPotential +
                dumplingScore +
                nigiriBase + wasabiBonus + unusedWasabiPenalty +
                makiScore +
                puddingScore +
                synergyBonus;

        return value;
    }
}