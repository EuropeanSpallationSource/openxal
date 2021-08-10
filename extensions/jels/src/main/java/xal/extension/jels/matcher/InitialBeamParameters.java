package xal.extension.jels.matcher;

import java.util.Arrays;
import java.util.List;

import xal.extension.solver.TrialPoint;
import xal.extension.solver.Variable;
import xal.model.probe.EnvelopeProbe;
import xal.tools.annotation.AProperty.NoEdit;
import xal.tools.beam.Twiss;
import xal.tools.beam.Twiss.PROP;

public class InitialBeamParameters {

    enum IND {
        X(0, "x"), Y(1, "y"), Z(2, "z");

        private int i;
        private String name;

        IND(int i, String name) {
            this.i = i;
            this.name = name;
        }

        public int getIndex() {
            return i;
        }

        public String getAxis() {
            return name;
        }
    }

    class TwissVariable extends Variable {

        private IND ind;
        private PROP prop;

        public TwissVariable(IND ind, PROP prop, double lowerLimit, double upperLimit) {
            super(prop.getPropertyLabel() + ind.name, 0., lowerLimit, upperLimit);
            this.ind = ind;
            this.prop = prop;
        }

        @Override
        @NoEdit
        public double getInitialValue() {
            return prop.getPropertyValue(probe.getCovariance().computeTwiss()[ind.i]);
        }

        @Override
        public void setInitialValue(double initialValue) {
            throw new UnsupportedOperationException();
        }

    }

    final TwissVariable ax;
    final TwissVariable bx;
    final TwissVariable ay;
    final TwissVariable by;
    final TwissVariable az;
    final TwissVariable bz;
    final List<Variable> variables;

    final TwissVariable ex;
    final TwissVariable ey;
    final TwissVariable ez;

    private EnvelopeProbe probe;

    public InitialBeamParameters(EnvelopeProbe probe) {
        this.probe = probe;

        ax = new TwissVariable(IND.X, PROP.ALPHA, -1, 1);
        bx = new TwissVariable(IND.X, PROP.BETA, -1, 1);
        ay = new TwissVariable(IND.Y, PROP.ALPHA, -1, 1);
        by = new TwissVariable(IND.Y, PROP.BETA, -1, 1);
        az = new TwissVariable(IND.Z, PROP.ALPHA, -1, 1);
        bz = new TwissVariable(IND.Z, PROP.BETA, -1, 1);
        variables = Arrays.<Variable>asList(ax, bx, ay, by, az, bz);

        ex = new TwissVariable(IND.X, PROP.EMIT, -1, 1);
        ey = new TwissVariable(IND.Y, PROP.EMIT, -1, 1);
        ez = new TwissVariable(IND.Z, PROP.EMIT, -1, 1);

    }

    public EnvelopeProbe getInitialProbe() {
        return probe;
    }

    public void setInitialProbe(EnvelopeProbe probe) {
        this.probe.getCovariance().setMatrix(probe.getCovariance().getArrayCopy());
    }

    public EnvelopeProbe getProbe(TrialPoint trialPoint) {
        EnvelopeProbe probeCopy = this.probe.copy();

        double aX = trialPoint.getValue(ax);
        double bX = trialPoint.getValue(bx);
        double aY = trialPoint.getValue(ay);
        double bY = trialPoint.getValue(by);
        double aZ = trialPoint.getValue(az);
        double bZ = trialPoint.getValue(bz);

        probeCopy.initFromTwiss(new Twiss[]{new Twiss(aX, bX, ex.getInitialValue()),
            new Twiss(aY, bY, ey.getInitialValue()),
            new Twiss(aZ, bZ, ez.getInitialValue())});

        return probeCopy;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public Variable getAx() {
        return ax;
    }

    public Variable getBx() {
        return bx;
    }

    public Variable getAy() {
        return ay;
    }

    public Variable getBy() {
        return by;
    }

    public Variable getAz() {
        return az;
    }

    public Variable getBz() {
        return bz;
    }
}
