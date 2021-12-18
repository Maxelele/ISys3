/**
 * Beobachtung mit weiteren Details:
 * - Wahrscheinlichkeiten, zu welcher Klasse sie gehört
 * - Zaehler, wie oft dieselbe ID beobachtet wurde
 */
public class CalculatedObservation {

    /**
     * ID
     */
    public final int id;

    /*
     * Wahrscheinlichkeiten
     */
    public double goodPercentage;
    public double neutralPercentage;
    public double poisonousPercentage;
    public double goodHeadingPercentage;
    public double neutralHeadingPercentage;
    public double poisonHeadingPercentage;

    /**
     * Zaehler, wie oft diese ID bereits beobachtet wurde
     */
    public int obsCount;

    public CalculatedObservation(int id, double goodPercentage, double neutralPercentage, double poisonousPercentage, double goodHeadingPercentage, double neutralHeadingPercentage, double poisonousHeadingPercentage) {
        this.id = id;
        this.goodPercentage = Math.log(goodPercentage);
        this.neutralPercentage = Math.log(neutralPercentage);
        this.poisonousPercentage = Math.log(poisonousPercentage);
        this.goodHeadingPercentage = Math.log(goodHeadingPercentage);
        this.neutralHeadingPercentage = Math.log(neutralHeadingPercentage);
        this.poisonHeadingPercentage = Math.log(poisonousHeadingPercentage);
        this.obsCount = 1;
    }

    public void updatePercentages(double newGoodPercentage, double newNeutralPercentage, double newPoisonousPercentage, double newgoodHeadingPercentage, double newneutralHeadingPercentage, double newpoisonousHeadingPercentage) {
        this.goodPercentage = this.goodPercentage + Math.log(newGoodPercentage);
        this.neutralPercentage = this.neutralPercentage + Math.log(newNeutralPercentage);
        this.poisonousPercentage = this.poisonousPercentage + Math.log(newPoisonousPercentage);
        this.goodHeadingPercentage = this.goodHeadingPercentage + Math.log(newgoodHeadingPercentage);
        this.neutralHeadingPercentage = this.neutralHeadingPercentage + Math.log(newneutralHeadingPercentage);
        this.poisonHeadingPercentage = this.poisonHeadingPercentage + Math.log(newpoisonousHeadingPercentage);
        this.obsCount++;
    }
}
