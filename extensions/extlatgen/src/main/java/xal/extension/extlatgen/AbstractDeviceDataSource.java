//
// AbstractDeviceDataSource.java
// xal
//
// Created by Pelaia II, Tom on 7/30/12
// Copyright 2012 ORNL. All rights reserved.
//
package xal.extension.extlatgen;

import java.util.logging.Level;
import java.util.logging.Logger;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.service.pvlogger.PvLoggerException;
import xal.service.pvlogger.sim.PVLoggerDataSource;
import xal.smf.impl.Bend;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.Magnet;
import xal.tools.beam.IConstants;

/**
 * Abstract Device Data Source
 */
public abstract class AbstractDeviceDataSource {

    /**
     * singleton design data source which carries no state
     */
    private static final DesignDeviceDataSource DESIGN_DATA_SOURCE;

    /**
     * Speed of light in billions of meters per second
     */
    static final double LIGHT_SPEED = IConstants.LIGHT_SPEED / 1e9;

    // static initializer
    static {
        DESIGN_DATA_SOURCE = new DesignDeviceDataSource();
    }

    /**
     * Get the field for the specified magnet
     */
    public abstract double getField(final Magnet magnet);

    /**
     * Get this source's label
     */
    public abstract String getLabel();

    /**
     * Get the magnet's bend angle per unit element length
     */
    public abstract double getBendAnglePerLength(final Bend bend, final double unitCharge, final double momentum);

    /**
     * Get the magnet's bend angle per unit element length
     */
    public abstract double getBendEntranceAngle(final Bend bend, final double unitCharge, final double momentum);

    /**
     * Get the magnet's bend angle per unit element length
     */
    public abstract double getBendExitAngle(final Bend bend, final double unitCharge, final double momentum);

    /**
     * Get an instance of the design data source
     */
    public static DesignDeviceDataSource getDesignDataSourceInstance() {
        return DESIGN_DATA_SOURCE;
    }

    /**
     * Get an instance of the live data source
     */
    public static LiveMachineDesignRFDeviceDataSource getLiveMachineDesignRFDataSourceInstance() {
        return new LiveMachineDesignRFDeviceDataSource();
    }

    /**
     * Get the PV Logger Snapshot Data Source
     */
    public static PVLoggerSnapshotDeviceDataSource getPVLoggerDataSourceInstance(final long pvLoggerID) {
        return new PVLoggerSnapshotDeviceDataSource(pvLoggerID);
    }
}

/**
 * data source rooted in a measurement
 */
abstract class MeasurementDataSource extends AbstractDeviceDataSource {

    /**
     * Get the magnet's bend angle per unit element length
     */
    @Override
    public double getBendAnglePerLength(final Bend bend, final double unitCharge, final double momentum) {
        final double field = getField(bend);
        return unitCharge * field * LIGHT_SPEED / momentum;
    }

    /**
     * Get the magnet's bend angle per unit element length
     */
    @Override
    public double getBendEntranceAngle(final Bend bend, final double unitCharge, final double momentum) {
        final double designAngle = bend.getEntrRotAngle() * Math.PI / 180.0;
        final double designField = bend.getDesignField();
        final double field = getField(bend);
        return unitCharge * field * Math.abs(designAngle / designField);
    }

    /**
     * Get the magnet's bend angle per unit element length
     */
    @Override
    public double getBendExitAngle(final Bend bend, final double unitCharge, final double momentum) {
        final double designAngle = bend.getExitRotAngle() * Math.PI / 180.0;
        final double designField = bend.getDesignField();
        final double field = getField(bend);
        return unitCharge * field * Math.abs(designAngle / designField);
    }
}

/**
 * Device Data Source which is based in a PV Logger Snapshot
 */
class PVLoggerSnapshotDeviceDataSource extends MeasurementDataSource {

    /**
     * PVLogger Data Source
     */
    private final PVLoggerDataSource LOGGER_DATA_SOURCE;

    /**
     * Constructor
     */
    public PVLoggerSnapshotDeviceDataSource(final long pvLoggerID) {
        LOGGER_DATA_SOURCE = new PVLoggerDataSource(pvLoggerID);
    }

    /**
     * Get this source's label
     */
    @Override
    public String getLabel() {
        return "Logged Machine Design RF Lattice";
    }

    /**
     * Get the field for the specified magnet
     */
    @Override
    public double getField(final Magnet magnet) {
        if (magnet.isPermanent()) {
            return magnet.getDesignField();
        }
        try {
            return LOGGER_DATA_SOURCE.getLoggedField((Electromagnet) magnet);
        } catch (PvLoggerException e) {
            // TODO: Meaningful handle for this case, currently 
            // this is handled as it was before the exception throwing
            return 0.0;
        }
    }
}

/**
 * Device Data Source which is based on the Live Machine
 */
class LiveMachineDesignRFDeviceDataSource extends MeasurementDataSource {

    private static final Logger LOGGER = Logger.getLogger(LiveMachineDesignRFDeviceDataSource.class.getName());

    /**
     * Get this source's label
     */
    @Override
    public String getLabel() {
        return "Live Machine Design RF Lattice";
    }

    /**
     * Get the field for the specified magnet
     */
    @Override
    public double getField(final Magnet magnet) {
        if (magnet.isPermanent()) {
            return magnet.getDesignField();
        } else {
            try {
                // use the total field setting rather than the readback
                return ((Electromagnet) magnet).getTotalFieldSetting();
            } catch (GetException exception) {
                LOGGER.log(Level.SEVERE, null, exception);
                return 0.0;
            }
        }
    }
}

/**
 * Device Data Source which is based in a PV Logger Snapshot
 */
class DesignDeviceDataSource extends AbstractDeviceDataSource {

    /**
     * Get this source's label
     */
    @Override
    public String getLabel() {
        return "Design Lattice";
    }

    /**
     * Get the magnet's bend angle per unit element length
     */
    @Override
    public double getBendAnglePerLength(final Bend bend, final double unitCharge, final double momentum) {
        // take the design angle as is without accounting for unit charge since it is a definition
        return bend.getDfltBendAngle() / bend.getEffLength() * Math.PI / 180.0;
    }

    /**
     * Get the magnet's bend angle per unit element length
     */
    @Override
    public double getBendEntranceAngle(final Bend bend, final double unitCharge, final double momentum) {
        return bend.getEntrRotAngle() * Math.PI / 180.0;
    }

    /**
     * Get the magnet's bend angle per unit element length
     */
    @Override
    public double getBendExitAngle(final Bend bend, final double unitCharge, final double momentum) {
        return bend.getExitRotAngle() * Math.PI / 180.0;
    }

    /**
     * Get the field for the specified magnet
     */
    @Override
    public double getField(final Magnet magnet) {
        return magnet.getDesignField();
    }
}
