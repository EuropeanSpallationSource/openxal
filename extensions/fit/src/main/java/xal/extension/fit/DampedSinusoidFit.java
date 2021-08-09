//
// DampedSinusoid.java: Source file for 'DampedSinusoid'
// Project xal
//
// Created by t6p on 3/11/2010
//
package xal.extension.fit;

import xal.tools.math.DifferentialVariable;
import xal.extension.solver.*;
import xal.extension.solver.hint.InitialDelta;

import java.util.*;

/**
 * DampedSinusoid provides an exact closed form solution for fitting a waveform
 * to a damped sinusoid of the form <strong><code><em>q</em> =
 * <em>A</em>e<em><sup>&gamma;t</sup></em>sin(<em>&mu;t</em> + <em>&phi;</em>) +
 * <em>C</em></code></strong> which is adapted for efficient fitting in the
 * presence of noise. The fits for frequency, offset and growth rate are good in
 * the presence of relatively small noise. However, the estimation of phase and
 * amplitude are relatively poor in the presence of noise. Also, the estimation
 * breaks down when the frequency is near an integer or half integer.
 */
public final class DampedSinusoidFit {

    /**
     * raw waveform
     */
    private final double[] waveform;

    /**
     * number of points to use
     */
    private final int numPoints;

    /**
     * waveform with the offset removed
     */
    private DifferentialVariable[] initialZeroedWaveform;

    /**
     * optimized waveform error
     */
    private double[] waveformError;

    /**
     * initial waveform error
     */
    private double[] initialWaveformError;

    /**
     * indicates whether the initial waveform error has been calculated
     */
    private boolean initialWaveformErrorCalculated;

    /**
     * initial fitted offset
     */
    private DifferentialVariable initialOffset;

    /**
     * initial estimate of the signal variance
     */
    private double initialSignalVariance;

    /**
     * indicates whether the offset has been calculated
     */
    private boolean initialOffsetCalculated;

    /**
     * optimal offset
     */
    private double offset;

    /**
     * optimized estimate of the signal variance
     */
    private double signalVariance;

    /**
     * initial fitted growth rate
     */
    private DifferentialVariable initialGrowthRate;

    /**
     * initial value of the exponent of the growth rate
     */
    private DifferentialVariable initialGrowthFactor;

    /**
     * indicates whether the growth rate has been calculated
     */
    private boolean initialGrowthRateCalculated;

    /**
     * optimized growth rate
     */
    private double growthRate;

    /**
     * optimized growth factor
     */
    private double growthFactor;

    /**
     * fitted frequency
     */
    private DifferentialVariable initialFrequency;

    /**
     * initial fitted cosine of the angular frequency
     */
    private DifferentialVariable initialCosineMu;

    /**
     * indicates whether the frequency has been calculated
     */
    private boolean initialFrequencyCalculated;

    /**
     * optimized frequency
     */
    private double frequency;

    /**
     * optimized cosine mu
     */
    private double cosineMu;

    /**
     * fitted phase for the sine-like wave
     */
    private double initialPhase;

    /**
     * indicates whether the phase has been calculated
     */
    private boolean initialPhaseCalculated;

    /**
     * optimized phase
     */
    private double phase;

    /**
     * fitted amplitude
     */
    private double initialAmplitude;

    /**
     * indicates whether the amplitude has been calculated
     */
    private boolean initialAmplitudeCalculated;

    /**
     * optimized amplitude
     */
    private double amplitude;

    /**
     * Primary Constructor
     *
     * @param waveform data to fit
     * @param count starting from the beginning of the waveform, the number of
     * items in the waveform to use in the fit
     */
    public DampedSinusoidFit(final double[] waveform, final int count) {
        if (count < 6) {
            throw new IllegalArgumentException("The element count must be at least six. The supplied element count was: " + count);
        }
        if (count > waveform.length) {
            throw new IllegalArgumentException("The element count " + count + " is greater than the waveform length: " + waveform.length);
        }

        this.waveform = waveform;
        numPoints = count;

        waveformError = new double[count];

        initialOffsetCalculated = false;
        initialOffset = null;
        offset = Double.NaN;

        initialSignalVariance = Double.NaN;
        signalVariance = Double.NaN;

        initialGrowthRateCalculated = false;
        initialGrowthRate = null;
        initialGrowthFactor = null;
        growthFactor = Double.NaN;
        growthRate = Double.NaN;

        initialFrequencyCalculated = false;
        initialFrequency = null;
        initialCosineMu = null;
        frequency = Double.NaN;
        cosineMu = Double.NaN;

        initialWaveformErrorCalculated = false;
        initialWaveformError = new double[count];

        initialPhaseCalculated = false;
        initialPhase = Double.NaN;
        phase = Double.NaN;

        initialAmplitudeCalculated = false;
        initialAmplitude = Double.NaN;
        amplitude = Double.NaN;
    }

