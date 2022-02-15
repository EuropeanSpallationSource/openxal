/*
 *  Score.java
 *
 *  Created Monday June 14, 2004 3:29 pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver;

/**
 * Score is a collection of objectives with scores mapped to those objectives.
 *
 * @author ky6
 */
public class Score {

    /**
     * Description of the Field
     */
    protected final Objective objective;
    /**
     * Description of the Field
     */
    protected final double value;
    /**
     * Description of the Field
     */
    protected final double satisfaction;

    /**
     * Creates a new instance of Score.
     *
     * @param anObjective The objective to be scored.
     * @param aValue The value given to the objective.
     */
    public Score(Objective anObjective, double aValue) {
        objective = anObjective;
        value = aValue;
        satisfaction = objective.satisfaction(value);
    }

    /**
     * Get the satisfaction.
     *
     * @return The satisfaction as a double.
     */
    public double getSatisfaction() {
        return satisfaction;
    }

    /**
     * Get the objective.
     *
     * @return The objective to be scored.
     */
    public Objective getObjective() {
        return objective;
    }

    /**
     * Get the objective's value.
     *
     * @return The value
     */
    public double getValue() {
        return value;
    }

    /**
     * A string for displaying a score. The string consist of a objective and a
     * value.
     *
     * @return The string representation of a score.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Objective: ").append(objective.toString()).append(", ");
        buffer.append("Value: ").append(value).append(", ");
        buffer.append("Satisfaction: ").append(satisfaction);

        return buffer.toString();
    }
}
