import thl.isys.Observation;
import thl.isys.State;
import thl.isys.World;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    private static int MIN_OBSERVATION = 15;
    private static int averageEnegery = 0;

    private static int stepDiff(Observation o1, Observation o2) {
        return Math.abs(o1.x - o2.x) + Math.abs(o1.y - o2.y);
    }

    private static int directionDiff(Observation o1, Observation o2) {
        return Math.abs(o1.heading - o2.heading);
    }

    /**
     * Iterates through the map and finds the best prey to eat
     *
     * @param map       Map with the ObservedPreys
     * @param minCount  Minimal amount of observations so the result is good enough
     *
     * @return Which prey to eat
     */
    private static ObservedPrey searchBestPrey(Map<Integer, ObservedPrey> map, int minCount) {
        // Iterate through all Preys and find the prey with the highest observation
        AtomicInteger count = new AtomicInteger();
        map.forEach((k, v) -> {
            if (v.counter > count.get()) {
                count.set(v.counter);
            }
        });

        AtomicReference<ObservedPrey> o = new AtomicReference<>();
        // Iterate through all preys and find the prey with the best probability
        while (o.get() == null && count.get() >= minCount) {
            int finalCount = count.get();
            map.forEach((k, v) -> {
                // We added +10 to both neutral and poison, so we can be sure, that the prey, which is about to get eaten is safe
                if (v.counter == finalCount && v.goodStepProbability + v.goodDirectionProbability > v.neutralStepProbability + v.neutralDirectionProbability + 10
                        && v.goodStepProbability + v.goodDirectionProbability > v.poisonStepProbability + v.poisonDirectionProbability + 10 && (o.get() == null || v.goodStepProbability > o.get().goodStepProbability)) {
                    o.set(v);
                }
            });
            count.getAndDecrement();
        }
        return o.get();
    }

    public static void main(String[] args) throws IOException {

        for (int i = 1; i <= 100; i++) {
            World world = new World("data/data-" + String.format("%03d", i) + ".csv");

            // get pre and current state, so we can compare them
            State preState = world.nextState(-1);
            State currentState = world.nextState(-1);

            // maphelper helps to save the moving patterns from each type
            // and returns the probability of those, if needed
            MapHelper mapHelper = new MapHelper();

            while (currentState.isObserving()) {
                List<Observation> preStateObs = preState.observations();
                List<Observation> currentStateObs = currentState.observations();

                preStateObs.forEach(observation1 -> {
                    currentStateObs.forEach(observation2 -> {
                        if (observation1.id == observation2.id) {
                            mapHelper.addSteps(observation1.type, stepDiff(observation1, observation2));
                            mapHelper.addDirection(observation1.type, directionDiff(observation1, observation2));
                        }
                    });
                });

                // go to the next state
                preState = currentState;
                currentState = world.nextState(-1);
            }
            // after we observed all preys, we calculate the probabilities of those
            // and use them for other comparisons later
            mapHelper.calculateProbabilities();

            // now we go one with the real states (no observation)
            preState = currentState;
            currentState = world.nextState(-1);

            // map for the observedPreys in the rounds
            Map<Integer, ObservedPrey> observedPreys = new TreeMap<>(Collections.reverseOrder());

            // as long the agent is alive and the game hasnt ended
            while (currentState.isAlive() && !currentState.hasSurvived()) {
                // iterators, so we can go through all the preys in the two different states and compare them
                Iterator<Observation> preStateIterator = preState.observations().iterator();
                Iterator<Observation> currStateIterator = currentState.observations().iterator();
                // as long as there are more preys
                while (preStateIterator.hasNext() && currStateIterator.hasNext()) {
                    Observation o1 = preStateIterator.next();
                    Observation o2 = currStateIterator.next();

                    // iterate through and check if the observedPrey isnt the same
                    while (o1.id != o2.id) {
                        // if they arent the same and the prestate prey id is higher
                        // we can remove it from the list, since it died
                        if (o1.id > o2.id) {
                            observedPreys.remove(o2.id);
                            if (currStateIterator.hasNext())
                                o2 = currStateIterator.next();
                        }
                        else {
                            // same as above, just with the different state
                            observedPreys.remove(o1.id);
                            if (preStateIterator.hasNext())
                                o1 = preStateIterator.next();
                        }
                    }
                    int steps = stepDiff(o1, o2);
                    int direction = directionDiff(o1, o2);
                    double goodPercentage = mapHelper.goodStepProbability.get(steps);
                    double neutralPercentage = mapHelper.neutralStepProbability.get(steps);
                    double poisonousPercentage = mapHelper.poisonStepProbability.get(steps);
                    double goodHeadingPercentage = mapHelper.goodDirectionProbability.get(direction);
                    double neutralHeadingPercentage = mapHelper.neutralDirectionProbability.get(direction);
                    double poisonousHeadingPercentage = mapHelper.poisonDirectionProbability.get(direction);

                    ObservedPrey currCalculatedObservation = observedPreys.get(o1.id);

                    if (currCalculatedObservation == null) {
                        ObservedPrey os = new ObservedPrey(o1.id);
                        os.addProbabilities(goodPercentage, neutralPercentage, poisonousPercentage, goodHeadingPercentage, neutralHeadingPercentage, poisonousHeadingPercentage);
                        observedPreys.put(o1.id, os);
                    }
                    else {
                        currCalculatedObservation.addProbabilities(goodPercentage, neutralPercentage, poisonousPercentage, goodHeadingPercentage, neutralHeadingPercentage, poisonousHeadingPercentage);
                    }
                }
                ObservedPrey o = searchBestPrey(observedPreys, MIN_OBSERVATION);
                // iterate
                preState = currentState;
                // if no good prey was found, we continue and dont eat anything
                // else we would eat the found prey
                if (o == null) {
                    currentState = world.nextState(-1);
                } else {
                    currentState = world.nextState(o.id);
                }
                // if the died (ate a poisonous prey or ran out of energy)
                if (!currentState.isAlive()) {
                    System.out.println("Agent died!");
                    System.out.println(currentState.time());
                    System.out.println(currentState.energy());
                    averageEnegery += currentState.energy();
                } else if (currentState.hasSurvived()) {
                    System.out.println("Agent survived!");
                    System.out.println(currentState.time());
                    System.out.println(currentState.energy());
                    averageEnegery += currentState.energy();
                }

            }
        }
        System.out.println("Average amount: " + averageEnegery / 100);
    }
}