    /**
     * Constructor accepting the entire waveform
     *
     * @param waveform data to fit
     */
    public DampedSinusoidFit(final double[] waveform) {
        this(waveform, waveform.length);
    }

    /**
     * Get a new instance of the damped sinusoid
     *
     * @param waveform data to fit
     * @param count starting from the beginning of the waveform, the number of
     * items in the waveform to use in the fit
     */
    public static DampedSinusoidFit getInstance(final double[] waveform, final int count) {
        return new DampedSinusoidFit(waveform, count);
    }

    /**
     * Get a new instance of the damped sinusoid using the entire waveform
     *
     * @param waveform data to fit
     */
    public static DampedSinusoidFit getInstance(final double[] waveform) {
        return new DampedSinusoidFit(waveform);
    }

    /**
     * calculate the mean square error
     */
    private double calculateRMSError(final double[] errors) {
        double errorSum = 0.0;
        for (final double epsilon : errors) {
            errorSum += epsilon * epsilon;
        }

        return Math.sqrt(errorSum / numPoints);
    }

    /**
     * calculate the initial waveform error
     */
    private void fitInitialWaveformError() {
        final double iniOffset = getInitialOffset();
        final double iniGrowthFactor = getInitialGrowthFactor();
        final double cosmu = getInitialCosineMu();

        initialWaveformError = calculateLeastWaveformError(iniOffset, cosmu, iniGrowthFactor);

        initialWaveformErrorCalculated = true;
    }

    /**
     * calculate the waveform error
     */
    private double[] calculateLeastWaveformError(final double offset, final double cosmu, final double growthFactor) {
        final double growthFactorSquared = growthFactor * growthFactor;
        final double alpha = 2.0 * growthFactor * cosmu;

        final double[] f = new double[numPoints];
        final double[] g = new double[numPoints];
        final double[] h = new double[numPoints];

        f[0] = 1.0;
        f[1] = 0.0;
        g[0] = 0.0;
        g[1] = 1.0;
        h[0] = 0.0;
        h[1] = 0.0;

        double ffSum = 1.0;
        double ggSum = 1.0;
        double fgSum = 0.0;
        double ghSum = 0.0;
        double fhSum = 0.0;
        for (int index = 2; index < numPoints; index++) {
            final double f0 = f[index - 2];
            final double f1 = f[index - 1];
            final double g0 = g[index - 2];
            final double g1 = g[index - 1];
            final double h0 = h[index - 2];
            final double h1 = h[index - 1];
            f[index] = alpha * f1 - growthFactorSquared * f0;
            g[index] = alpha * g1 - growthFactorSquared * g0;
            h[index] = alpha * h1 - growthFactorSquared * h0 + alpha * (waveform[index - 1] - offset) - growthFactorSquared * (waveform[index - 2] - offset) + offset - waveform[index];

            ffSum += f[index] * f[index];
            ggSum += g[index] * g[index];
            fgSum += f[index] * g[index];
            ghSum += g[index] * h[index];
            fhSum += f[index] * h[index];
        }

        final double[] leastErrors = new double[numPoints];
        final double wronskian = ffSum * ggSum - fgSum * fgSum;
        leastErrors[0] = wronskian != 0.0 ? (ghSum * fgSum - ggSum * fhSum) / wronskian : 0.0;
        leastErrors[1] = wronskian != 0.0 ? (fhSum * fgSum - ffSum * ghSum) / wronskian : 0.0;

        for (int index = 2; index < numPoints; index++) {
            leastErrors[index] = f[index] * leastErrors[0] + g[index] * leastErrors[1] + h[index];
        }
        return leastErrors;
    }

    /**
     * scores the RMS error
     */
    private class ErrorScorer implements Scorer {

        /**
         * offset variable
         */
        private final Variable offsetVariable;

        /**
         * variable for exponent of the growth rate
         */
        private final Variable growthFactorVariable;

        /**
         * variable for cosine mu
         */
        private final Variable cosineMuVariable;

