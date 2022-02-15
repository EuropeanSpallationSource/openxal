package xal.model.elem;

import xal.model.elem.sync.IElectromagnet;
import xal.sim.scenario.LatticeElement;
import xal.smf.impl.Magnet;

/**
 * This class implements IElectromagnet interface for composed magnets.
 *
 */
public abstract class ElectromagnetSeq extends ElementSeq implements IElectromagnet {

    protected ElectromagnetSeq(String strType, String strId, int szReserve) {
        super(strType, strId, szReserve);
    }

    protected ElectromagnetSeq(String strType, String strId) {
        super(strType, strId);
    }

    protected ElectromagnetSeq(String strType) {
        super(strType);
    }

    /**
     * Conversion method to be provided by the user
     *
     * @param latticeElement the SMF node to convert
     */
    @Override
    public void initializeFrom(LatticeElement latticeElement) {
        super.initializeFrom(latticeElement);
        Magnet magnetNode = (Magnet) latticeElement.getHardwareNode();
        int orientation = IElectromagnet.ORIENT_NONE;
        if (magnetNode.isHorizontal()) {
            orientation = IElectromagnet.ORIENT_HOR;
        } else if (magnetNode.isVertical()) {
            orientation = IElectromagnet.ORIENT_VER;
        }
        setOrientation(orientation);
        setMagField(magnetNode.getDesignField());
    }

}
