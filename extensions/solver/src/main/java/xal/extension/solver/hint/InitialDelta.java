//
//  InitialDeltaDomain.java
//  xal
//
//  Created by Thomas Pelaia on 4/18/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.solver.hint;

import xal.extension.solver.*;

import java.util.*;

/**
 * A hint that indicates a good initial search space about the initial variable
 * values.
 */
public class InitialDelta extends DomainHint {

    public static final String TYPE = "InitialDelta";

    /**
     * delta keyed by variable
     */
    protected final Map<Variable, Double> variableDeltas;

    /**
     * default delta
     */
    protected final double defaultDelta;

    /**
     * Primary Constructor
     */
    public InitialDelta(final double delta) {
        super("Initial Delta");

        defaultDelta = delta;

        variableDeltas = new HashMap<>();
    }

    /**
     * Constructor
     */
    public InitialDelta() {
        this(Double.NaN);
    }

    /**
     * Get the type identifier of this Hint which will be used to fetch this
     * hint in a table of hints.
     *
     * @return the unique type identifier of this Hint
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Determine if there is an entry for the variable
     */
    @Override
    public boolean hasVariable(final Variable variable) {
        return variableDeltas.containsKey(variable);
    }

    /**
     * add the initial delta for the specified variable
     */
    public void addInitialDelta(final Variable variable, final double delta) {
        variableDeltas.put(variable, delta);
    }

    /**
     * Get the domain for the specified variable.
     */
    @Override
    public double[] getRange(final Variable variable) {
        final Double deltaD = variableDeltas.get(variable);

        if (deltaD != null) {
            return getRange(variable, deltaD);
        } else if (!Double.isNaN(defaultDelta)) {
            return getRange(variable, defaultDelta);
        } else {
            return new double[]{variable.getLowerLimit(), variable.getUpperLimit()};
        }
    }

    /**
     * Get the range given the variable's initial value, limits and the
     * specified delta.
     */
    private static double[] getRange(final Variable variable, final double delta) {
        final double initialValue = variable.getInitialValue();
        final double lowerLimit = Math.max(initialValue - delta, variable.getLowerLimit());
        final double upperLimit = Math.min(initialValue + delta, variable.getUpperLimit());

        return new double[]{lowerLimit, upperLimit};
    }
}