        /**
         * Constructor
         */
        public ErrorScorer(final Variable offsetVariable, final Variable growthFactorVariable, final Variable cosineMuVariable) {
            this.offsetVariable = offsetVariable;
            this.growthFactorVariable = growthFactorVariable;
            this.cosineMuVariable = cosineMuVariable;
        }

        /**
         * score the RMS error
         */
        @Override
        public double score(final Trial trial, final List<Variable> variables) {
            final TrialPoint trialPoint = trial.getTrialPoint();
            final double newOffset = trialPoint.getValue(offsetVariable);
            final double cosmu = trialPoint.getValue(cosineMuVariable);
            final double newGrowthFactor = trialPoint.getValue(growthFactorVariable);

            if (Double.isNaN(newOffset) || Double.isNaN(cosmu) || Double.isNaN(newGrowthFactor)) {
                return Double.POSITIVE_INFINITY;
            }

            final double[] waveformErrors = calculateLeastWaveformError(newOffset, cosmu, newGrowthFactor);
            double sumSquareError = 0.0;
            for (final double error : waveformErrors) {
                sumSquareError += error * error;
            }

            if (Double.isNaN(sumSquareError)) {
                trial.vetoTrial(new TrialVeto(trial, null, "error is NaN"));
                return Double.POSITIVE_INFINITY;
            }

            return Math.sqrt(sumSquareError / waveformErrors.length);
        }
    }

    /**
     * Run the solver to find the best fit
     *
     * @param noiseLevel estimate of the expected noise
     */
    public void solveWithNoise(final double noiseLevel) {
        solveWithNoiseMaxEvaluationsSatisfaction(noiseLevel, 100000, 0.95);
    }

    /**
     * Run the solver to find the best fit
     *
     * @param noiseLevel estimate of the expected noise
     * @param maxEvaluations the maximum number of evaluations to perform
     */
    public void solveWithNoiseMaxEvaluations(final double noiseLevel, final int maxEvaluations) {
        final Stopper stopper = SolveStopperFactory.maxEvaluationsStopper(maxEvaluations);
        solve(noiseLevel, stopper);
    }

    /**
     * Run the solver to find the best fit
     *
     * @param noiseLevel estimate of the expected noise
     * @param maxEvaluations the maximum number of evaluations to perform
     * @param satisfaction the satisfaction target to reach before stopping
     */
    public void solveWithNoiseMaxEvaluationsSatisfaction(final double noiseLevel, final int maxEvaluations, final double satisfaction) {
        final Stopper stopper = SolveStopperFactory.maxEvaluationsSatisfactionStopper(maxEvaluations, satisfaction);
        solve(noiseLevel, stopper);
    }

    /**
     * Run the solver to find the best fit
     *
     * @param noiseLevel estimate of the expected noise
     * @param maxTime the maximum time to wait for the solution
     */
    public void solveWithNoiseMaxTime(final double noiseLevel, final double maxTime) {
        solveWithNoiseMaxTimeSatisfaction(noiseLevel, maxTime, 0.95);
    }

    /**
     * Run the solver to find the best fit
     *
     * @param noiseLevel estimate of the expected noise
     * @param maxTime the maximum time to wait for the solution
     * @param satisfaction the satisfaction target to reach before stopping
     */
    public void solveWithNoiseMaxTimeSatisfaction(final double noiseLevel, final double maxTime, final double satisfaction) {
        final Stopper stopper = SolveStopperFactory.minMaxTimeSatisfactionStopper(0.01, maxTime, satisfaction);
        solve(noiseLevel, stopper);
    }

