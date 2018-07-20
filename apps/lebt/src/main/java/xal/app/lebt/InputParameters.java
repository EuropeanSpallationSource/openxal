/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lebt;

import java.util.logging.Level;
import java.util.logging.Logger;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.extension.jels.smf.impl.EMU;
import xal.extension.jels.smf.impl.NPM;
import xal.model.probe.EnvelopeProbe;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;

/**
 *
 * @author nataliamilas
 */
public class InputParameters {

    protected double X;

    protected double XP;

    protected double Y;

    protected double YP;

    protected double BETAX;

    protected double ALPHAX;

    protected double BETAY;

    protected double ALPHAY;

    protected double EMITTX;

    protected double EMITTY;

    protected Object nodeName;

    protected double _beta_gamma = 0.013;

    public InputParameters(Object nodeName) {
        this.nodeName = nodeName;
        this.ALPHAX = 0.0;
        this.ALPHAY = 0.0;
        this.BETAX = 0.0;
        this.BETAY = 0.0;
        this.EMITTY = 0.0;
        this.EMITTX = 0.0;
        this.X = 0.0;
        this.XP = 0.0;
        this.Y = 0.0;
        this.YP = 0.0;

        if(nodeName instanceof NPM){
            try {
                if(((NPM) nodeName).getChannel(NPM.ALPHA_X_TWISS_HANDLE).isConnected()){
                    this.ALPHAX = ((NPM) nodeName).getXAlphaTwiss();
                }
                if(((NPM) nodeName).getChannel(NPM.ALPHA_Y_TWISS_HANDLE).isConnected()){
                    this.ALPHAY = ((NPM) nodeName).getYAlphaTwiss();
                }
                if(((NPM) nodeName).getChannel(NPM.BETA_X_TWISS_HANDLE).isConnected()){
                    this.BETAX = ((NPM) nodeName).getXBetaTwiss();
                    if(((NPM) nodeName).getChannel(NPM.SIGMA_X_AVG_HANDLE).isConnected()){
                        this.EMITTX = Math.pow(((NPM) nodeName).getXSigmaAvg(),2)/BETAX*_beta_gamma;
                    }
                }
                if(((NPM) nodeName).getChannel(NPM.BETA_Y_TWISS_HANDLE).isConnected()){
                    this.BETAY = ((NPM) nodeName).getYBetaTwiss();
                    if(((NPM) nodeName).getChannel(NPM.SIGMA_Y_AVG_HANDLE).isConnected()){
                        this.EMITTY = Math.pow(((NPM) nodeName).getYSigmaAvg(),2)/BETAY*_beta_gamma;
                    }
                }
                if(((NPM) nodeName).getChannel(NPM.X_AVG_HANDLE).isConnected()){
                    this.X = ((NPM) nodeName).getXAvg()*1e-03;
                }
                if(((NPM) nodeName).getChannel(NPM.X_P_AVG_HANDLE).isConnected()){
                    this.XP = ((NPM) nodeName).getXpAvg();
                }
                if(((NPM) nodeName).getChannel(NPM.Y_AVG_HANDLE).isConnected()){
                    this.Y = ((NPM) nodeName).getYAvg()*1e-03;
                }
                if(((NPM) nodeName).getChannel(NPM.Y_P_AVG_HANDLE).isConnected()){
                    this.YP = ((NPM) nodeName).getYpAvg();
                }
            } catch (ConnectionException | GetException ex) {
                Logger.getLogger(InputParameters.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if(nodeName instanceof EMU){
            try {
                if(((EMU) nodeName).getChannel(EMU.EMITT_X_HANDLE).isConnected()){
                    this.EMITTX = ((EMU) nodeName).getXEmittance()*_beta_gamma;
                }
                if(((EMU) nodeName).getChannel(EMU.EMITT_Y_HANDLE).isConnected()){
                    this.EMITTY = ((EMU) nodeName).getYEmittance()*_beta_gamma;
                }
                if(((EMU) nodeName).getChannel(EMU.ALPHA_X_TWISS_HANDLE).isConnected()){
                    this.ALPHAX = ((EMU) nodeName).getXAlphaTwiss();
                }
                if(((EMU) nodeName).getChannel(EMU.ALPHA_Y_TWISS_HANDLE).isConnected()){
                    this.ALPHAY = ((EMU) nodeName).getYAlphaTwiss();
                }
                if(((EMU) nodeName).getChannel(EMU.BETA_X_TWISS_HANDLE).isConnected()){
                    this.BETAX = ((EMU) nodeName).getXBetaTwiss();
                }
                if(((EMU) nodeName).getChannel(EMU.BETA_Y_TWISS_HANDLE).isConnected()){
                    this.BETAY = ((EMU) nodeName).getYBetaTwiss();
                }
            } catch (ConnectionException | GetException ex) {
                Logger.getLogger(InputParameters.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if(nodeName instanceof EnvelopeProbe){

            double beta_gamma = ((EnvelopeProbe) nodeName).getGamma()*((EnvelopeProbe) nodeName).getBeta();
            Twiss[] iniTwiss = ((EnvelopeProbe) nodeName).getCovariance().computeTwiss();
            this.ALPHAX = iniTwiss[0].getAlpha();
            this.BETAX =  iniTwiss[0].getBeta();
            this.EMITTX =  iniTwiss[0].getEmittance()*beta_gamma;

            this.ALPHAY = iniTwiss[1].getAlpha();
            this.BETAY =  iniTwiss[1].getBeta();
            this.EMITTY =  iniTwiss[1].getEmittance()*beta_gamma;

            PhaseVector iniPos = ((EnvelopeProbe) nodeName).getCovariance().getMean();

            this.X = iniPos.getx();
            this.XP = iniPos.getxp();
            this.Y = iniPos.gety();
            this.YP = iniPos.getyp();
        }

    }

    public String getName(){
        if(nodeName instanceof EnvelopeProbe){
            return ((EnvelopeProbe) nodeName).getCurrentElement();
        } else {
            return nodeName.toString();
        }
    }

    public double[] getInit(){
        double[] initPos = new double[4];
        initPos[0] = this.getX();
        initPos[1] = this.getXP();
        initPos[2] = this.getY();
        initPos[3] = this.getYP();

        return initPos;
    }

    public double[] getTwissX(){
        double[] TwissX = new double[3];
        TwissX[0] = this.getALPHAX();
        TwissX[1] = this.getBETAX();
        TwissX[2] = this.getEMITTX();

        return TwissX;
    }

    public double[] getTwissY(){
        double[] TwissY = new double[3];
        TwissY[0] = this.getALPHAY();
        TwissY[1] = this.getBETAY();
        TwissY[2] = this.getEMITTY();

        return TwissY;
    }

    public void setInit(double[] initPos){
        this.setX(initPos[0]);
        this.setXP(initPos[1]);
        this.setY(initPos[2]);
        this.setYP(initPos[3]);

    }

    public void setTwissX(double[] TwissX){
        this.setALPHAX(TwissX[0]);
        this.setBETAX(TwissX[1]);
        this.setEMITTX(TwissX[2]);

    }

    public void setTwissY(double[] TwissY){
        this.setALPHAY(TwissY[0]);
        this.setBETAY(TwissY[1]);
        this.setEMITTY(TwissY[2]);

    }

    public void updateValues(){
        if(nodeName instanceof NPM){
            try {
                if(((NPM) nodeName).getChannel(NPM.ALPHA_X_TWISS_HANDLE).isConnected()){
                    this.ALPHAX = ((NPM) nodeName).getXAlphaTwiss();
                }
                if(((NPM) nodeName).getChannel(NPM.ALPHA_Y_TWISS_HANDLE).isConnected()){
                    this.ALPHAY = ((NPM) nodeName).getYAlphaTwiss();
                }
                if(((NPM) nodeName).getChannel(NPM.BETA_X_TWISS_HANDLE).isConnected()){
                    this.BETAX = ((NPM) nodeName).getXBetaTwiss();
                    if(((NPM) nodeName).getChannel(NPM.SIGMA_X_AVG_HANDLE).isConnected()){
                        this.EMITTX = Math.pow(((NPM) nodeName).getXSigmaAvg(),2)/BETAX*_beta_gamma;
                    }
                }
                if(((NPM) nodeName).getChannel(NPM.BETA_Y_TWISS_HANDLE).isConnected()){
                    this.BETAY = ((NPM) nodeName).getYBetaTwiss();
                    if(((NPM) nodeName).getChannel(NPM.SIGMA_Y_AVG_HANDLE).isConnected()){
                        this.EMITTY = Math.pow(((NPM) nodeName).getYSigmaAvg(),2)/BETAY*_beta_gamma;
                    }
                }
                if(((NPM) nodeName).getChannel(NPM.X_AVG_HANDLE).isConnected()){
                    this.X = ((NPM) nodeName).getXAvg()*1e-03;
                }
                if(((NPM) nodeName).getChannel(NPM.X_P_AVG_HANDLE).isConnected()){
                    this.XP = ((NPM) nodeName).getXpAvg();
                }
                if(((NPM) nodeName).getChannel(NPM.Y_AVG_HANDLE).isConnected()){
                    this.Y = ((NPM) nodeName).getYAvg()*1e-03;
                }
                if(((NPM) nodeName).getChannel(NPM.Y_P_AVG_HANDLE).isConnected()){
                    this.YP = ((NPM) nodeName).getYpAvg();
                }
            } catch (ConnectionException | GetException ex) {
                Logger.getLogger(InputParameters.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if(nodeName instanceof EMU){
            try {
                if(((EMU) nodeName).getChannel(EMU.EMITT_X_HANDLE).isConnected()){
                    this.EMITTX = ((EMU) nodeName).getXEmittance()*_beta_gamma;
                }
                if(((EMU) nodeName).getChannel(EMU.EMITT_Y_HANDLE).isConnected()){
                    this.EMITTY = ((EMU) nodeName).getYEmittance()*_beta_gamma;
                }
                if(((EMU) nodeName).getChannel(EMU.ALPHA_X_TWISS_HANDLE).isConnected()){
                    this.ALPHAX = ((EMU) nodeName).getXAlphaTwiss();
                }
                if(((EMU) nodeName).getChannel(EMU.ALPHA_Y_TWISS_HANDLE).isConnected()){
                    this.ALPHAY = ((EMU) nodeName).getYAlphaTwiss();
                }
                if(((EMU) nodeName).getChannel(EMU.BETA_X_TWISS_HANDLE).isConnected()){
                    this.BETAX = ((EMU) nodeName).getXBetaTwiss();
                }
                if(((EMU) nodeName).getChannel(EMU.BETA_Y_TWISS_HANDLE).isConnected()){
                    this.BETAY = ((EMU) nodeName).getYBetaTwiss();
                }
            } catch (ConnectionException | GetException ex) {
                Logger.getLogger(InputParameters.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public double getX() {
        return X;
    }

    public void setX(double X) {
        this.X = X;
    }

    public double getXP() {
        return XP;
    }

    public void setXP(double XP) {
        this.XP = XP;
    }

    public double getY() {
        return Y;
    }

    public void setY(double Y) {
        this.Y = Y;
    }

    public double getYP() {
        return YP;
    }

    public void setYP(double YP) {
        this.YP = YP;
    }

    public double getBETAX() {
        return BETAX;
    }

    public void setBETAX(double BETAX) {
        this.BETAX = BETAX;
    }

    public double getALPHAX() {
        return ALPHAX;
    }

    public void setALPHAX(double ALPHAX) {
        this.ALPHAX = ALPHAX;
    }

    public double getBETAY() {
        return BETAY;
    }

    public void setBETAY(double BETAY) {
        this.BETAY = BETAY;
    }

    public double getALPHAY() {
        return ALPHAY;
    }

    public void setALPHAY(double ALPHAY) {
        this.ALPHAY = ALPHAY;
    }

    public double getEMITTX() {
        return EMITTX;
    }

    public void setEMITTX(double EMITTX) {
        this.EMITTX = EMITTX;
    }

    public double getEMITTY() {
        return EMITTY;
    }

    public void setEMITTY(double EMITTY) {
        this.EMITTY = EMITTY;
    }

}
