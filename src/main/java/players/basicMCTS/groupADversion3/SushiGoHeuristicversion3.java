package players.basicMCTS.groupADversion3;

import core.AbstractGameState;
import core.components.Component;
import core.components.Deck;
import core.interfaces.IStateHeuristic;

import java.util.Collections;
import java.util.List;

public class SushiGoHeuristicversion3 implements IStateHeuristic {

    @Override
    public double evaluateState(AbstractGameState state, int playerId) {
        double value = state.getGameScore(playerId);

        // Initialize the counts of the cards
        int tempura = 0;
        int sashimi = 0;
        int dumpling = 0;
        int maki = 0;
        int puddings = 0;
        int wasabi = 0;
        int nigiri1 = 0;
        int nigiri2 = 0;
        int nigiri3 = 0;

        // Retrieve the player's deck and count cards
        List<Component> allComponents = Collections.singletonList(state.getAllComponents()); // Get all components from game state
        Deck<Component> playerDeck = getPlayerDeck(allComponents, playerId); // Get the player's deck

        if (playerDeck != null) {
            // Count occurrences of each card type
            for (Component card : playerDeck) {
                String cardName = card.toString().toLowerCase();
                switch (cardName) {
                    case "tempura":
                        tempura++;
                        break;
                    case "sashimi":
                        sashimi++;
                        break;
                    case "dumpling":
                        dumpling++;
                        break;
                    case "maki roll 1":
                        maki += 1;
                        break;
                    case "maki roll 2":
                        maki += 2;
                        break;
                    case "maki roll 3":
                        maki += 3;
                        break;
                    case "pudding":
                        puddings++;
                        break;
                    case "wasabi":
                        wasabi++;
                        break;
                    case "egg nigiri":
                        nigiri1++;
                        break;
                    case "salmon nigiri":
                        nigiri2++;
                        break;
                    case "squid nigiri":
                        nigiri3++;
                        break;
                }
            }
        }

        // Score for Tempura, Sashimi, Dumplings, Nigiri, Wasabi, Maki, Puddings
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

        // Increased importance for Maki Rolls (adjusted the formula to be more rewarding)
        double makiScore = 0.5 * Math.min(maki, 8) + 0.1 * Math.pow(maki, 2);

        // Pudding score with a slight penalty for excess puddings that might be wasted
        double puddingScore = 0.4 * puddings;
        double puddingPenalty = (puddings > 3) ? -2.0 * (puddings - 3) : 0;

        // Diversity Bonus (now more powerful, rewarding a wider range of cards)
        double diversityBonus = (tempura > 0 ? 1.0 : 0) +
                (sashimi > 0 ? 1.0 : 0) +
                (dumpling > 0 ? 1.0 : 0) +
                (nigiriBase > 0 ? 1.0 : 0) +
                (maki > 0 ? 1.0 : 0);

        // Calculating the total value by summing all the factors
        value += tempuraScore + sashimiScore + dumplingScore + nigiriBase + wasabiBonus
                + wastedWasabiPenalty + makiScore + puddingScore + puddingPenalty + diversityBonus;

        // Adjusting overall scoring to penalize if there are too many low-value cards (such as excess nigiri or wasabi)
        if (nigiri1 + nigiri2 + nigiri3 > 6) {
            value -= 5.0;  // Penalizing excess nigiri cards
        }

        return value;
    }

    private Deck<Component> getPlayerDeck(List<Component> allComponents, int playerId) {
        for (Component c : allComponents) {
            if (c instanceof Deck) {
                Deck<Component> deck = (Deck<Component>) c;
                if (deck.getOwnerId() == playerId) {
                    return deck;
                }
            }
        }
        return null;
    }
}