    /**
     * Run the solver to find the best fit
     *
     * @param noiseLevel the noise level used to estimate the error bounds and
     * provide a measure for satisfaction
     * @param stopper the stopper which determines when to stop the solver
     */
    public void solve(final double noiseLevel, final Stopper stopper) {
        final double iniOffset = getInitialOffset();

        final double iniGrowthFactor = getInitialGrowthFactor();
        final double newGrowthFactor = Double.isNaN(iniGrowthFactor) ? 1.0 : iniGrowthFactor > 0.0 ? iniGrowthFactor : 1.0;

        final double initialCosMu = getInitialCosineMu();
        final double cosmu = Double.isNaN(initialCosMu) ? 0.0 : initialCosMu < -1.0 ? -1.0 : initialCosMu > 1.0 ? 1.0 : initialCosMu;

        final double iniSignalVariance = noiseLevel > 0.0 ? noiseLevel * noiseLevel : getInitialSignalVariance();
        final double initialOffsetSigma = Math.sqrt(initialOffset.varianceWithSignalVariance(iniSignalVariance));
        final double initialGrowthFactorSigma = Math.sqrt(this.initialGrowthFactor.varianceWithSignalVariance(iniSignalVariance));
        final double initialCosMuSigma = Math.sqrt(initialCosineMu.varianceWithSignalVariance(iniSignalVariance));

        // if the initial sigmas are indeterminate we attempt to guess, but better guesses are needed
        final double offsetSigma = Double.isNaN(initialOffsetSigma) ? 10.0 : initialOffsetSigma;
        final double growthFactorSigma = Double.isNaN(initialGrowthFactorSigma) ? 1.0 : initialGrowthFactorSigma;
        final double cosMuSigma = Double.isNaN(initialCosMuSigma) ? 0.1 : initialCosMuSigma;

        // allow ten sigma for ranges
        final double offsetSlack = 10.0 * offsetSigma;
        final double growthFactorSlack = 10.0 * growthFactorSigma;
        final double cosMuSlack = 10.0 * cosMuSigma;

        final double lowerCosine = Math.max(-1.0, cosmu - cosMuSlack);
        final double upperCosine = Math.min(1.0, cosmu + cosMuSlack);

        final double lowerGrowthFactor = Math.max(0.0, newGrowthFactor - growthFactorSlack);

        final Variable offsetVariable = new Variable("offset", iniOffset, iniOffset - offsetSlack, iniOffset + offsetSlack);
        final Variable cosineMuVariable = new Variable("cosmu", cosmu, lowerCosine, upperCosine);
        final Variable growthFactorVariable = new Variable("growthFactor", newGrowthFactor, lowerGrowthFactor, newGrowthFactor + growthFactorSlack);

        final List<Variable> variables = new ArrayList<>();
        variables.add(offsetVariable);
        variables.add(cosineMuVariable);
        variables.add(growthFactorVariable);

        final double errorTolerance = Math.sqrt(iniSignalVariance);

        final Solver solver = new Solver(stopper);
        final ErrorScorer scorer = new ErrorScorer(offsetVariable, growthFactorVariable, cosineMuVariable);
        final Problem problem = ProblemFactory.getInverseSquareMinimizerProblem(variables, scorer, errorTolerance);
        final InitialDelta initialRange = new InitialDelta();
        initialRange.addInitialDelta(offsetVariable, offsetSigma);
        initialRange.addInitialDelta(growthFactorVariable, growthFactorSigma);
        initialRange.addInitialDelta(cosineMuVariable, cosMuSigma);
        problem.addHint(initialRange);
        solver.solve(problem);

        final TrialPoint solution = solver.getScoreBoard().getBestSolution().getTrialPoint();
        this.offset = solution.getValue(offsetVariable);
        this.growthFactor = solution.getValue(growthFactorVariable);
        growthRate = Math.log(newGrowthFactor);
        cosineMu = solution.getValue(cosineMuVariable);
        frequency = 0.5 * Math.acos(cosineMu) / Math.PI;

        waveformError = calculateLeastWaveformError(this.offset, cosineMu, newGrowthFactor);
        final double signalSigma = calculateRMSError(waveformError);
        this.signalVariance = signalSigma * signalSigma;
        final double[] zeroedWaveform = new double[numPoints];
        for (int index = 0; index < numPoints; index++) {
            zeroedWaveform[index] = waveform[index] - this.offset;
        }

        final double mu = frequency * 2.0 * Math.PI;
        fitPhaseAndAmplitude(newGrowthFactor, mu, zeroedWaveform, waveformError);
    }

    /**
     * get the fitted offset calculating it if necessary
     */
    public double getInitialOffset() {
        if (!initialOffsetCalculated) {
            fitInitialOffset();
        }

        return initialOffset.getValue();
    }

    /**
     * get the variance in the initial offset estimate using the initial
     * estimate of the signal variance
     */
    public double getInitialOffsetVariance() {
        if (!initialOffsetCalculated) {
            fitInitialOffset();
        }

        return initialOffset.varianceWithSignalVariance(initialSignalVariance);
    }

    /**
     * get the initial estimate of the signal variance
     */
    public double getInitialSignalVariance() {
        if (!initialOffsetCalculated) {
            fitInitialOffset();
        }

        return initialSignalVariance;
    }

