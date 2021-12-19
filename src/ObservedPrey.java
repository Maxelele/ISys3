
public class ObservedPrey {

    // Prey ID
    public final int id;

    // Probabilities
    public double goodStepProbability;
    public double neutralStepProbability;
    public double poisonStepProbability;
    public double goodDirectionProbability;
    public double neutralDirectionProbability;
    public double poisonDirectionProbability;

    /**
     * Counter which shows how often the prey was observed
     */
    public int counter;

    public ObservedPrey(int id) {
        this.id = id;
        this.goodStepProbability = 0;
        this.neutralStepProbability = 0;
        this.poisonStepProbability = 0;
        this.goodDirectionProbability = 0;
        this.neutralDirectionProbability = 0;
        this.poisonDirectionProbability = 0;
        this.counter = 0;
    }

    /**
     * Adds the new probability to the old one
     * Log, because of tiny numbers
     */
    public void addProbabilities(double goodPercentage, double neutralPercentage, double poisonPercentage, double goodDirectionProbability, double neutralDirectionProbability, double poisonDirectionProbability) {
        this.goodStepProbability = this.goodStepProbability + Math.log(goodPercentage);
        this.neutralStepProbability = this.neutralStepProbability + Math.log(neutralPercentage);
        this.poisonStepProbability = this.poisonStepProbability + Math.log(poisonPercentage);
        this.goodDirectionProbability = this.goodDirectionProbability + Math.log(goodDirectionProbability);
        this.neutralDirectionProbability = this.neutralDirectionProbability + Math.log(neutralDirectionProbability);
        this.poisonDirectionProbability = this.poisonDirectionProbability + Math.log(poisonDirectionProbability);
        this.counter++;
    }
}
