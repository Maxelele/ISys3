import thl.isys.Observation;
import thl.isys.State;
import thl.isys.World;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    private static int calcSteps(Observation o1, Observation o2) {
        int xDiff = Math.abs(o1.x - o2.x);
        int yDiff = Math.abs(o1.y - o2.y);

        return xDiff + yDiff;
    }

    private static int calcHeading(Observation o1, Observation o2) {
        return Math.abs(o1.heading - o2.heading);
    }

    /**
     * Durchsucht die Map nach der geeignetesten Beute.
     * Bedingungen:
     * - obsCount muss möglichst max sein
     * - goodPercentage muss hoeher sein als neutral und poisonous
     *
     * @param map       Beobachtungs-Map
     * @param min_count Minimale Anzahl an Beobachtungsbesuchen, um sicherzustellen, dass genug Informationen vorliegen
     * @return Beste Beute zum essen
     */
    private static CalculatedObservation getBestObsToEat(Map<Integer, CalculatedObservation> map, int min_count) {
        // Durchsuche alle Preys und finde die hoechste beobachtungszahl
        AtomicInteger count = new AtomicInteger();
        map.forEach((k, v) -> {
            if (v.obsCount > count.get()) {
                count.set(v.obsCount);
            }
        });

        AtomicReference<CalculatedObservation> o = new AtomicReference<>();
        while (o.get() == null && count.get() >= min_count) {
            // Suche Prey mit bester Wahrscheinlichkeit
            int finalCount = count.get();
            map.forEach((k, v) -> {
                if (v.obsCount == finalCount && v.goodPercentage + v.goodHeadingPercentage > v.neutralPercentage + v.neutralHeadingPercentage + 10
                        && v.goodPercentage + v.goodHeadingPercentage > v.poisonousPercentage + v.poisonHeadingPercentage + 10 && (o.get() == null || v.goodPercentage > o.get().goodPercentage)) {
                    o.set(v);
                }
            });
            count.getAndDecrement();
        }
        return o.get();
    }

    public static void main(String[] args) throws IOException {

        for (int i = 1; i <= 100; i++) {
            String worldnumber = i < 10 ? "00" + i : i < 100 ? "0" + i : "" + i;
            World world = new World("data/data-"+worldnumber+".csv");

            State preState = world.nextState(-1);
            State currentState = world.nextState(-1);

            MapHelper mapHelper = new MapHelper();

            while (currentState.isObserving()) {
                List<Observation> preStateObs = preState.observations();
                List<Observation> currentStateObs = currentState.observations();


                preStateObs.forEach(observation1 -> {
                    currentStateObs.forEach(observation2 -> {
                        if (observation1.id == observation2.id) {
                            mapHelper.addSteps(observation1.type, calcSteps(observation1, observation2));
                            mapHelper.addDirection(observation1.type, calcHeading(observation1, observation2));
                        }
                    });
                });

                // States weiter iterieren
                preState = currentState;
                currentState = world.nextState(-1);
            }

            /* -- Wahrscheinlichkeiten werden berechnet -- */

            mapHelper.calculateProbabilities();

            /* ----- KLASSIFIKATIONS PHASE ----- */
            // States weiter iterieren
            preState = currentState;
            currentState = world.nextState(-1);

            // Map aus einzelnen Beobachtungen inkl. Wahrscheinlichkeitsinformationen
            Map<Integer, CalculatedObservation> currPercentages = new TreeMap<>(Collections.reverseOrder());

            while (currentState.isAlive() && !currentState.hasSurvived()) {
                /* --- Zwei einzelne States werden miteinander verglichen --- */
                ListIterator<Observation> preStateIterator = preState.observations().listIterator();
                ListIterator<Observation> currStateIterator = currentState.observations().listIterator();
                while (preStateIterator.hasNext() && currStateIterator.hasNext()) {
                    Observation o1 = preStateIterator.next();
                    Observation o2 = currStateIterator.next();
                    int steps;
                    int headings;

                    // Durchlauf die Schleife solange ids nicht übereinstimmen (weil ein Iterator weitergelaufen ist)
                    while (o1.id != o2.id) {
                        // Fall: PreState ist bereits mind. eine ID weiter
                        if (o1.id > o2.id) {
                            // Folge: Eintrag mit o2.id ist bereits verstorben
                            currPercentages.remove(o2.id);
                            if (currStateIterator.hasNext())
                                o2 = currStateIterator.next();
                        }
                        // Fall: CurrState ist bereits mind. eine ID weiter
                        else {
                            // Folge: Eintrag mit o1.id ist bereits verstorben
                            currPercentages.remove(o1.id);
                            if (preStateIterator.hasNext())
                                o1 = preStateIterator.next();
                        }
                    }
                    // ids stimmen ueberein
                    steps = calcSteps(o1, o2);
                    headings = calcHeading(o1, o2);
                    double goodPercentage = mapHelper.goodStepProbability.get(steps);
                    double neutralPercentage = mapHelper.neutralStepProbability.get(steps);
                    double poisonousPercentage = mapHelper.poisonStepProbability.get(steps);
                    double goodHeadingPercentage = mapHelper.goodDirectionProbability.get(headings);
                    double neutralHeadingPercentage = mapHelper.neutralDirectionProbability.get(headings);
                    double poisonousHeadingPercentage = mapHelper.poisonDirectionProbability.get(headings);

                    CalculatedObservation currCalculatedObservation = currPercentages.get(o1.id);

                    // Fall: ID ist noch unbekannt
                    if (currCalculatedObservation == null) {
                        currPercentages.put(o1.id, new CalculatedObservation(o1.id, goodPercentage, neutralPercentage, poisonousPercentage, goodHeadingPercentage, neutralHeadingPercentage, poisonousHeadingPercentage));
                    }
                    // Fall: ID ist bekannt -> updaten
                    else {
                        currCalculatedObservation.updatePercentages(goodPercentage, neutralPercentage, poisonousPercentage, goodHeadingPercentage, neutralHeadingPercentage, poisonousHeadingPercentage);
                    }
                }
                CalculatedObservation o = getBestObsToEat(currPercentages, 20);
                // iterate
                preState = currentState;
                if (o == null) {
                    // dann gibts noch keinen guten!
                    currentState = world.nextState(-1);
                } else {
                    // dann essen!
                    currentState = world.nextState(o.id);
                }
                // Fehler: Tot
                if (!currentState.isAlive()) {
                    System.out.println("Agent died!");
                    System.out.println(currentState.time());
                    System.out.println(currentState.energy());
                } else if (currentState.time() == 10000) {
                    System.out.println("Agent survived!");
                    System.out.println(currentState.time());
                    System.out.println(currentState.energy());
                }

            }
        }
    }
}