    /**
     * get an estimate of the initial offset's variance
     */
    public double estimateInitialOffsetVariance(final double signalVariance) {
        if (!initialOffsetCalculated) {
            fitInitialOffset();
        }

        return initialOffset.varianceWithSignalVariance(signalVariance);
    }

    /**
     * get the optimized offset
     */
    public double getOffset() {
        return offset;
    }

    /**
     * get the optimized estimate of the signal variance
     */
    public double getSignalVariance() {
        return signalVariance;
    }

    /**
     * get the initial growth factor
     */
    private double getInitialGrowthFactor() {
        if (!initialGrowthRateCalculated) {
            fitInitialGrowthRate();
        }

        return initialGrowthFactor.getValue();
    }

    /**
     * get the fitted growth rate calculating it if necessary
     */
    public double getInitialGrowthRate() {
        if (!initialGrowthRateCalculated) {
            fitInitialGrowthRate();
        }

        return initialGrowthRate.getValue();
    }

    /**
     * get the variance in the initial growth rate estimate using the initial
     * estimate of the signal variance
     */
    public double getInitialGrowthRateVariance() {
        if (!initialGrowthRateCalculated) {
            fitInitialGrowthRate();
        }

        return initialGrowthRate.varianceWithSignalVariance(initialSignalVariance);
    }

    /**
     * get an estimate of the initial growth rate's variance
     */
    public double estimateInitialGrowthRateVariance(final double signalVariance) {
        if (!initialGrowthRateCalculated) {
            fitInitialGrowthRate();
        }

        return initialGrowthRate.varianceWithSignalVariance(signalVariance);
    }

    /**
     * get the initial waveform error calculating it if necessary
     */
    public double[] getInitialWaveformError() {
        if (!initialWaveformErrorCalculated) {
            fitInitialWaveformError();
        }

        return initialWaveformError;
    }

    /**
     * get the initial RMS error
     */
    public double getInitialWaveformRMSError() {
        return calculateRMSError(getInitialWaveformError());
    }

    /**
     * get the optimized growth rate
     */
    public double getGrowthRate() {
        return growthRate;
    }

    /**
     * get the initial fitted cosine of the angular frequency calculating it if
     * necessary
     */
    private double getInitialCosineMu() {
        if (!initialFrequencyCalculated) {
            fitInitialFrequency();
        }

        return initialCosineMu.getValue();
    }

    /**
     * get the fitted frequency calculating it if necessary
     */
    public double getInitialFrequency() {
        if (!initialFrequencyCalculated) {
            fitInitialFrequency();
        }

        return initialFrequency.getValue();
    }

    /**
     * get the variance in the initial frequency estimate using the initial
     * estimate of the signal variance
     */
    public double getInitialFrequencyVariance() {
        if (!initialFrequencyCalculated) {
            fitInitialFrequency();
        }

        return initialFrequency.varianceWithSignalVariance(initialSignalVariance);
    }

    /**
     * get an estimate of the initial frequency's variance
     */
    public double estimateInitialFrequencyVariance(final double signalVariance) {
        if (!initialFrequencyCalculated) {
            fitInitialFrequency();
        }

        return initialFrequency.varianceWithSignalVariance(signalVariance);
    }

    /**
     * get the optimized frequency
     */
    public double getFrequency() {
        return frequency;
    }

    /**
     * Get the sine-like phase estimation calculating it if necessary. Note that
     * this estimation is relatively poor.
     */
    public double getInitialPhase() {
        if (!initialPhaseCalculated) {
            fitInitialPhaseAndAmplitude();
        }

        return initialPhase;
    }

    /**
     * get the optimized phase
     */
    public double getPhase() {
        return phase;
    }

    /**
     * Get the optimized sine-like phase.
     */
    public double getSineLikePhase() {
        return getPhase();
    }

    /**
     * Get the optimized cosine-like phase (equivalent phase if the fitted
     * equation were of the form of A * damping * cos( mu + phase ) ).
     */
    public double getCosineLikePhase() {
        return toCosineLikePhase(getPhase());
    }

    /**
     * get the waveform error
     */
    public double[] getWaveformError() {
        return waveformError;
    }

    /**
     * get the initial RMS error
     */
    public double getWaveformRMSError() {
        return calculateRMSError(getWaveformError());
    }

    /**
     * Get the sine-like phase calculating it if necessary. Note that this
     * estimation is relatively poor.
     */
    public double getInitialSineLikePhase() {
        return getInitialPhase();
    }

