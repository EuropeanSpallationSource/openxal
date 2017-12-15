/*
 * Copyright (C) 2017 European Spallation Source ERIC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package xal.extension.jels.tools.math;

/**
 * @author Ivo List
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class MeanFieldPolynomial extends InverseRealPolynomial {

    private final InverseRealPolynomial TTF;
    private final InverseRealPolynomial TTFPrime;

    public MeanFieldPolynomial(InverseRealPolynomial TTF, InverseRealPolynomial TTFPrime) {
        this.TTF = TTF;
        this.TTFPrime = TTFPrime;
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
