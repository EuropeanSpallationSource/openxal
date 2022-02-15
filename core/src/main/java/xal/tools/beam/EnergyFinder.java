package xal.tools.beam;

import xal.model.IProbe;

/**
 * A class to find the energy of the beam, given information about measured
 * phase differences between BPMs We assume the BPMs report phase differences
 * from -180 to 180
 *
 * @author J. Galambos
 */
public class EnergyFinder {

    /**
     * rest mass of the beam (MeV)
     */
    private double restMass;

    /**
     * separation of the BPMs (m)
     */
    private double length;

    /**
     * the initial guess of the beam energy (MeV) we will try and find the beam
     * energy that gives the right phase, within a 2*pi interval from this
     * energy
     */
    private double energyGuess;

    /**
     * frequency of BPM phase (Hz)
     */
    private double frequency;

    /**
     * Speed of light in a vacuum (meters/second)
     */
    /**
     * error tolerance (relative)
     */
    private static final double TOL = 1.e-5;

    /**
     * max number of iterations
     */
    private static final int N_MAX = 30;

    /**
     * constructor
     *
     * @param probe for the beam
     * @param freq the BPM frequency (MHz)
     */
    public EnergyFinder(IProbe probe, double freq) {
        restMass = probe.getSpeciesRestEnergy() / 1.e6;
        frequency = freq * 1.e6;
    }

    /**
     * initialize problem specific information
     */
    public void initCalc(double l, double energy) {
        energyGuess = energy;
        length = l;
    }

    /**
     * solve the problem, using a simple linear step scheme
     *
     * @param targetPhase the phase difference between BPMs in deg
     * @param energy the starting guess for energy (MeV)
     */
    public double findEnergy(double targetPhase, double energy) {
        energyGuess = energy;
        return findEnergy(targetPhase);
    }

    /**
     * solve the problem, using a simple linear step scheme
     *
     * @param targetPhase the phase difference between BPMs in deg
     */
    public double findEnergy(double targetPhase) {
        double error;
        double errorOld;
        double eNew;
        double slope;
        double b;
        double step;
        double temp;
        int nTrys = 0;
        // solve in space -180 < phi < 180
        error = findPhase(energyGuess) - targetPhase;
        if (error < -180.) {
            error += 360.;
        }
        if (error > 180.) {
            error -= 360.;
        }
        errorOld = error;

        step = energyGuess * 0.005;
        eNew = energyGuess + step;
        while (Math.abs((error / targetPhase)) > TOL && (nTrys < N_MAX)) {
            error = findPhase(eNew) - targetPhase;
            if (error < -180.) {
                error += 360.;
            }
            if (error > 180.) {
                error -= 360.;
            }
            slope = (error - errorOld) / step;
            b = error - slope * eNew;
            temp = -b / slope;
            step = temp - eNew;
            errorOld = error;
            eNew = temp;
            nTrys++;
        }
        if (nTrys < N_MAX) {
            return eNew;
        } else {
            return -1.;
        }
    }

    /**
     * find the phase for a given energy
     */
    private double findPhase(double energy) {
        double gamma = 1 + energy / restMass;
        double beta = Math.sqrt(1.0 - 1.0 / (gamma * gamma));
        double time = length / (beta * Constants.LIGHT_SPEED);
        return ((time * frequency) % 1.) * 360.;
    }
}