    /**
     * Get the cosine-like phase calculating it if necessary. Note that this
     * estimation is relatively poor.
     */
    public double getInitialCosineLikePhase() {
        return toCosineLikePhase(getInitialPhase());
    }

    /**
     * Convert a sine like phase (default) to a cosine like phase (equivalent
     * phase if the fitted equation were of the form of A * damping * cos( mu +
     * phase ))
     */
    private double toCosineLikePhase(final double sineLikePhase) {
        // shift by pi/2
        final double rawCosinePhase = sineLikePhase - Math.PI / 2.0;
        // force the phase to be between -pi and pi
        return rawCosinePhase < -Math.PI ? rawCosinePhase + 2 * Math.PI : rawCosinePhase;
    }

    /**
     * Get the sine-like amplitude calculating it if necessary. Note that this
     * estimation is relatively poor.
     */
    public double getInitialAmplitude() {
        if (!initialAmplitudeCalculated) {
            fitInitialPhaseAndAmplitude();
        }

        return initialAmplitude;
    }

    /**
     * Get the optimized sine-like amplitude
     */
    public double getAmplitude() {
        return amplitude;
    }

    /**
     * calculate the constant offset
     */
    private void fitInitialOffset() {
        final int wcount = numPoints - 2;
        final DifferentialVariable[] w = new DifferentialVariable[wcount];
        final DifferentialVariable[] z = new DifferentialVariable[wcount];
        for (int index = 0; index < wcount; index++) {
            final double q0 = waveform[index];
            final double q1 = waveform[index + 1];
            final double q2 = waveform[index + 2];
            w[index] = new DifferentialVariable(q0 * q2 - q1 * q1, index, q2, -2.0 * q1, q0);
            z[index] = new DifferentialVariable(2 * q1 - q0 - q2, index, -1.0, 2.0, -1.0);
        }

        final int rcount = numPoints - 4;
        final DifferentialVariable[] r = new DifferentialVariable[rcount];
        final DifferentialVariable[] s = new DifferentialVariable[rcount];
        final DifferentialVariable[] t = new DifferentialVariable[rcount];
        for (int index = 0; index < rcount; index++) {
            final DifferentialVariable z0 = z[index];
            final DifferentialVariable z1 = z[index + 1];
            final DifferentialVariable z2 = z[index + 2];
            final DifferentialVariable w0 = w[index];
            final DifferentialVariable w1 = w[index + 1];
            final DifferentialVariable w2 = w[index + 2];
            r[index] = z1.pow(2.0).minus(z0.times(z2));
            s[index] = z1.times(w1).times(2.0).minus(z0.times(w2)).minus(z2.times(w0));
            t[index] = w1.pow(2.0).minus(w0.times(w2));
        }

        final int count = numPoints - 5;

        DifferentialVariable offsetSum = DifferentialVariable.ZERO;
        DifferentialVariable[] offsetEstimates = new DifferentialVariable[count];
        double totalWeight = 0.0;
        for (int index = 0; index < count; index++) {
            final DifferentialVariable r0 = r[index];
            final DifferentialVariable r1 = r[index + 1];
            final DifferentialVariable s0 = s[index];
            final DifferentialVariable s1 = s[index + 1];
            final DifferentialVariable t0 = t[index];
            final DifferentialVariable t1 = t[index + 1];

            final DifferentialVariable numerator = r0.times(t1).minus(r1.times(t0));
            final DifferentialVariable denominator = r1.times(s0).minus(r0.times(s1));
            final DifferentialVariable offsetEstimate = numerator.over(denominator);
            offsetEstimates[index] = offsetEstimate;

            // signal variance must be one since we really want the sum of square of first partials
            final double weight = 1.0 / offsetEstimate.varianceWithSignalVariance(1.0);
            final DifferentialVariable weightedOffset = offsetEstimate.times(weight);
            offsetSum = offsetSum.plus(weightedOffset);
            totalWeight += weight;
        }

        initialOffset = offsetSum.over(totalWeight);

        initialZeroedWaveform = new DifferentialVariable[numPoints];
        final DifferentialVariable negativeOffset = initialOffset.negate();
        for (int index = 0; index < numPoints; index++) {
            initialZeroedWaveform[index] = new DifferentialVariable(waveform[index], index, 1.0).plus(negativeOffset);
        }

        final double offsetValue = initialOffset.getValue();

        // calculate the estimated mean signal variance
        double penaltySum = 0.0;
        for (final DifferentialVariable offsetEstimate : offsetEstimates) {
            final double error = offsetValue - offsetEstimate.getValue();
            penaltySum += error * error / offsetEstimate.varianceWithSignalVariance(1.0);
        }
        initialSignalVariance = penaltySum / count;

        initialOffsetCalculated = true;
    }

