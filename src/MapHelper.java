import thl.isys.PreyType;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapHelper {

    // Variables for counting the steps/direction
    public Map<Integer, Integer> goodStepCounter;
    public Map<Integer, Integer> neutralStepCounter;
    public Map<Integer, Integer> poisonStepCounter;

    public Map<Integer, Integer> goodDirectionCounter;
    public Map<Integer, Integer> neutralDirectionCounter;
    public Map<Integer, Integer> poisonDirectionCounter;

    // Variables for calculating the overall probability
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

    /**
     * Adds the step difference to the map and increments the amount
     *
     * @param preyType which prey type
     * @param stepDiff step amount
     */
    public void addSteps(PreyType preyType, int stepDiff) {
        if (preyType == PreyType.GOOD) {
            goodStepCounter.put(stepDiff, goodStepCounter.getOrDefault(stepDiff, 0) + 1);
        } else if (preyType == PreyType.NEUTRAL) {
            neutralStepCounter.put(stepDiff, neutralStepCounter.getOrDefault(stepDiff, 0) + 1);
        } else if (preyType == PreyType.POISONOUS) {
            poisonStepCounter.put(stepDiff, poisonStepCounter.getOrDefault(stepDiff, 0) + 1);
        }
    }

    /**
     * Adds the heading direction to the map and increments the amount
     *
     * @param preyType which prey type
     * @param direction direction
     */
    public void addDirection(PreyType preyType, int direction) {
        if (preyType == PreyType.GOOD) {
            goodDirectionCounter.put(direction, goodDirectionCounter.getOrDefault(direction, 0) + 1);
        } else if (preyType == PreyType.NEUTRAL) {
            neutralDirectionCounter.put(direction, neutralDirectionCounter.getOrDefault(direction, 0) + 1);
        } else if (preyType == PreyType.POISONOUS) {
            poisonDirectionCounter.put(direction, poisonDirectionCounter.getOrDefault(direction, 0) + 1);
        }
    }

    /**
     * calculates the probabilities for every type
     *
     * gets the total amount for a specific type and step/direction and divides it by the total amount
     *
     */
    public void calculateProbabilities() {

        int totalGoodSteps = goodStepCounter.values().stream().reduce(0, Integer::sum);
        int totalNeutralSteps = neutralStepCounter.values().stream().reduce(0, Integer::sum);
        int totalPoisonSteps = poisonStepCounter.values().stream().reduce(0, Integer::sum);
        int totalGoodDirection = goodDirectionCounter.values().stream().reduce(0, Integer::sum);
        int totalNeutralDirection = neutralDirectionCounter.values().stream().reduce(0, Integer::sum);
        int totalPoisonDirection = poisonDirectionCounter.values().stream().reduce(0, Integer::sum);

        // get the highest counter, so we can iterate through all
        int highestCounter = Math.max(goodStepCounter.get(goodStepCounter.keySet().size()),
                Math.max(neutralStepCounter.get(neutralStepCounter.keySet().size()), poisonStepCounter.get(poisonStepCounter.keySet().size())));


        // and add them to the map
        // steps
        for (int i = 1; i <= highestCounter; i++) {
            goodStepProbability.put(i, (goodStepCounter.getOrDefault(i, 0)) / (double) totalGoodSteps);
            neutralStepProbability.put(i, (neutralStepCounter.getOrDefault(i, 0)) / (double) totalNeutralSteps);
            poisonStepProbability.put(i, (poisonStepCounter.getOrDefault(i, 0)) / (double) totalPoisonSteps);
        }

        // direction
        for (int i = 0; i <= 360; i += 90) {
            goodDirectionProbability.put(i, (goodDirectionCounter.getOrDefault(i,0)+1) / (double) totalGoodDirection);
            neutralDirectionProbability.put(i, (neutralDirectionCounter.getOrDefault(i,0)+1) / (double) totalNeutralDirection);
            poisonDirectionProbability.put(i, (poisonDirectionCounter.getOrDefault(i,0)+1) / (double) totalPoisonDirection);
        }
    }

}
