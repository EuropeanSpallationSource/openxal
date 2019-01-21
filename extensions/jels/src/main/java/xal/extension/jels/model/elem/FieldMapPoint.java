/*
 * Copyright (C) 2019 European Spallation Source ERIC.
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
package xal.extension.jels.model.elem;

/**
 * A FieldMapPoint object is normally returned by a
 * {@link xal.extension.jels.smf.impl.FieldMap} object and contains the values
 * of the electric and magnetic fields at a given point, together with the value
 * of the derivative of the fields.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 */
public class FieldMapPoint {

    private double amplitudeFactorE = 1.0;
    private double amplitudeFactorB = 1.0;

    // Electric field
    private double Ex = 0;
    private double Ey = 0;
    private double Ez = 0;

    // Derivatives of Ex
    private double dExdx = 0;
    private double dExdy = 0;
    private double dExdz = 0;

    // Derivatives of Ey
    private double dEydx = 0;
    private double dEydy = 0;
    private double dEydz = 0;

    // Derivatives of Ez
    private double dEzdx = 0;
    private double dEzdy = 0;
    private double dEzdz = 0;

    // Magnetic field
    private double Bx = 0;
    private double By = 0;
    private double Bz = 0;

    // Derivatives of Bx
    private double dBxdx = 0;
    private double dBxdy = 0;
    private double dBxdz = 0;

    // Derivatives of By
    private double dBydx = 0;
    private double dBydy = 0;
    private double dBydz = 0;

    public FieldMapPoint() {
    }

    public FieldMapPoint(double Ex, double Ey, double Ez, double dExdx, double dExdy, double dExdz,
            double dEydx, double dEydy, double dEydz, double dEzdx, double dEzdy,
            double dEzdz, double Bx, double By, double Bz, double dBxdx, double dBxdy,
            double dBxdz, double dBydx, double dBydy, double dBydz) {

        this.Ex = Ex;
        this.Ey = Ey;
        this.Ez = Ez;

        this.dExdx = dExdx;
        this.dExdy = dExdy;
        this.dExdz = dExdz;

        this.dEydx = dEydx;
        this.dEydy = dEydy;
        this.dEydz = dEydz;

        this.dEzdx = dEzdx;
        this.dEzdy = dEzdy;
        this.dEzdz = dEzdz;

        this.Bx = Bx;
        this.By = By;
        this.Bz = Bz;

        this.dBxdx = dBxdx;
        this.dBxdy = dBxdy;
        this.dBxdz = dBxdz;

        this.dBydx = dBydx;
        this.dBydy = dBydy;
        this.dBydz = dBydz;
    }

    public void setAmplitudeFactorE(double amplitudeFactorE) {
        this.amplitudeFactorE = amplitudeFactorE;
    }

    public void setAmplitudeFactorB(double amplitudeFactorB) {
        this.amplitudeFactorB = amplitudeFactorB;
    }

    public double getEx() {
        return Ex * amplitudeFactorE;
    }

    public double getEy() {
        return Ey * amplitudeFactorE;
    }

    public double getEz() {
        return Ez * amplitudeFactorE;
    }

    public double getdExdx() {
        return dExdx * amplitudeFactorE;
    }

    public double getdExdy() {
        return dExdy * amplitudeFactorE;
    }

    public double getdExdz() {
        return dExdz * amplitudeFactorE;
    }

    public double getdEydx() {
        return dEydx * amplitudeFactorE;
    }

    public double getdEydy() {
        return dEydy * amplitudeFactorE;
    }

    public double getdEydz() {
        return dEydz * amplitudeFactorE;
    }

    public double getdEzdx() {
        return dEzdx * amplitudeFactorE;
    }

    public double getdEzdy() {
        return dEzdy * amplitudeFactorE;
    }

    public double getdEzdz() {
        return dEzdz * amplitudeFactorE;
    }

    public double getBx() {
        return Bx * amplitudeFactorB;
    }

    public double getBy() {
        return By * amplitudeFactorB;
    }

    public double getBz() {
        return Bz * amplitudeFactorB;
    }

    public double getdBxdx() {
        return dBxdx * amplitudeFactorB;
    }

    public double getdBxdy() {
        return dBxdy * amplitudeFactorB;
    }

    public double getdBxdz() {
        return dBxdz * amplitudeFactorB;
    }

    public double getdBydx() {
        return dBydx * amplitudeFactorB;
    }

    public double getdBydy() {
        return dBydy * amplitudeFactorB;
    }

    public double getdBydz() {
        return dBydz * amplitudeFactorB;
    }

    public void setEx(double Ex) {
        this.Ex = Ex;
    }

    public void setEy(double Ey) {
        this.Ey = Ey;
    }

    public void setEz(double Ez) {
        this.Ez = Ez;
    }

    public void setdExdx(double dExdx) {
        this.dExdx = dExdx;
    }

    public void setdExdy(double dExdy) {
        this.dExdy = dExdy;
    }

    public void setdExdz(double dExdz) {
        this.dExdz = dExdz;
    }

    public void setdEydx(double dEydx) {
        this.dEydx = dEydx;
    }

    public void setdEydy(double dEydy) {
        this.dEydy = dEydy;
    }

    public void setdEydz(double dEydz) {
        this.dEydz = dEydz;
    }

    public void setdEzdx(double dEzdx) {
        this.dEzdx = dEzdx;
    }

    public void setdEzdy(double dEzdy) {
        this.dEzdy = dEzdy;
    }

    public void setdEzdz(double dEzdz) {
        this.dEzdz = dEzdz;
    }

    public void setBx(double Bx) {
        this.Bx = Bx;
    }

    public void setBy(double By) {
        this.By = By;
    }

    public void setBz(double Bz) {
        this.Bz = Bz;
    }

    public void setdBxdx(double dBxdx) {
        this.dBxdx = dBxdx;
    }

    public void setdBxdy(double dBxdy) {
        this.dBxdy = dBxdy;
    }

    public void setdBxdz(double dBxdz) {
        this.dBxdz = dBxdz;
    }

    public void setdBydx(double dBydx) {
        this.dBydx = dBydx;
    }

    public void setdBydy(double dBydy) {
        this.dBydy = dBydy;
    }

    public void setdBydz(double dBydz) {
        this.dBydz = dBydz;
    }
}
