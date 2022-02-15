//
//  OrbitMatcher.java
//  xal
//
//  Created by Tom Pelaia on 6/21/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.orbit;

import java.util.ArrayList;
import java.util.List;

import xal.model.probe.traj.Trajectory;
import xal.model.probe.traj.TransferMapState;
import xal.smf.AcceleratorNode;
import xal.tools.beam.PhaseMatrix;
import xal.tools.math.GenericMatrix;

/**
 * using the online model (ignoring coupling), determines a beam position and
 * momentum at an element which best matches the measured positions at a series
 * of specified elements
 */
public class OrbitMatcher {

    /**
     * node for which we wish to determine the matching beam position and
     * momentum
     */
    final AcceleratorNode targetNode;

    /**
     * list of nodes for which we have measured beam positions
     */
    final List<? extends AcceleratorNode> measuredNodes;

    /**
     * trajectory from which to get the transfer matrices
     */
    protected Trajectory<TransferMapState> trajectory;

    /**
     * horizontal beam position transform
     */
    protected BeamPositionTransform xBeamPositionTransform;

    /**
     * vertical beam position transform
     */
    protected BeamPositionTransform yBeamPositionTransform;

    /**
     * Constructor
     */
    public OrbitMatcher(final AcceleratorNode targetNode, final List<? extends AcceleratorNode> measuredNodes, final Trajectory<TransferMapState> trajectory) {
        this.targetNode = targetNode;
        this.measuredNodes = measuredNodes;
        setTrajectory(trajectory);
    }

    /**
     * get the best matching horizontal beam position in mm at the target node
     * based on the beam position measurements in mm at the measurement nodes
     */
    public double getHorizontalTargetBeamPosition(final double[] measuredBeamPositions) {
        return xBeamPositionTransform.getTargetBeamPosition(measuredBeamPositions);
    }

    /**
     * get the best matching vertical beam position in mm at the target node
     * based on the beam position measurements in mm at the measurement nodes
     */
    public double getVerticalTargetBeamPosition(final double[] measuredBeamPositions) {
        return yBeamPositionTransform.getTargetBeamPosition(measuredBeamPositions);
    }

    /**
     * set the trajectory
     */
    public void setTrajectory(final Trajectory<TransferMapState> trajectory) {
        this.trajectory = trajectory;

        final List<TransferRow> xTransferRows = new ArrayList<>(measuredNodes.size());
        final List<TransferRow> yTransferRows = new ArrayList<>(measuredNodes.size());

        for (final AcceleratorNode node : measuredNodes) {
            // we need to get the transfer matrix from the target node to the measurement node (see the equations)
            final PhaseMatrix transferMatrix = getTransferMatrix(targetNode, node);
            xTransferRows.add(extractHorizontalSubMatrix(transferMatrix));
            yTransferRows.add(extractVerticalSubMatrix(transferMatrix));
        }

        xBeamPositionTransform = new BeamPositionTransform(xTransferRows);
        yBeamPositionTransform = new BeamPositionTransform(yTransferRows);
    }

    /**
     * extract the horizontal sub matrix
     */
    protected static TransferRow extractHorizontalSubMatrix(final PhaseMatrix transferMatrix) {
        final double t11 = transferMatrix.getElem(PhaseMatrix.IND_X, PhaseMatrix.IND_X);
        final double t12 = transferMatrix.getElem(PhaseMatrix.IND_X, PhaseMatrix.IND_XP);
        final double t13 = 1000 * transferMatrix.getElem(PhaseMatrix.IND_X, PhaseMatrix.IND_HOM);

        return new TransferRow(t11, t12, t13);
    }

    /**
     * extract the vertical sub matrix
     */
    protected static TransferRow extractVerticalSubMatrix(final PhaseMatrix transferMatrix) {
        final double t11 = transferMatrix.getElem(PhaseMatrix.IND_Y, PhaseMatrix.IND_Y);
        final double t12 = transferMatrix.getElem(PhaseMatrix.IND_Y, PhaseMatrix.IND_YP);
        final double t13 = 1000 * transferMatrix.getElem(PhaseMatrix.IND_Y, PhaseMatrix.IND_HOM);

        return new TransferRow(t11, t12, t13);
    }

    /**
     * get the transfer matrix from the transfer map trajectory
     */
    protected PhaseMatrix getTransferMatrix(final AcceleratorNode fromNode, final AcceleratorNode toNode) {
        final TransferMapState fromState = this.trajectory.stateForElement(fromNode.getId());
        final TransferMapState toState = this.trajectory.stateForElement(toNode.getId());

        // get the transfer matricies for the "from" and "to" states
        final PhaseMatrix fromTransferMatrix = fromState.getTransferMap().getFirstOrder();
        final PhaseMatrix toTransferMatrix = toState.getTransferMap().getFirstOrder();

        // compute the transfer matrix from the "from" state to the "toState"
        return toTransferMatrix.times(fromTransferMatrix.inverse());
    }
}

/**
 * best fit transform (in one plane) from a set of beam positions to a beam
 * position at a specified point
 */
class BeamPositionTransform {

    protected final GenericMatrix kickTransform;
    protected final GenericMatrix projectionTransform;

    /**
     * Constructor
     */
    public BeamPositionTransform(final List<TransferRow> transferRows) {
        final int rowCount = transferRows.size();

        kickTransform = new GenericMatrix(rowCount, 1);
        final GenericMatrix phaseTransform = new GenericMatrix(rowCount, 2);

        int row = 0;
        for (final TransferRow transferRow : transferRows) {
            phaseTransform.setElem(row, 0, transferRow.t11);
            phaseTransform.setElem(row, 1, transferRow.t12);
            kickTransform.setElem(row, 0, transferRow.t13);
            ++row;
        }

        // projection transform:  (A<sup>T</sup> A)<sup>-1</sup> A<sup>T</sup>
        final GenericMatrix phaseTransformTranspose = phaseTransform.transpose();
        projectionTransform = phaseTransformTranspose.times(phaseTransform).inverse().times(phaseTransformTranspose);
    }

    /**
     * get the best matching beam position in mm at the target node based on the
     * beam position measurements in mm at the measurement nodes
     */
    public double getTargetBeamPosition(final double[] measuredBeamPositions) {
        final GenericMatrix beamPositionVector = new GenericMatrix(measuredBeamPositions.length, 1);

        for (int row = 0; row < measuredBeamPositions.length; row++) {
            beamPositionVector.setElem(row, 0, measuredBeamPositions[row]);
        }

        return projectionTransform.times(beamPositionVector.minus(kickTransform)).getElem(0, 0);
    }
}

/**
 * three elements of the transfer matrix
 */
class TransferRow {

    public final double t11;
    public final double t12;
    public final double t13;

    /**
     * Constructor
     */
    public TransferRow(final double t11, final double t12, final double t13) {
        this.t11 = t11;
        this.t12 = t12;
        this.t13 = t13;
    }
}
