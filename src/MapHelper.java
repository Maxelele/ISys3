import thl.isys.PreyType;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapHelper {

    // Zaehlen
    public Map<Integer, Integer> goodStepCounter;
    public Map<Integer, Integer> neutralStepCounter;
    public Map<Integer, Integer> poisonStepCounter;

    public Map<Integer, Integer> goodDirectionCounter;
    public Map<Integer, Integer> neutralDirectionCounter;
    public Map<Integer, Integer> poisonDirectionCounter;

    // Berechnung
    public Map<Integer, Double> goodStepProbability;
    public Map<Integer, Double> neutralStepProbability;
    public Map<Integer, Double> poisonStepProbability;

    public Map<Integer, Double> goodDirectionProbability;
    public Map<Integer, Double> neutralDirectionProbability;
    public Map<Integer, Double> poisonDirectionProbability;

    MapHelper() {
        goodStepCounter = new LinkedHashMap<>();
        neutralStepCounter = new LinkedHashMap<>();
        poisonStepCounter = new LinkedHashMap<>();
        goodDirectionCounter = new LinkedHashMap<>();
        neutralDirectionCounter = new LinkedHashMap<>();
        poisonDirectionCounter = new LinkedHashMap<>();
        goodStepProbability = new LinkedHashMap<>();
        neutralStepProbability = new LinkedHashMap<>();
        poisonStepProbability = new LinkedHashMap<>();
        goodDirectionProbability = new LinkedHashMap<>();
        neutralDirectionProbability = new LinkedHashMap<>();
        poisonDirectionProbability = new LinkedHashMap<>();
    }

    public void addSteps(PreyType preyType, int stepDiff) {
        if (preyType == PreyType.GOOD) {
            goodStepCounter.put(stepDiff, goodStepCounter.getOrDefault(stepDiff, 0) + 1);
        } else if (preyType == PreyType.NEUTRAL) {
            neutralStepCounter.put(stepDiff, neutralStepCounter.getOrDefault(stepDiff, 0) + 1);
        } else if (preyType == PreyType.POISONOUS) {
            poisonStepCounter.put(stepDiff, poisonStepCounter.getOrDefault(stepDiff, 0) + 1);
        }
    }

    public void addDirection(PreyType preyType, int direction) {
        if (preyType == PreyType.GOOD) {
            goodDirectionCounter.put(direction, goodDirectionCounter.getOrDefault(direction, 0) + 1);
        } else if (preyType == PreyType.NEUTRAL) {
            neutralDirectionCounter.put(direction, neutralDirectionCounter.getOrDefault(direction, 0) + 1);
        } else if (preyType == PreyType.POISONOUS) {
            poisonDirectionCounter.put(direction, poisonDirectionCounter.getOrDefault(direction, 0) + 1);
        }
    }

    public void calculateProbabilities() {
        // Summe an gezaehlten Werten pro Klasse
        // -> Hier werden alle Werte mit 1 addiert, um Rechenfehler zu vermeiden
        int totalGoodCount = goodStepCounter.values().stream().reduce(0, Integer::sum) + 1;
        int totalNeutralCount = neutralStepCounter.values().stream().reduce(0, Integer::sum) + 1;
        int totalPoisonousCount = poisonStepCounter.values().stream().reduce(0, Integer::sum) + 1;
        int totalGoodHeadingCount = goodDirectionCounter.values().stream().reduce(0, Integer::sum) +1;
        int totalNeutralHeadingCount = neutralDirectionCounter.values().stream().reduce(0, Integer::sum) +1;
        int totalPoisonousHeadingCount = poisonDirectionCounter.values().stream().reduce(0, Integer::sum) +1;

        // hoechster Key (Laengste Schrittfolge) wird ermittelt
        int highestKeyObservation = Math.max(goodStepCounter.get(goodStepCounter.keySet().size()),
                Math.max(neutralStepCounter.get(neutralStepCounter.keySet().size()), poisonStepCounter.get(poisonStepCounter.keySet().size())));


        // Wahrscheinlichkeiten werden eingetragen
        // -> Hier werden alle Werte mit 1 addiert, um Rechenfehler zu vermeiden
        for (int i = 1; i <= highestKeyObservation; i++) {
            goodStepProbability.put(i, (goodStepCounter.getOrDefault(i, 0) + 1) / (double) totalGoodCount);
            neutralStepProbability.put(i, (neutralStepCounter.getOrDefault(i, 0) + 1) / (double) totalNeutralCount);
            poisonStepProbability.put(i, (poisonStepCounter.getOrDefault(i, 0) + 1) / (double) totalPoisonousCount);
        }

        for (int i = 0; i <= 360; i += 90) {
            goodDirectionProbability.put(i, (goodDirectionCounter.getOrDefault(i,0)+1) / (double) totalGoodHeadingCount);
            neutralDirectionProbability.put(i, (neutralDirectionCounter.getOrDefault(i,0)+1) / (double) totalNeutralHeadingCount);
            poisonDirectionProbability.put(i, (poisonDirectionCounter.getOrDefault(i,0)+1) / (double) totalPoisonousHeadingCount);
        }
    }

}