    /**
     * fit the growth rate to the waveform
     */
    private void fitInitialGrowthRate() {
        // make sure the zeroed waveform is calculated
        getInitialOffset();

        final int count = numPoints - 3;
        DifferentialVariable growthFactorSquareSum = DifferentialVariable.ZERO;
        double totalWeight = 0.0;
        for (int index = 0; index < count; index++) {
            final DifferentialVariable q0 = initialZeroedWaveform[index];
            final DifferentialVariable q1 = initialZeroedWaveform[index + 1];
            final DifferentialVariable q2 = initialZeroedWaveform[index + 2];
            final DifferentialVariable q3 = initialZeroedWaveform[index + 3];

            final DifferentialVariable numerator = q1.times(q3).minus(q2.pow(2));
            final DifferentialVariable denominator = q0.times(q2).minus(q1.pow(2));
            final DifferentialVariable growthFactorSquareEstimate = numerator.over(denominator);
            // exclude points where the growth factor is negative
            if (growthFactorSquareEstimate.getValue() >= 0.0) {
                final double weight = 1.0 / growthFactorSquareEstimate.varianceWithSignalVariance(1.0);
                growthFactorSquareSum = growthFactorSquareSum.plus(growthFactorSquareEstimate.times(weight));
                totalWeight += weight;
            }
        }

        initialGrowthFactor = growthFactorSquareSum.over(totalWeight).sqrt();

        initialGrowthRate = initialGrowthFactor.log();
        initialGrowthRateCalculated = true;
    }

    /**
     * fit the frequency to the waveform
     */
    private void fitInitialFrequency() {
        getInitialGrowthFactor();
        final DifferentialVariable reciprocolGrowthFactor = initialGrowthFactor.reciprocal();
        final double count = numPoints - 2;

        DifferentialVariable cosMuSum = DifferentialVariable.ZERO;
        double totalWeight = 0.0;
        for (int index = 0; index < count; index++) {
            final DifferentialVariable q0 = initialZeroedWaveform[index];
            final DifferentialVariable q1 = initialZeroedWaveform[index + 1];
            final DifferentialVariable q2 = initialZeroedWaveform[index + 2];

            // exclude points where the denominator goes to zero
            if (q1.getValue() != 0.0) {
                final DifferentialVariable numerator = q0.times(initialGrowthFactor).plus(q2.times(reciprocolGrowthFactor));
                final DifferentialVariable estimate = numerator.over(q1);
                final DifferentialVariable cosMuEstimate = estimate.times(0.5);
                final double weight = 1.0 / cosMuEstimate.varianceWithSignalVariance(1.0);
                cosMuSum = cosMuSum.plus(cosMuEstimate.times(weight));
                totalWeight += weight;
            }
        }

        final DifferentialVariable cosMu = cosMuSum.over(totalWeight);
        final double cosMuValue = cosMu.getValue();
        // restrict the frequency to the real part of the arc cosine
        final DifferentialVariable mu = cosMuValue > 1.0 ? DifferentialVariable.ZERO : cosMuValue < -1.0 ? DifferentialVariable.newConstant(Math.PI) : cosMu.acos();

        initialCosineMu = cosMu;
        initialFrequency = mu.over(2.0 * Math.PI);
        initialFrequencyCalculated = true;
    }

    /**
     * fit the phase and amplitude
     */
    private void fitPhaseAndAmplitude(final double growthFactor, final double mu, final double[] zeroedWaveform, final double[] waveformErrors) {
        final double q0 = zeroedWaveform[0] + waveformErrors[0];
        final double q1 = (zeroedWaveform[1] + waveformErrors[1]) / growthFactor;

        final double sinMu = Math.sin(mu);
        // avoid catastrophe by setting the sin amplitude to zero near the integer and half integer frequencies
        final boolean isZeroSineMu = Math.abs(sinMu) < 0.0001;
        final double cosAmp = isZeroSineMu ? 0.0 : (q1 - q0 * Math.cos(mu)) / Math.sin(mu);
        final double sinAmp = q0;

        amplitude = Math.sqrt(cosAmp * cosAmp + sinAmp * sinAmp);
        phase = Math.atan2(sinAmp, cosAmp);
    }

