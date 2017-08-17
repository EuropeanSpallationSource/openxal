package xal.extension.jels.tools.math;

public class MeanFieldPolynomial extends InverseRealPolynomial {

    private InverseRealPolynomial TTF, TTFPrime, STF, STFPrime;

    public MeanFieldPolynomial(InverseRealPolynomial TTF, InverseRealPolynomial TTFPrime, InverseRealPolynomial STF, InverseRealPolynomial STFPrime) {
        this.TTF = TTF;
        this.TTFPrime = TTFPrime;
        this.STF = STF;
        this.STFPrime = STFPrime;
    }

    @Override
    public double evaluateAt(double dblVal) {
        return TTF.evaluateAt(dblVal);
    }

    @Override
    public double derivativeAt(double dblVal) {
        // If TTFPrime coefficients are not provided, it uses TTF
        if (TTFPrime.getCoef(0) != 0) {
            return 0.01 * TTFPrime.evaluateAt(dblVal);
        } else {
            return TTF.derivativeAt(dblVal);
        }
    }

    @Override
    public double getCoef(int iOrder) {
        return TTF.getCoef(iOrder); // TODO: return coefficients of all polynomials
    }
}