    /**
     * fit the frequency to the waveform
     */
    private void fitInitialPhaseAndAmplitude() {
        getInitialGrowthFactor();
        getInitialFrequency();

        final DifferentialVariable reciprocolGrowthFactor = initialGrowthFactor.reciprocal();
        final DifferentialVariable tune = initialFrequency;
        final DifferentialVariable mu = tune.times(2.0 * Math.PI);
        final DifferentialVariable sinMu = mu.sin();

        final double count = numPoints - 1.;

        DifferentialVariable sinAmpSum = DifferentialVariable.ZERO;
        DifferentialVariable cosAmpSum = DifferentialVariable.ZERO;
        double totalCosWeight = 0.0;
        double totalSinWeight = 0.0;
        DifferentialVariable growth = DifferentialVariable.newConstant(1.0);
        // sine is too close to zero, so need to use approximation about it to avoid catastrophe
        final boolean isZeroSineMu = Math.abs(sinMu.getValue()) < 0.0001;
        for (int turn = 0; turn < count; turn++) {
            final DifferentialVariable q0 = initialZeroedWaveform[turn].times(growth);
            final DifferentialVariable q1 = initialZeroedWaveform[turn + 1].times(growth).times(reciprocolGrowthFactor);

            final DifferentialVariable muN = mu.times(turn);
            final DifferentialVariable muNP1 = mu.times(turn + 1);
            final DifferentialVariable cosMuN = muN.cos();
            final DifferentialVariable cosMuNP1 = muNP1.cos();
            final DifferentialVariable sinMuN = muN.sin();

            final DifferentialVariable cosAmpEstimate = isZeroSineMu ? DifferentialVariable.ZERO : (q1.times(cosMuN).minus(q0.times(cosMuNP1))).over(sinMu);
            final double cosWeight = isZeroSineMu ? 1.0 : 1.0 / cosAmpEstimate.varianceWithSignalVariance(1.0);
            cosAmpSum = cosAmpSum.plus(cosAmpEstimate.times(cosWeight));
            totalCosWeight += cosWeight;

            final DifferentialVariable sinAmpEstimate = isZeroSineMu ? q0.over(cosMuN) : q0.minus(cosAmpEstimate.times(sinMuN)).over(cosMuN);
            final double sinWeight = 1.0 / sinAmpEstimate.varianceWithSignalVariance(1.0);
            sinAmpSum = sinAmpSum.plus(sinAmpEstimate.times(sinWeight));
            totalSinWeight += sinWeight;

            growth = growth.times(reciprocolGrowthFactor);
        }

        final double cosAmp = cosAmpSum.over(totalCosWeight).getValue();
        final double sinAmp = sinAmpSum.over(totalSinWeight).getValue();

        initialAmplitude = Math.sqrt(cosAmp * cosAmp + sinAmp * sinAmp);
        initialPhase = Math.atan2(sinAmp, cosAmp);

        initialAmplitudeCalculated = true;
        initialPhaseCalculated = true;
    }

    /**
     * Convenience method to calculate the fitted waveform over the specified
     * positions
     *
     * @param positions array of positions over which to calculate the waveform
     * @return array holding the calculated waveform over each of the positions
     */
    public double[] getFittedWaveform(final double[] positions) {
        final double[] newWaveform = new double[positions.length];
        calculateFittedWaveform(positions, newWaveform);
        return newWaveform;
    }

    /**
     * Convenience method to calculate the fitted waveform over the specified
     * positions
     *
     * @param positions array of positions over which to calculate the waveform
     * @param waveform big enough to hold the calculated waveform over each of
     * the positions
     */
    public void calculateFittedWaveform(final double[] positions, double[] waveform) {
        final double newOffset = Double.isNaN(this.offset) ? getInitialOffset() : this.offset;
        final double newGrowthFactor = Double.isNaN(this.growthFactor) ? getInitialGrowthFactor() : this.growthFactor;
        final double newFrequency = Double.isNaN(this.frequency) ? getInitialFrequency() : this.frequency;
        final double newAmplitude = Double.isNaN(this.amplitude) ? getInitialAmplitude() : this.amplitude;
        final double newPhase = Double.isNaN(this.phase) ? getInitialPhase() : this.phase;

        for (int index = 0; index < waveform.length; index++) {
            final double position = positions[index];
            waveform[index] = newAmplitude * Math.pow(newGrowthFactor, position) * Math.sin(2 * Math.PI * newFrequency * position + newPhase) + newOffset;
        }
    }
}
