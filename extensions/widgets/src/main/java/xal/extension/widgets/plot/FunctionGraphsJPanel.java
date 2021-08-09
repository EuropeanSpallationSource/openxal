package xal.extension.widgets.plot;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.awt.image.*;
import java.text.*;
import java.awt.event.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * This is the base class of the plotting package. It is a sub-class of JPanel
 * and displays all graphics
 *
 * @author shishlo, tap
 * @version 1.0
 */
public class FunctionGraphsJPanel extends JPanel implements MouseListener, MouseMotionListener {

    private static final Logger LOGGER = Logger.getLogger(FunctionGraphsJPanel.class.getName());

    /**
     * serialization ID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The static field that defines HORIZONTAL line on a graph panel
     */
    public static final int HORIZONTAL = 0;

    /**
     * The static field that defines Vertical line on a graph panel
     */
    public static final int VERTICAL = 1;

    private Vector<BasicGraphData> graphDataV = new Vector<>();
    private Vector<Color> graphColorV = new Vector<>();
    private Vector<CurveData> curveDataV = new Vector<>();
    private ColorSurfaceData colorSurfaceData = null;

    private int nTotalGraphPoints = 0;
    private int nTotalCurvePoints = 0;

    private Color bkgGraphAreaColor = null;
    private Color bkgBorderAreaColor = null;

    private Color gridLineColor = null;

    private Color lineDefaultColor = Color.black;
    private Color lineChoosenColor = Color.red;

    private String nameOfGraph = null;
    private String nameX = null;
    private String nameY = null;

    private Font nameOfGraphFont = null;
    private Font nameXFont = null;
    private Font nameYFont = null;
    private Font numberFont = null;

    private Color nameOfGraphColor = Color.black;
    private Color nameXColor = Color.black;
    private Color nameYColor = Color.black;
    private Color numberColor = Color.black;

    private NumberFormat numberFormatX = new DecimalFormat(STRING_DEC_FORMAT);
    private NumberFormat numberFormatY = new DecimalFormat(STRING_DEC_FORMAT);

    private boolean gridLineOnX = true;
    private boolean gridLineOnY = true;

    private boolean gridXmarkerOn = true;
    private boolean gridYmarkerOn = true;

    //scale factors for numbers at the ticks. They are changing the presentation on the screen only
    private double numbMarkScaleX = 1.0;
    private double numbMarkScaleY = 1.0;

    // Vertical and horizontal lines (vector includes Double with y and x coordinates)
    private Vector<Double> vLinesV = new Vector<>();
    private Vector<Double> hLinesV = new Vector<>();
    private Vector<Color> vLinesColorV = new Vector<>();
    private Vector<Color> hLinesColorV = new Vector<>();

    private Color defaultVerticalLineColor = Color.cyan;
    private Color defaultHorizontLineColor = Color.cyan;

    //grid limits instances
    private GridLimits innerGridLimits = new SmartGridLimits();
    private boolean useSmartGridLimits = true;
    private GridLimits externalGridLimits = null;
    private Vector<GridLimits> zoomGridLimitsV = new Vector<>();

    //off screen image
    private transient Image offScreenImage = null;
    private boolean offScreenImageOn = true;

    //data that are used as temporary for drawing
    private double scaleX = 0.;
    private double scaleY = 0.;

    private double xMin = 0.;
    private double yMin = 0.;
    private double xMax = 0.;
    private double yMax = 0.;

    private int xLOffSet = 0;
    private int xROffSet = 0;
    private int yUOffSet = 0;
    private int yBOffSet = 0;

    private int screenW = 0;
    private int screenH = 0;

    private int fSizeX = 0;
    private int fSizeY = 0;

    //point selected by mouse
    private transient ClickedPoint clickedPoint = new ClickedPoint();

    private int evntIniX;
    private int evntIniY;
    private boolean mouseDrugged = false;
    private int mouseUsedButton = 0;

    //right now there are four types of task
    //0 - zoom
    //1 - horizontal lines dragging
    //2 - vertical lines dragging
    //3 - legend dragging
    private int mouseDraggedTaskType = -1;

    //dialog related members
    private JDialog axisDialog = null;
    private transient Object parentFrameOrDialog = null;
    private GridLimitsPanel glPanel = new GridLimitsPanel();

    //graph choosing mode
    private boolean graphChoosingYes = false;
    private boolean graphChosenYes = false;
    private int graphChosenIndex = 0;
    private int graphPointChosenIndex = 0;
    private double choosenX = 0.;
    private double choosenY = 0.;
    private JRadioButton chooseModeButton = new JRadioButton("S", false);
    private boolean chooseModeButtonVisible = true;
    private transient ActionListener chooseListener = null;

    //graph dragging vertical and horizontal lines mode
    private boolean dragHorLinesModeYes = false;
    private boolean dragVerLinesModeYes = false;
    private JRadioButton dragHorLinesModeButton = new JRadioButton("Y", false);
    private JRadioButton dragVerLinesModeButton = new JRadioButton("X", false);
    private boolean horLinesModeButtonVisible = true;
    private boolean verLinesModeButtonVisible = true;
    private int draggedLinesIndex;
    private transient ActionListener draggedHorLinesListener = null;
    private transient ActionListener draggedVerLinesListener = null;
    private ActionEvent draggedHorLinesEvent = null;
    private ActionEvent draggedVerLinesEvent = null;
    private boolean draggedHorLinesMotionListenYes = false;
    private boolean draggedVerLinesMotionListenYes = false;
    private Polygon triangleMarkerLeft;
    private Polygon triangleMarkerRight;

    //Change in VERTICAL and HORIZONTAL limits listeners
    private transient Vector<ActionListener> horLimListenersV = new Vector<>();
    private transient Vector<ActionListener> verLimListenersV = new Vector<>();
    private ActionEvent horLimEvent = null;
    private ActionEvent verLimEvent = null;

    //Legend
    private transient GraphLegend legend;
    private JRadioButton legendButton = new JRadioButton("L", false);
    private boolean legendButtonVisible = true;
    private String legendKeyString = STRING_KEY_LEGEND;

    /**
     * The constant defining the legend position at the arbitrary place of the
     * graph panel
     */
    public static final int LEGEND_POSITION_ARBITRARY = 0;

    /**
     * The constant defining the legend position at the top left corner of the
     * graph panel
     */
    public static final int LEGEND_POSITION_TOP_LEFT = 1;

    /**
     * The constant defining the legend position at the top right corner of the
     * graph panel
     */
    public static final int LEGEND_POSITION_TOP_RIGHT = 2;

    /**
     * The constant defining the legend position at the bottom left corner of
     * the graph panel
     */
    public static final int LEGEND_POSITION_BOTTOM_LEFT = 3;
    /**
     * The constant defining the legend position at the bottom right corner of
     * the graph panel
     */
    public static final int LEGEND_POSITION_BOTTOM_RIGHT = 4;

    protected static final String STRING_DEC_FORMAT = "0.00E0";
    private static final String STRING_KEY_LEGEND = "Legend";

    /**
     * Constructor for the FunctionGraphsJPanel object
     */
    public FunctionGraphsJPanel() {
        this.initialSettings();
    }

    /**
     * Performs initial settings
     */
    private void initialSettings() {

        //this two lines do nothing but we need them to get fonts
        GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        gEnv.getAvailableFontFamilyNames();

        nameOfGraphFont = this.getFont();
        nameXFont = this.getFont();
        nameYFont = this.getFont();
        numberFont = this.getFont();

        gridXmarkerOn = true;
        gridYmarkerOn = true;

        bkgGraphAreaColor = getBackground();
        bkgBorderAreaColor = getBackground();

        addMouseListener(this);
        addMouseMotionListener(this);

        clickedPoint.xValueText.setFont(new Font(this.getFont().getFamily(), Font.BOLD, 10));
        clickedPoint.yValueText.setFont(new Font(this.getFont().getFamily(), Font.BOLD, 10));
        clickedPoint.zValueText.setFont(new Font(this.getFont().getFamily(), Font.BOLD, 10));
        clickedPoint.xValueLabel.setFont(new Font(this.getFont().getFamily(), Font.BOLD, 10));
        clickedPoint.yValueLabel.setFont(new Font(this.getFont().getFamily(), Font.BOLD, 10));
        clickedPoint.zValueLabel.setFont(new Font(this.getFont().getFamily(), Font.BOLD, 10));

        glPanel.setFunctionGraphsJPanel(this);

        innerGridLimits.setGridLimitsSwitch(true);
        innerGridLimits.initialize();

        //ActionEvents for VERTICAL and HORIZONTAL limits
        horLimEvent = new ActionEvent(this, HORIZONTAL, "changed");
        verLimEvent = new ActionEvent(this, VERTICAL, "changed");

        //legend
        legend = new GraphLegend(this);
        legend.setKeyString(legendKeyString);
        legend.setVisible(legendButton.isSelected());
        legend.positionArbitrary = LEGEND_POSITION_ARBITRARY;
        legend.positionTopLeft = LEGEND_POSITION_TOP_LEFT;
        legend.positionTopRight = LEGEND_POSITION_TOP_RIGHT;
        legend.positionBottomLeft = LEGEND_POSITION_BOTTOM_LEFT;
        legend.positionBottomRight = LEGEND_POSITION_BOTTOM_RIGHT;

        //buttons
        chooseModeButton.setFont(new Font(this.getFont().getFamily(), Font.BOLD, 10));
        chooseModeButton.setToolTipText("Selection Mode");
        chooseModeButton.addActionListener(e -> {
            if (chooseModeButton.isSelected()) {
                setChoosingGraphMode();
            } else {
                setDisplayGraphMode();
            }
        });

        dragHorLinesModeButton.setFont(new Font(this.getFont().getFamily(), Font.BOLD, 10));
        dragHorLinesModeButton.setToolTipText("Dragging Horizontal Lines Mode");
        dragHorLinesModeButton.addActionListener(e -> setDraggingHorLinesGraphMode(dragHorLinesModeButton.isSelected()));

        dragVerLinesModeButton.setFont(new Font(this.getFont().getFamily(), Font.BOLD, 10));
        dragVerLinesModeButton.setToolTipText("Dragging Vertical Lines Mode");
        dragVerLinesModeButton.addActionListener(e -> setDraggingVerLinesGraphMode(dragVerLinesModeButton.isSelected()));

        legendButton.setFont(new Font(this.getFont().getFamily(), Font.BOLD, 10));
        legendButton.setToolTipText(STRING_KEY_LEGEND);
        legendButton.addActionListener(e -> setLegendVisible(legendButton.isSelected()));

        setLayout(null);
        add(chooseModeButton);
        add(dragHorLinesModeButton);
        add(dragVerLinesModeButton);
        add(legendButton);

        synchronized (this) {
            draggedHorLinesEvent = new ActionEvent(this, HORIZONTAL, "dragging");
            draggedVerLinesEvent = new ActionEvent(this, VERTICAL, "dragging");
        }

        int[] triangleX = new int[3];
        int[] triangleY = new int[3];
        triangleX[0] = -7;
        triangleY[0] = -5;
        triangleX[1] = -7;
        triangleY[1] = 5;
        triangleX[2] = 0;
        triangleY[2] = 0;
        triangleMarkerLeft = new Polygon(triangleX, triangleY, 3);
        triangleMarkerLeft.invalidate();
        triangleX[0] = -5;
        triangleY[0] = 7;
        triangleX[1] = 5;
        triangleY[1] = 7;
        triangleX[2] = 0;
        triangleY[2] = 0;
        triangleMarkerRight = new Polygon(triangleX, triangleY, 3);
        triangleMarkerRight.invalidate();

        //-----------------------------------------------
        //last adjustments
        //-----------------------------------------------
        //set legend button invisible
        setLegendButtonVisible(false);
        setChooseModeButtonVisible(false);
        setHorLinesButtonVisible(false);
        setVerLinesButtonVisible(false);
    }

    /**
     * Adds a BasicGraphData instance to the graph panel
     *
     * @param lgd The BasicGraphData instance
     * @return The index of this new data set in the internal array of data
     * references
     */
    public synchronized int addGraphData(BasicGraphData lgd) {
        graphDataV.add(lgd);
        if (graphChoosingYes) {
            graphColorV.add(lineDefaultColor);
        } else {
            graphColorV.add(null);
        }
        lgd.registerInContainer(this);
        updateData();
        return graphDataV.size() - 1;
    }

    /**
     * Removes a BasicGraphData instance with a particular index from the graph
     * panel
     *
     * @param index The index of this data set in the graph panel
     */
    public synchronized void removeGraphData(int index) {
        if (index < graphDataV.size()) {
            BasicGraphData lgd = graphDataV.get(index);
            lgd.removeContainer(this);
            graphDataV.remove(index);
            graphColorV.remove(index);
            updateData();
        }
    }

    /**
     * Removes a BasicGraphData instance from the graph panel
     *
     * @param gd The BasicGraphData instance
     */
    public synchronized void removeGraphData(BasicGraphData gd) {
        int index = graphDataV.indexOf(gd);
        if (index < 0) {
            return;
        }
        removeGraphData(index);
    }

    /**
     * Adds all BasicGraphData instances in the vector to the graph panel. All
     * previous data will remain on the graph panel.
     *
     * @param gdV The vector with BasicGraphData instances
     */
    public synchronized void addGraphData(final Vector<? extends BasicGraphData> gdV) {
        for (final BasicGraphData lgd : gdV) {
            if (graphChoosingYes) {
                graphColorV.add(lineDefaultColor);
            } else {
                graphColorV.add(null);
            }
            graphDataV.add(lgd);
            lgd.registerInContainer(this);
        }
        updateData();
    }

    /**
     * Sets all BasicGraphData instances in the vector to the graph panel. All
     * previous data will be removed from the graph panel
     *
     * @param gdV The vector with BasicGraphData instances
     */
    public synchronized void setGraphData(final Vector<? extends BasicGraphData> gdV) {
        for (int i = 0, n = graphDataV.size(); i < n; i++) {
            final BasicGraphData lgd = graphDataV.get(i);
            lgd.removeContainer(this);
        }

        graphDataV.clear();
        graphColorV.clear();

        for (final BasicGraphData lgd : gdV) {
            if (graphChoosingYes) {
                graphColorV.add(lineDefaultColor);
            } else {
                graphColorV.add(null);
            }
            graphDataV.add(lgd);
            lgd.registerInContainer(this);
        }
        updateData();
    }

    /**
     * Removes all BasicGraphData instances in the vector from the graph panel
     *
     * @param gdV The vector with BasicGraphData instances
     */
    public synchronized void removeGraphData(final Vector<? extends BasicGraphData> gdV) {
        for (int i = 0, nGDV = gdV.size(); i < nGDV; i++) {
            BasicGraphData lgd = gdV.get(i);
            int index = graphDataV.indexOf(lgd);
            if (index < 0) {
                continue;
            }
            lgd = graphDataV.get(index);
            lgd.removeContainer(this);
            graphDataV.remove(index);
            graphColorV.remove(index);
        }
        updateData();
    }

    /**
     * Removes all BasicGraphData instances from the graph panel
     */
    public synchronized void removeAllGraphData() {
        for (int i = 0, n = graphDataV.size(); i < n; i++) {
            BasicGraphData lgd = graphDataV.get(i);
            lgd.removeContainer(this);
        }
        graphDataV.clear();
        graphColorV.clear();
        updateData();
    }

    /**
     * Returns the reference to BasicGraphData object with a particular index
     *
     * @param index The index of BasicGraphData object inside the graph panel
     * @return The reference to BasicGraphData object
     */
    public BasicGraphData getInstanceOfGraphData(int index) {
        if (graphDataV.size() > index) {
            return graphDataV.get(index);
        }
        return null;
    }

    /**
     * Returns the vector with references to all BasicGraphData objects on this
     * graph panel
     *
     * @return The vector with references to all BasicGraphData objects
     */
    public Vector<BasicGraphData> getAllGraphData() {
        Vector<BasicGraphData> tmp = new Vector<>();
        synchronized (graphDataV) {
            for (int i = 0; i < graphDataV.size(); i++) {
                tmp.add(graphDataV.get(i));
            }
        }
        return tmp;
    }

    /**
     * Returns the number of BasicGraphData objects on this graph panel
     *
     * @return The number of BasicGraphData objects
     */
    public int getNumberOfInstanceOfGraphData() {
        return graphDataV.size();
    }

    /**
     * Returns the total number of points in the all BasicGraphData objects
     *
     * @return The total number of points in the all BasicGraphData objects on
     * this panel
     */
    public synchronized int getNumbTotalGraphPoints() {
        return nTotalGraphPoints;
    }

    //----------------------------------------------
    //Methods related to the colored surface data
    //(contour plot)
    //----------------------------------------------
    /**
     * Sets ColorSurfaceData object that will be plotted on the graph panel
     *
     * @param colorSurfaceData New ColorSurfaceData object
     */
    public void setColorSurfaceData(ColorSurfaceData colorSurfaceData) {
        this.colorSurfaceData = colorSurfaceData;
        updateData();
    }

    /**
     * Returns the reference to ColorSurfaceData object that is plotted on the
     * graph panel
     *
     * @return The reference to ColorSurfaceData object currently plotted on the
     * graph panel
     */
    public ColorSurfaceData getColorSurfaceData() {
        return colorSurfaceData;
    }

    //----------------------------------------------
    //Methods related to the curve data
    //----------------------------------------------
    /**
     * Returns the curveData attribute of the FunctionGraphsJPanel object
     *
     * @param i Description of the Parameter
     * @return The curveData value
     */
    public CurveData getCurveData(int i) {
        return curveDataV.get(i);
    }

    /**
     * Returns the allCurveData attribute of the FunctionGraphsJPanel object
     *
     * @return The allCurveData value
     */
    public Vector<CurveData> getAllCurveData() {
        return new Vector<>(curveDataV);
    }

    /**
     * Adds a feature to the CurveData attribute of the FunctionGraphsJPanel
     * object
     *
     * @param curveData The feature to be added to the CurveData attribute
     */
    public void addCurveData(CurveData curveData) {
        curveDataV.add(curveData);
        updateData();
    }

    /**
     * Adds a feature to the CurveData attribute of the FunctionGraphsJPanel
     * object
     *
     * @param cdV The feature to be added to the CurveData attribute
     */
    public void addCurveData(final Vector<? extends CurveData> cdV) {
        for (final CurveData cd : cdV) {
            if (cd != null) {
                curveDataV.add(cd);
            }
        }
        updateData();
    }

    /**
     * Sets the curveData attribute of the FunctionGraphsJPanel object
     *
     * @param cdV The new curveData value
     */
    public void setCurveData(final Vector<? extends CurveData> cdV) {
        curveDataV.clear();
        for (final CurveData cd : cdV) {
            if (cd != null) {
                curveDataV.add(cd);
            }
        }
        updateData();
    }

    /**
     * Description of the Method
     *
     * @param i Description of the Parameter
     */
    public void removeCurveData(int i) {
        if (i < curveDataV.size()) {
            curveDataV.remove(i);
            updateData();
        }
    }

    /**
     * Description of the Method
     *
     * @param curveData Description of the Parameter
     */
    public void removeCurveData(CurveData curveData) {
        curveDataV.remove(curveData);
        updateData();
    }

    /**
     * Description of the Method
     */
    public void removeAllCurveData() {
        curveDataV.clear();
        updateData();
    }

    //----------------------------------------------
    //----------------------------------------------
    /**
     * Sets the graphsDefaultColor attribute of the FunctionGraphsJPanel object
     *
     * @param color The new graphsDefaultColor value
     */
    public synchronized void setGraphsDefaultColor(Color color) {
        lineDefaultColor = color;
        updateGraphJPanel();
    }

    /**
     * Sets the graphLineChoosenColor attribute of the FunctionGraphsJPanel
     * object
     *
     * @param color The new graphLineChoosenColor value
     */
    public synchronized void setGraphLineChoosenColor(Color color) {
        lineChoosenColor = color;
        updateGraphJPanel();
    }

    /**
     * Returns the graphsDefaultColor attribute of the FunctionGraphsJPanel
     * object
     *
     * @return The graphsDefaultColor value
     */
    public synchronized Color getGraphsDefaultColor() {
        return lineDefaultColor;
    }

    /**
     * Description of the Method
     */
    public synchronized void resetGraphsDefaultColor() {
        for (int i = 0, n = graphColorV.size(); i < n; i++) {
            graphColorV.set(i, lineDefaultColor);
        }
        updateGraphJPanel();
    }

    /**
     * Description of the Method
     */
    public void removeColorForAllGraphs() {
        for (int i = 0, n = graphColorV.size(); i < n; i++) {
            graphColorV.set(i, null);
        }
        updateGraphJPanel();
    }

    /**
     * Returns the graphColor attribute of the FunctionGraphsJPanel object
     *
     * @param index Description of the Parameter
     * @return The graphColor value
     */
    public Color getGraphColor(int index) {
        if (graphColorV.size() > index) {
            return graphColorV.get(index);
        }
        return null;
    }

    /**
     * Sets the graphColor attribute of the FunctionGraphsJPanel object
     *
     * @param index The new graphColor value
     * @param color The new graphColor value
     * @return Description of the Return Value
     */
    public boolean setGraphColor(int index, Color color) {
        if (graphColorV.size() > index) {
            graphColorV.set(index, color);
            updateGraphJPanel();
            return true;
        }
        return false;
    }

    /**
     * Description of the Method
     *
     * @param gridXmarkerOnIn Description of the Parameter
     */
    public void xMarkersOn(boolean gridXmarkerOnIn) {
        gridXmarkerOn = gridXmarkerOnIn;
        updateGraphJPanel();
    }

    /**
     * Description of the Method
     *
     * @param gridYmarkerOnIn Description of the Parameter
     */
    public void yMarkersOn(boolean gridYmarkerOnIn) {
        gridYmarkerOn = gridYmarkerOnIn;
        updateGraphJPanel();
    }

    //--------------------------------------------------
    //method related to the off screen image drawing
    //--------------------------------------------------
    /**
     * Sets the offScreenImageDrawing attribute of the FunctionGraphsJPanel
     * object
     *
     * @param offScreenImageOnIn The new offScreenImageDrawing value
     */
    public synchronized void setOffScreenImageDrawing(boolean offScreenImageOnIn) {
        offScreenImageOn = offScreenImageOnIn;
    }

    //--------------------------------------------------
    //methods related to the graph choosing
    //--------------------------------------------------
    /**
     * Sets the displayGraphMode attribute of the FunctionGraphsJPanel object
     */
    public void setDisplayGraphMode() {
        graphChoosingYes = false;
        graphChosenYes = false;
        synchronized (this) {
            for (int i = 0, n = graphColorV.size(); i < n; i++) {
                graphColorV.set(i, null);
            }
        }
        clickedPoint.setDisplayed(false);
        updateGraphJPanel();
        chooseModeButton.setSelected(graphChoosingYes);
    }

    /**
     * Sets the choosingGraphMode attribute of the FunctionGraphsJPanel object
     */
    public void setChoosingGraphMode() {
        graphChoosingYes = true;
        graphChosenYes = false;
        synchronized (this) {
            for (int i = 0, n = graphColorV.size(); i < n; i++) {
                graphColorV.set(i, lineDefaultColor);
            }
        }
        clickedPoint.setDisplayed(false);
        updateGraphJPanel();
        chooseModeButton.setSelected(graphChoosingYes);
    }

    /**
     * Sets the chooseModeButtonVisible attribute of the FunctionGraphsJPanel
     * object
     *
     * @param vs The new chooseModeButtonVisible value
     */
    public void setChooseModeButtonVisible(boolean vs) {
        chooseModeButtonVisible = vs;
        remove(chooseModeButton);
        if (vs) {
            add(chooseModeButton);
            chooseModeButton.setSelected(graphChoosingYes);
        }
        updateGraphJPanel();
    }

    /**
     * Description of the Method
     */
    private void unChooseGraph() {
        graphChosenYes = false;
        clickedPoint.setDisplayed(false);
        if (!graphChoosingYes) {
            return;
        }
        synchronized (this) {
            for (int i = 0, n = graphColorV.size(); i < n; i++) {
                graphColorV.set(i, lineDefaultColor);
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param iX Description of the Parameter
     * @param iY Description of the Parameter
     * @return Description of the Return Value
     */
    private boolean chooseGraphFromLegend(int iX, int iY) {
        if (!graphChoosingYes) {
            return false;
        }
        graphChosenYes = false;
        graphChosenIndex = -1;
        graphPointChosenIndex = -1;
        Integer ind = legend.getChoosenGraphIndex(iX, iY);
        if (ind != null) {
            graphChosenYes = true;
            graphChosenIndex = ind;
            synchronized (this) {
                for (int i = 0, n = graphColorV.size(); i < n; i++) {
                    graphColorV.set(i, lineDefaultColor);
                }
                graphColorV.set(graphChosenIndex, lineChoosenColor);
            }
            graphChosenYes = true;
            clickedPoint.setDisplayed(false);
            if (chooseListener != null) {
                ActionEvent evnt = new ActionEvent(this, 0, "chosen");
                chooseListener.actionPerformed(evnt);
            }
            return true;
        }
        return false;
    }

    /**
     * Description of the Method
     *
     * @param x Description of the Parameter
     * @param y Description of the Parameter
     */
    private void chooseGraph(double x, double y) {
        if (!graphChoosingYes) {
            return;
        }
        boolean success = false;
        BasicGraphData gd;
        double minDist = Double.MAX_VALUE;
        double dist;
        double xG;
        double yG;
        double xPos = 0.;
        double yPos = 0.;
        double xCurrMin = getCurrentMinX();
        double yCurrMin = getCurrentMinY();
        double xCurrMax = getCurrentMaxX();
        double yCurrMax = getCurrentMaxY();
        for (int i = 0, ni = graphDataV.size(); i < ni; i++) {
            gd = graphDataV.get(i);
            synchronized (gd) {
                for (int j = 0, nj = gd.getNumbOfPoints(); j < nj; j++) {
                    xG = gd.getX(j);
                    yG = gd.getY(j);
                    if (xG < xCurrMin || xG > xCurrMax
                            || yG < yCurrMin || yG > yCurrMax) {
                        continue;
                    }
                    xG = xG - x;
                    yG = yG - y;
                    dist = xG * xG + yG * yG;
                    if (dist < minDist) {
                        graphChosenIndex = i;
                        graphPointChosenIndex = j;
                        xPos = xG + x;
                        yPos = yG + y;
                        success = true;
                        minDist = dist;
                    }
                }
            }
        }
        if (success) {
            synchronized (this) {
                for (int i = 0, n = graphColorV.size(); i < n; i++) {
                    graphColorV.set(i, lineDefaultColor);
                }
                graphColorV.set(graphChosenIndex, lineChoosenColor);
            }
            graphChosenYes = true;
            choosenX = xPos;
            choosenY = yPos;
            if (colorSurfaceData == null) {
                clickedPoint.updateValues(xPos, yPos);
            } else {
                clickedPoint.updateValues(xPos, yPos, colorSurfaceData.getValue(xPos, yPos));
            }
            clickedPoint.setDisplayed(true);
            if (chooseListener != null) {
                ActionEvent evnt = new ActionEvent(this, 0, "chosen");
                chooseListener.actionPerformed(evnt);

            }
        }
    }

    /**
     * Returns the graphChosenIndex attribute of the FunctionGraphsJPanel object
     *
     * @return The graphChosenIndex value
     */
    public Integer getGraphChosenIndex() {
        if (graphChosenYes && graphChosenIndex >= 0) {
            return graphChosenIndex;
        }
        return null;
    }

    /**
     * Returns the pointChosenIndex attribute of the FunctionGraphsJPanel object
     *
     * @return The pointChosenIndex value
     */
    public Integer getPointChosenIndex() {
        if (graphChosenYes && graphPointChosenIndex >= 0 && graphChosenIndex >= 0) {
            return graphPointChosenIndex;
        }
        return null;
    }

    /**
     * Returns the pointChosenX attribute of the FunctionGraphsJPanel object
     *
     * @return The pointChosenX value
     */
    public Double getPointChosenX() {
        if (graphChosenYes && graphPointChosenIndex >= 0 && graphChosenIndex >= 0) {
            return choosenX;
        }
        return null;
    }

    /**
     * Returns the pointChosenY attribute of the FunctionGraphsJPanel object
     *
     * @return The pointChosenY value
     */
    public Double getPointChosenY() {
        if (graphChosenYes && graphPointChosenIndex >= 0 && graphChosenIndex >= 0) {
            return choosenY;
        }
        return null;
    }

    /**
     * Adds a feature to the ChooseListener attribute of the
     * FunctionGraphsJPanel object
     *
     * @param al The feature to be added to the ChooseListener attribute
     */
    public void addChooseListener(ActionListener al) {
        chooseListener = al;
    }

    //--------------------------------------------------
    //methods related to the grid limits
    //--------------------------------------------------
    /**
     * Returns the newGridLimits attribute of the FunctionGraphsJPanel object
     *
     * @return The newGridLimits value
     */
    public GridLimits getNewGridLimits() {
        return new GridLimits();
    }

    /**
     * Returns the currentGL attribute of the FunctionGraphsJPanel object
     *
     * @return The currentGL value
     */
    public synchronized GridLimits getCurrentGL() {
        if (zoomGridLimitsV.isEmpty()) {
            if (externalGridLimits == null) {
                externalGridLimits = new GridLimits();
                externalGridLimits.setNumberFormatX(numberFormatX);
                externalGridLimits.setNumberFormatY(numberFormatY);
                externalGridLimits.setGridLimitsSwitch(true);
                return externalGridLimits;
            } else {
                return externalGridLimits;
            }
        } else {
            return zoomGridLimitsV.lastElement();
        }
    }

    /**
     * Sets the GridLimits object to the graph panel as an external grid limits
     *
     * @param gL The GridLimits object
     */
    public synchronized void setExternalGL(GridLimits gL) {
        externalGridLimits = gL;
        updateGraphJPanel();
    }

    /**
     * Sets the boolean value that defines if the smart (slow one) GridLimits
     * object will be used as internal GL manager for the graph panel
     *
     * @param smart The boolean value
     */
    public void setSmartGL(boolean smart) {
        useSmartGridLimits = smart;
        if (smart) {
            innerGridLimits = new SmartGridLimits();
        } else {
            innerGridLimits = new GridLimits();
        }
        refreshGraphJPanel();
    }

    /**
     * Returns the external GridLimits object
     *
     * @return The external GL object
     */
    public synchronized GridLimits getExternalGL() {
        return externalGridLimits;
    }

    /**
     * Returns the innerMinX attribute of the FunctionGraphsJPanel object
     *
     * @return The innerMinX value
     */
    public double getInnerMinX() {
        return innerGridLimits.getMinX();
    }

    /**
     * Returns the innerMaxX attribute of the FunctionGraphsJPanel object
     *
     * @return The innerMaxX value
     */
    public double getInnerMaxX() {
        return innerGridLimits.getMaxX();
    }

    /**
     * Returns the innerMinY attribute of the FunctionGraphsJPanel object
     *
     * @return The innerMinY value
     */
    public double getInnerMinY() {
        return innerGridLimits.getMinY();
    }

    /**
     * Returns the innerMaxY attribute of the FunctionGraphsJPanel object
     *
     * @return The innerMaxY value
     */
    public double getInnerMaxY() {
        return innerGridLimits.getMaxY();
    }

    /**
     * Returns the currentMinX attribute of the FunctionGraphsJPanel object
     *
     * @return The currentMinX value
     */
    public synchronized double getCurrentMinX() {
        if (zoomGridLimitsV.isEmpty()) {
            if (externalGridLimits == null || !externalGridLimits.isSetXmin()) {
                return getInnerMinX();
            } else {
                return externalGridLimits.getMinX();
            }
        } else {
            GridLimits gl = zoomGridLimitsV.lastElement();
            if (!gl.isSetXmin()) {
                return getInnerMinX();
            }
            return gl.getMinX();
        }
    }

    /**
     * Returns the currentMaxX attribute of the FunctionGraphsJPanel object
     *
     * @return The currentMaxX value
     */
    public synchronized double getCurrentMaxX() {
        if (zoomGridLimitsV.isEmpty()) {
            if (externalGridLimits == null || !externalGridLimits.isSetXmax()) {
                return getInnerMaxX();
            } else {
                return externalGridLimits.getMaxX();
            }
        } else {
            GridLimits gl = zoomGridLimitsV.lastElement();
            if (!gl.isSetXmax()) {
                return getInnerMaxX();
            }
            return gl.getMaxX();
        }
    }

    /**
     * Returns the currentMinY attribute of the FunctionGraphsJPanel object
     *
     * @return The currentMinY value
     */
    public synchronized double getCurrentMinY() {
        if (zoomGridLimitsV.isEmpty()) {
            if (externalGridLimits == null || !externalGridLimits.isSetYmin()) {
                return getInnerMinY();
            } else {
                return externalGridLimits.getMinY();
            }
        } else {
            GridLimits gl = zoomGridLimitsV.lastElement();
            if (!gl.isSetYmin()) {
                return getInnerMinY();
            }
            return gl.getMinY();
        }
    }

    /**
     * Returns the currentMaxY attribute of the FunctionGraphsJPanel object
     *
     * @return The currentMaxY value
     */
    public synchronized double getCurrentMaxY() {
        if (zoomGridLimitsV.isEmpty()) {
            if (externalGridLimits == null || !externalGridLimits.isSetYmax()) {
                return getInnerMaxY();
            } else {
                return externalGridLimits.getMaxY();
            }
        } else {
            GridLimits gl = zoomGridLimitsV.lastElement();
            if (!gl.isSetYmax()) {
                return getInnerMaxY();
            }
            return gl.getMaxY();
        }
    }

    //----------------------------------------------------------
    //clear the zoom stack
    //----------------------------------------------------------
    /**
     * Description of the Method
     */
    public void clearZoomStack() {
        zoomGridLimitsV.clear();
        updateGraphJPanel();
    }

    //----------------------------------------------------------
    //convenience methods to define grid limits and ticks
    //----------------------------------------------------------
    /**
     * Sets the limitsAndTicksX attribute of the FunctionGraphsJPanel object
     *
     * @param vMin The new limitsAndTicksX value
     * @param step The new limitsAndTicksX value
     * @param nStep The new limitsAndTicksX value
     * @param nMinorTicksIn The new limitsAndTicksX value
     */
    public synchronized void setLimitsAndTicksX(double vMin, double step, int nStep, int nMinorTicksIn) {
        if (externalGridLimits == null) {
            externalGridLimits = new GridLimits();
            externalGridLimits.setNumberFormatX(numberFormatX);
            externalGridLimits.setNumberFormatY(numberFormatY);
        }
        externalGridLimits.setLimitsAndTicksX(vMin, step, nStep, nMinorTicksIn);
        updateGraphJPanel();
    }

    /**
     * Sets the limitsAndTicksY attribute of the FunctionGraphsJPanel object
     *
     * @param vMin The new limitsAndTicksY value
     * @param step The new limitsAndTicksY value
     * @param nStep The new limitsAndTicksY value
     * @param nMinorTicksIn The new limitsAndTicksY value
     */
    public synchronized void setLimitsAndTicksY(double vMin, double step, int nStep, int nMinorTicksIn) {
        if (externalGridLimits == null) {
            externalGridLimits = new GridLimits();
            externalGridLimits.setNumberFormatX(numberFormatX);
            externalGridLimits.setNumberFormatY(numberFormatY);
        }
        externalGridLimits.setLimitsAndTicksY(vMin, step, nStep, nMinorTicksIn);
        updateGraphJPanel();
    }

    /**
     * Sets the limitsAndTicksX attribute of the FunctionGraphsJPanel object
     *
     * @param vMin The new limitsAndTicksX value
     * @param step The new limitsAndTicksX value
     * @param nStep The new limitsAndTicksX value
     */
    public synchronized void setLimitsAndTicksX(double vMin, double step, int nStep) {
        if (externalGridLimits == null) {
            externalGridLimits = new GridLimits();
            externalGridLimits.setNumberFormatX(numberFormatX);
            externalGridLimits.setNumberFormatY(numberFormatY);
        }
        externalGridLimits.setLimitsAndTicksX(vMin, step, nStep);
        updateGraphJPanel();
    }

    /**
     * Sets the limitsAndTicksY attribute of the FunctionGraphsJPanel object
     *
     * @param vMin The new limitsAndTicksY value
     * @param step The new limitsAndTicksY value
     * @param nStep The new limitsAndTicksY value
     */
    public synchronized void setLimitsAndTicksY(double vMin, double step, int nStep) {
        if (externalGridLimits == null) {
            externalGridLimits = new GridLimits();
            externalGridLimits.setNumberFormatX(numberFormatX);
            externalGridLimits.setNumberFormatY(numberFormatY);
        }
        externalGridLimits.setLimitsAndTicksY(vMin, step, nStep);
        updateGraphJPanel();
    }

    /**
     * Sets the limitsAndTicksX attribute of the FunctionGraphsJPanel object
     *
     * @param vMin The new limitsAndTicksX value
     * @param vMax The new limitsAndTicksX value
     * @param step The new limitsAndTicksX value
     * @param nMinorTicksIn The new limitsAndTicksX value
     */
    public synchronized void setLimitsAndTicksX(double vMin, double vMax, double step, int nMinorTicksIn) {
        if (externalGridLimits == null) {
            externalGridLimits = new GridLimits();
            externalGridLimits.setNumberFormatX(numberFormatX);
            externalGridLimits.setNumberFormatY(numberFormatY);
        }
        externalGridLimits.setLimitsAndTicksX(vMin, vMax, step, nMinorTicksIn);
        updateGraphJPanel();
    }

    /**
     * Sets the limitsAndTicksY attribute of the FunctionGraphsJPanel object
     *
     * @param vMin The new limitsAndTicksY value
     * @param vMax The new limitsAndTicksY value
     * @param step The new limitsAndTicksY value
     * @param nMinorTicksIn The new limitsAndTicksY value
     */
    public synchronized void setLimitsAndTicksY(double vMin, double vMax, double step, int nMinorTicksIn) {
        if (externalGridLimits == null) {
            externalGridLimits = new GridLimits();
            externalGridLimits.setNumberFormatX(numberFormatX);
            externalGridLimits.setNumberFormatY(numberFormatY);
        }
        externalGridLimits.setLimitsAndTicksY(vMin, vMax, step, nMinorTicksIn);
        updateGraphJPanel();
    }

    /**
     * Sets the limitsAndTicksX attribute of the FunctionGraphsJPanel object
     *
     * @param vMin The new limitsAndTicksX value
     * @param vMax The new limitsAndTicksX value
     * @param step The new limitsAndTicksX value
     */
    public synchronized void setLimitsAndTicksX(double vMin, double vMax, double step) {
        if (externalGridLimits == null) {
            externalGridLimits = new GridLimits();
            externalGridLimits.setNumberFormatX(numberFormatX);
            externalGridLimits.setNumberFormatY(numberFormatY);
        }
        externalGridLimits.setLimitsAndTicksX(vMin, vMax, step);
        updateGraphJPanel();
    }

    /**
     * Sets the limitsAndTicksY attribute of the FunctionGraphsJPanel object
     *
     * @param vMin The new limitsAndTicksY value
     * @param vMax The new limitsAndTicksY value
     * @param step The new limitsAndTicksY value
     */
    public synchronized void setLimitsAndTicksY(double vMin, double vMax, double step) {
        if (externalGridLimits == null) {
            externalGridLimits = new GridLimits();
            externalGridLimits.setNumberFormatX(numberFormatX);
            externalGridLimits.setNumberFormatY(numberFormatY);
        }
        externalGridLimits.setLimitsAndTicksY(vMin, vMax, step);
        updateGraphJPanel();
    }

    //----------------------------------------------------------
    //methods to control the color
    //----------------------------------------------------------
    /**
     * Sets the graphBackGroundColor attribute of the FunctionGraphsJPanel
     * object
     *
     * @param bkgGraphAreaColor The new graphBackGroundColor value
     */
    public void setGraphBackGroundColor(Color bkgGraphAreaColor) {
        this.bkgGraphAreaColor = bkgGraphAreaColor;
        updateGraphJPanel();
    }

    /**
     * Sets the grid lines color of the FunctionGraphsJPanel. If it is null
     * object the color of grid lines will be darker than the background color.
     *
     * @param gridLineColor The new grid lines color
     */
    public void setGridLineColor(Color gridLineColor) {
        this.gridLineColor = gridLineColor;
        updateGraphJPanel();
    }

    /**
     * Sets the borderBackGroundColor attribute of the FunctionGraphsJPanel
     * object
     *
     * @param bkgBorderAreaColor The new borderBackGroundColor value
     */
    public void setBorderBackGroundColor(Color bkgBorderAreaColor) {
        this.bkgBorderAreaColor = bkgBorderAreaColor;
        updateGraphJPanel();
    }

    /**
     * Returns the graphBackGroundColor attribute of the FunctionGraphsJPanel
     * object
     *
     * @return The graphBackGroundColor value
     */
    public Color getGraphBackGroundColor() {
        return bkgGraphAreaColor;
    }

    /**
     * Returns the borderBackGroundColor attribute of the FunctionGraphsJPanel
     * object
     *
     * @return The borderBackGroundColor value
     */
    public Color getBorderBackGroundColor() {
        return bkgBorderAreaColor;
    }

    /**
     * Sets the name attribute of the FunctionGraphsJPanel object
     *
     * @param name The new name value
     */
    @Override
    public void setName(String name) {
        nameOfGraph = name;
        updateGraphJPanel();
    }

    /**
     * Returns the name attribute of the FunctionGraphsJPanel object
     *
     * @return The name value
     */
    @Override
    public String getName() {
        return nameOfGraph;
    }

    /**
     * Sets the axisNames attribute of the FunctionGraphsJPanel object
     *
     * @param nameX The new axisNames value
     * @param nameY The new axisNames value
     */
    public void setAxisNames(String nameX, String nameY) {
        this.nameX = nameX;
        this.nameY = nameY;
        updateGraphJPanel();
    }

    /**
     * Sets the axisNameX attribute of the FunctionGraphsJPanel object
     *
     * @param nameX The new axisNameX value
     */
    public void setAxisNameX(String nameX) {
        this.nameX = nameX;
        updateGraphJPanel();
    }

    /**
     * Sets the axisNameY attribute of the FunctionGraphsJPanel object
     *
     * @param nameY The new axisNameY value
     */
    public void setAxisNameY(String nameY) {
        this.nameY = nameY;
        updateGraphJPanel();
    }

    /**
     * Sets the nameFont attribute of the FunctionGraphsJPanel object
     *
     * @param fn The new nameFont value
     */
    public void setNameFont(Font fn) {
        nameOfGraphFont = fn;
        updateGraphJPanel();
    }

    /**
     * Sets the axisNameFontX attribute of the FunctionGraphsJPanel object
     *
     * @param fnX The new axisNameFontX value
     */
    public void setAxisNameFontX(Font fnX) {
        nameXFont = fnX;
        updateGraphJPanel();
    }

    /**
     * Sets the axisNameFontY attribute of the FunctionGraphsJPanel object
     *
     * @param fnY The new axisNameFontY value
     */
    public void setAxisNameFontY(Font fnY) {
        nameYFont = fnY;
        updateGraphJPanel();
    }

    /**
     * Sets the numberFont attribute of the FunctionGraphsJPanel object
     *
     * @param fn The new numberFont value
     */
    public void setNumberFont(Font fn) {
        numberFont = fn;
        updateGraphJPanel();
    }

    /**
     * Sets the nameColor attribute of the FunctionGraphsJPanel object
     *
     * @param cl The new nameColor value
     */
    public void setNameColor(Color cl) {
        nameOfGraphColor = cl;
        updateGraphJPanel();
    }

    /**
     * Sets the axisNameColorX attribute of the FunctionGraphsJPanel object
     *
     * @param clX The new axisNameColorX value
     */
    public void setAxisNameColorX(Color clX) {
        nameXColor = clX;
        updateGraphJPanel();
    }

    /**
     * Sets the axisNameColorY attribute of the FunctionGraphsJPanel object
     *
     * @param clY The new axisNameColorY value
     */
    public void setAxisNameColorY(Color clY) {
        nameYColor = clY;
        updateGraphJPanel();
    }

    /**
     * Sets the numberColor attribute of the FunctionGraphsJPanel object
     *
     * @param cl The new numberColor value
     */
    public void setNumberColor(Color cl) {
        numberColor = cl;
        updateGraphJPanel();
    }

    /**
     * Sets the numberFormatX attribute of the FunctionGraphsJPanel object
     *
     * @param df The new numberFormatX value
     */
    public synchronized void setNumberFormatX(NumberFormat df) {
        numberFormatX = df;
        if (externalGridLimits != null) {
            externalGridLimits.setNumberFormatX(numberFormatX);
        }
        if (!useSmartGridLimits) {
            innerGridLimits.setNumberFormatX(numberFormatX);
        }
        updateGraphJPanel();
    }

    /**
     * Sets the numberFormatY attribute of the FunctionGraphsJPanel object
     *
     * @param df The new numberFormatY value
     */
    public synchronized void setNumberFormatY(NumberFormat df) {
        numberFormatY = df;
        if (externalGridLimits != null) {
            externalGridLimits.setNumberFormatY(numberFormatY);
        }
        if (!useSmartGridLimits) {
            innerGridLimits.setNumberFormatY(numberFormatY);
        }
        updateGraphJPanel();
    }

    /**
     * Sets the makrsScaleX attribute of the FunctionGraphsJPanel object
     *
     * @param numbMarkScaleXin The new makrsScaleX value
     */
    public void setMakrsScaleX(double numbMarkScaleXin) {
        numbMarkScaleX = numbMarkScaleXin;
        updateGraphJPanel();
    }

    /**
     * Sets the makrsScaleY attribute of the FunctionGraphsJPanel object
     *
     * @param numbMarkScaleYin The new makrsScaleY value
     */
    public void setMakrsScaleY(double numbMarkScaleYin) {
        numbMarkScaleY = numbMarkScaleYin;
        updateGraphJPanel();
    }

    /**
     * Returns the clickedPointObject attribute of the FunctionGraphsJPanel
     * object
     *
     * @return The clickedPointObject value
     */
    public ClickedPoint getClickedPointObject() {
        return clickedPoint;
    }

    //------------------------------------------------------
    //method related to the vertical and horizontal lines
    //------------------------------------------------------
    /**
     * Returns the numberOfVerticalLines attribute of the FunctionGraphsJPanel
     * object
     *
     * @return The numberOfVerticalLines value
     */
    public synchronized int getNumberOfVerticalLines() {
        return vLinesV.size();
    }

    /**
     * Returns the numberOfHorizontalLines attribute of the FunctionGraphsJPanel
     * object
     *
     * @return The numberOfHorizontalLines value
     */
    public synchronized int getNumberOfHorizontalLines() {
        return hLinesV.size();
    }

    /**
     * Adds a feature to the VerticalLine attribute of the FunctionGraphsJPanel
     * object
     *
     * @param x The feature to be added to the VerticalLine attribute
     * @return Description of the Return Value
     */
    public int addVerticalLine(double x) {
        synchronized (this) {
            vLinesV.add(x);
            vLinesColorV.add(defaultVerticalLineColor);
        }
        updateGraphJPanel();
        if (draggedVerLinesListener != null) {
            draggedVerLinesListener.actionPerformed(draggedVerLinesEvent);
        }
        return vLinesV.size() - 1;
    }

    /**
     * Adds a feature to the HorizontalLine attribute of the
     * FunctionGraphsJPanel object
     *
     * @param y The feature to be added to the HorizontalLine attribute
     * @return Description of the Return Value
     */
    public int addHorizontalLine(double y) {
        synchronized (this) {
            hLinesV.add(y);
            hLinesColorV.add(defaultHorizontLineColor);
        }
        updateGraphJPanel();
        if (draggedHorLinesListener != null) {
            draggedHorLinesListener.actionPerformed(draggedHorLinesEvent);
        }
        return hLinesV.size() - 1;
    }

    /**
     * Adds a feature to the VerticalLine attribute of the FunctionGraphsJPanel
     * object
     *
     * @param x The feature to be added to the VerticalLine attribute
     * @param cl The feature to be added to the VerticalLine attribute
     * @return Description of the Return Value
     */
    public int addVerticalLine(double x, Color cl) {
        synchronized (this) {
            vLinesV.add(x);
            vLinesColorV.add(cl);
        }
        updateGraphJPanel();
        if (draggedVerLinesListener != null) {
            draggedVerLinesListener.actionPerformed(draggedVerLinesEvent);
        }
        return vLinesV.size() - 1;
    }

    /**
     * Adds a feature to the HorizontalLine attribute of the
     * FunctionGraphsJPanel object
     *
     * @param y The feature to be added to the HorizontalLine attribute
     * @param cl The feature to be added to the HorizontalLine attribute
     * @return Description of the Return Value
     */
    public int addHorizontalLine(double y, Color cl) {
        synchronized (this) {
            hLinesV.add(y);
            hLinesColorV.add(cl);
        }
        updateGraphJPanel();
        if (draggedHorLinesListener != null) {
            draggedHorLinesListener.actionPerformed(draggedHorLinesEvent);
        }
        return hLinesV.size() - 1;
    }

    /**
     * Sets the verticalLineValue attribute of the FunctionGraphsJPanel object
     *
     * @param x The new verticalLineValue value
     * @param index The new verticalLineValue value
     */
    public void setVerticalLineValue(double x, int index) {
        if (index < vLinesV.size() && index >= 0) {
            synchronized (this) {
                vLinesV.remove(index);
                vLinesV.add(index, x);
            }
            updateGraphJPanel();
            if (draggedVerLinesListener != null) {
                draggedVerLinesListener.actionPerformed(draggedVerLinesEvent);
            }
        }
    }

    /**
     * Sets the horizontalLineValue attribute of the FunctionGraphsJPanel object
     *
     * @param y The new horizontalLineValue value
     * @param index The new horizontalLineValue value
     */
    public void setHorizontalLineValue(double y, int index) {
        if (index < hLinesV.size() && index >= 0) {
            synchronized (this) {
                hLinesV.remove(index);
                hLinesV.add(index, y);
            }
            updateGraphJPanel();
            if (draggedHorLinesListener != null) {
                draggedHorLinesListener.actionPerformed(draggedHorLinesEvent);
            }
        }
    }

    /**
     * Sets the verticalLineColor attribute of the FunctionGraphsJPanel object
     *
     * @param cl The new verticalLineColor value
     * @param index The new verticalLineColor value
     */
    public synchronized void setVerticalLineColor(Color cl, int index) {
        if (index < vLinesColorV.size() && index >= 0) {
            vLinesColorV.remove(index);
            vLinesColorV.add(index, cl);
            updateGraphJPanel();
        }
    }

    /**
     * Sets the horizontalLineColor attribute of the FunctionGraphsJPanel object
     *
     * @param cl The new horizontalLineColor value
     * @param index The new horizontalLineColor value
     */
    public synchronized void setHorizontalLineColor(Color cl, int index) {
        if (index < hLinesColorV.size() && index >= 0) {
            hLinesColorV.remove(index);
            hLinesColorV.add(index, cl);
            updateGraphJPanel();
        }
    }

    /**
     * Returns the verticalValue attribute of the FunctionGraphsJPanel object
     *
     * @param index Description of the Parameter
     * @return The verticalValue value
     */
    public synchronized double getVerticalValue(int index) {
        if (index < vLinesV.size() && index >= 0) {
            return vLinesV.get(index);
        }
        return -Double.MAX_VALUE;
    }

    /**
     * Returns the horizontalValue attribute of the FunctionGraphsJPanel object
     *
     * @param index Description of the Parameter
     * @return The horizontalValue value
     */
    public synchronized double getHorizontalValue(int index) {
        if (index < hLinesV.size() && index >= 0) {
            return hLinesV.get(index);
        }
        return -Double.MAX_VALUE;
    }

    /**
     * Description of the Method
     *
     * @param index Description of the Parameter
     */
    public synchronized void removeVerticalValue(int index) {
        if (index < vLinesV.size() && index >= 0) {
            vLinesV.remove(index);
            vLinesColorV.remove(index);
            updateGraphJPanel();
        }
    }

    /**
     * Description of the Method
     *
     * @param index Description of the Parameter
     */
    public synchronized void removeHorizontalValue(int index) {
        if (index < hLinesV.size() && index >= 0) {
            hLinesV.remove(index);
            hLinesColorV.remove(index);
            updateGraphJPanel();
        }
    }

    /**
     * Description of the Method
     */
    public synchronized void removeVerticalValues() {
        vLinesV.clear();
        vLinesColorV.clear();
        updateGraphJPanel();
    }

    /**
     * Description of the Method
     */
    public synchronized void removeHorizontalValues() {
        hLinesV.clear();
        hLinesColorV.clear();
        updateGraphJPanel();
    }

    /**
     * Sets the draggingHorLinesGraphMode attribute of the FunctionGraphsJPanel
     * object
     *
     * @param dragLinesModeYes The new draggingHorLinesGraphMode value
     */
    public void setDraggingHorLinesGraphMode(boolean dragLinesModeYes) {
        dragHorLinesModeYes = dragLinesModeYes;
        dragHorLinesModeButton.setSelected(dragHorLinesModeYes);
        updateGraphJPanel();
    }

    /**
     * Sets the draggingVerLinesGraphMode attribute of the FunctionGraphsJPanel
     * object
     *
     * @param dragLinesModeYes The new draggingVerLinesGraphMode value
     */
    public void setDraggingVerLinesGraphMode(boolean dragLinesModeYes) {
        dragVerLinesModeYes = dragLinesModeYes;
        dragVerLinesModeButton.setSelected(dragVerLinesModeYes);
        updateGraphJPanel();

    }

    /**
     * Sets the horLinesButtonVisible attribute of the FunctionGraphsJPanel
     * object
     *
     * @param vs The new horLinesButtonVisible value
     */
    public void setHorLinesButtonVisible(boolean vs) {
        horLinesModeButtonVisible = vs;
        dragHorLinesModeYes = vs;
        dragHorLinesModeButton.setSelected(dragHorLinesModeYes);
        remove(dragHorLinesModeButton);
        if (vs) {
            add(dragHorLinesModeButton);
        }
        updateGraphJPanel();
    }

    /**
     * Sets the verLinesButtonVisible attribute of the FunctionGraphsJPanel
     * object
     *
     * @param vs The new verLinesButtonVisible value
     */
    public void setVerLinesButtonVisible(boolean vs) {
        verLinesModeButtonVisible = vs;
        dragVerLinesModeYes = vs;
        dragVerLinesModeButton.setSelected(dragVerLinesModeYes);
        remove(dragVerLinesModeButton);
        if (vs) {
            add(dragVerLinesModeButton);
        }
        updateGraphJPanel();
    }

    /**
     * Adds a feature to the DraggedHorLinesListener attribute of the
     * FunctionGraphsJPanel object
     *
     * @param draggedHorLinesListenerIn The feature to be added to the
     * DraggedHorLinesListener attribute
     */
    public void addDraggedHorLinesListener(ActionListener draggedHorLinesListenerIn) {
        draggedHorLinesListener = draggedHorLinesListenerIn;
    }

    /**
     * Adds a feature to the DraggedVerLinesListener attribute of the
     * FunctionGraphsJPanel object
     *
     * @param draggedVerLinesListenerIn The feature to be added to the
     * DraggedVerLinesListener attribute
     */
    public void addDraggedVerLinesListener(ActionListener draggedVerLinesListenerIn) {
        draggedVerLinesListener = draggedVerLinesListenerIn;
    }

    /**
     * Sets the draggedHorLinesMotionListen attribute of the
     * FunctionGraphsJPanel object
     *
     * @param draggedHorLinesMotionListenYesIn The new
     * draggedHorLinesMotionListen value
     */
    public void setDraggedHorLinesMotionListen(boolean draggedHorLinesMotionListenYesIn) {
        draggedHorLinesMotionListenYes = draggedHorLinesMotionListenYesIn;
    }

    /**
     * Sets the draggedVerLinesMotionListen attribute of the
     * FunctionGraphsJPanel object
     *
     * @param draggedVerLinesMotionListenYesIn The new
     * draggedVerLinesMotionListen value
     */
    public void setDraggedVerLinesMotionListen(boolean draggedVerLinesMotionListenYesIn) {
        draggedVerLinesMotionListenYes = draggedVerLinesMotionListenYesIn;
    }

    /**
     * Returns the draggedLineIndex attribute of the FunctionGraphsJPanel object
     *
     * @return The draggedLineIndex value
     */
    public synchronized int getDraggedLineIndex() {
        if (mouseDraggedTaskType == 1 || mouseDraggedTaskType == 2) {
            return draggedLinesIndex;
        }
        return -1;
    }

    /**
     * Returns the nearestHorizontalLineIndex attribute of the
     * FunctionGraphsJPanel object
     *
     * @param y Description of the Parameter
     * @return The nearestHorizontalLineIndex value
     */
    private int getNearestHorizontalLineIndex(double y) {
        int index = -1;
        double dMin = Double.MAX_VALUE;
        double dMinG;
        double dMaxG;
        double d;
        if (dragHorLinesModeYes) {
            if (hLinesV.isEmpty()) {
                return index;
            }
            dMinG = yMin;
            dMaxG = yMax;
            for (int i = 0; i < hLinesV.size(); i++) {
                d = hLinesV.get(i);
                if (d < dMinG) {
                    d = dMinG;
                }
                if (d > dMaxG) {
                    d = dMaxG;
                }
                d = Math.abs(y - d);
                if (dMin > d) {
                    dMin = d;
                    index = i;
                }
            }
        }
        return index;
    }

    /**
     * Returns the nearestVerticalLineIndex attribute of the
     * FunctionGraphsJPanel object
     *
     * @param x Description of the Parameter
     * @return The nearestVerticalLineIndex value
     */
    private int getNearestVerticalLineIndex(double x) {
        int index = -1;
        double dMin = Double.MAX_VALUE;
        double dMinG;
        double dMaxG;
        double d;
        if (dragVerLinesModeYes) {
            if (vLinesV.isEmpty()) {
                return index;
            }
            dMinG = xMin;
            dMaxG = xMax;
            for (int i = 0; i < vLinesV.size(); i++) {
                d = vLinesV.get(i);
                if (d < dMinG) {
                    d = dMinG;
                }
                if (d > dMaxG) {
                    d = dMaxG;
                }
                d = Math.abs(x - d);
                if (dMin > d) {
                    dMin = d;
                    index = i;
                }
            }
        }
        return index;
    }

    //-----------------------------------------------
    //methods related to the appearance of the grids
    //-----------------------------------------------
    /**
     * Sets the gridLinesVisibleX attribute of the FunctionGraphsJPanel object
     *
     * @param vsbl The new gridLinesVisibleX value
     */
    public void setGridLinesVisibleX(boolean vsbl) {
        gridLineOnX = vsbl;
        updateGraphJPanel();
    }

    /**
     * Returns the gridLinesVisibleX attribute of the FunctionGraphsJPanel
     * object
     *
     * @return The gridLinesVisibleX value
     */
    public boolean getGridLinesVisibleX() {
        return gridLineOnX;
    }

    /**
     * Sets the gridLinesVisibleY attribute of the FunctionGraphsJPanel object
     *
     * @param vsbl The new gridLinesVisibleY value
     */
    public void setGridLinesVisibleY(boolean vsbl) {
        gridLineOnY = vsbl;
        updateGraphJPanel();
    }

    /**
     * Returns the gridLinesVisibleY attribute of the FunctionGraphsJPanel
     * object
     *
     * @return The gridLinesVisibleY value
     */
    public boolean getGridLinesVisibleY() {
        return gridLineOnY;
    }

    //----------------------------------------------
    //methods related to the axis' parameters dialog
    //----------------------------------------------
    /**
     * Returns the parentFrameOrDialog attribute of the FunctionGraphsJPanel
     * object
     *
     * @return The parentFrameOrDialog value
     */
    private Component getParentFrameOrDialog() {
        Component cmp = this.getParent();
        while ((cmp != null) && (!(cmp instanceof Frame)) && (!(cmp instanceof Dialog))) {
            cmp = cmp.getParent();
        }
        if (cmp == null) {
            return null;
        }
        return cmp;
    }

    //Method related to VERTICAL and HORIZONTAL limits listeners
    /**
     * Adds a feature to the HorLimitsListener attribute of the
     * FunctionGraphsJPanel object
     *
     * @param al The feature to be added to the HorLimitsListener attribute
     */
    public void addHorLimitsListener(ActionListener al) {
        horLimListenersV.add(al);
        al.actionPerformed(horLimEvent);
    }

    /**
     * Adds a feature to the VerLimitsListener attribute of the
     * FunctionGraphsJPanel object
     *
     * @param al The feature to be added to the VerLimitsListener attribute
     */
    public void addVerLimitsListener(ActionListener al) {
        verLimListenersV.add(al);
        al.actionPerformed(verLimEvent);
    }

    /**
     * Description of the Method
     *
     * @param al Description of the Parameter
     */
    public void removeHorLimitsListener(ActionListener al) {
        horLimListenersV.remove(al);
    }

    /**
     * Description of the Method
     *
     * @param al Description of the Parameter
     */
    public void removeVerLimitsListener(ActionListener al) {
        verLimListenersV.remove(al);
    }

    /**
     * Returns the horLimitsListeners attribute of the FunctionGraphsJPanel
     * object
     *
     * @return The horLimitsListeners value
     */
    public Vector<ActionListener> getHorLimitsListeners() {
        return new Vector<>(horLimListenersV);
    }

    /**
     * Returns the verLimitsListeners attribute of the FunctionGraphsJPanel
     * object
     *
     * @return The verLimitsListeners value
     */
    public Vector<ActionListener> getVerLimitsListeners() {
        return new Vector<>(verLimListenersV);
    }

    /**
     * Returns the axisParamDialog attribute of the FunctionGraphsJPanel object
     *
     * @return The axisParamDialog value
     */
    private JDialog getAxisParamDialog() {
        Object frameOrDialog = getParentFrameOrDialog();
        if (frameOrDialog == null) {
            return null;
        }
        if (parentFrameOrDialog == null) {
            parentFrameOrDialog = frameOrDialog;
        }
        if (parentFrameOrDialog != frameOrDialog) {
            parentFrameOrDialog = frameOrDialog;
            if (parentFrameOrDialog instanceof Frame) {
                axisDialog = new JDialog((Frame) parentFrameOrDialog, true);
            }
            if (parentFrameOrDialog instanceof Dialog) {
                axisDialog = new JDialog((Dialog) parentFrameOrDialog, true);
            }
        }
        if (axisDialog == null) {
            if (parentFrameOrDialog instanceof Frame) {
                axisDialog = new JDialog((Frame) parentFrameOrDialog, true);
            }
            if (parentFrameOrDialog instanceof Dialog) {
                axisDialog = new JDialog((Dialog) parentFrameOrDialog, true);
            }
        }
        if (axisDialog != null) {
            axisDialog.setLocationRelativeTo(this);
        }
        return axisDialog;
    }

    //----------------------------------------------
    //methods related to Legend
    //----------------------------------------------
    /**
     * Sets the legendVisible attribute of the FunctionGraphsJPanel object
     *
     * @param vs The new legendVisible value
     */
    public void setLegendVisible(boolean vs) {
        legendButton.setSelected(vs);
        legend.setVisible(vs);
        updateGraphJPanel();
    }

    /**
     * Returns the legendVisible attribute of the FunctionGraphsJPanel object
     *
     * @return The legendVisible value
     */
    public boolean isLegendVisible() {
        return legend.isVisible();
    }

    /**
     * Sets the legendButtonVisible attribute of the FunctionGraphsJPanel object
     *
     * @param vs The new legendButtonVisible value
     */
    public void setLegendButtonVisible(boolean vs) {
        legendButtonVisible = vs;
        remove(legendButton);
        if (vs) {
            add(legendButton);
            legendButton.setSelected(legendButton.isSelected());
        }
        updateGraphJPanel();
    }

    /**
     * Sets the legendKeyString attribute of the FunctionGraphsJPanel object
     *
     * @param legendKeyString The new legendKeyString value
     */
    public void setLegendKeyString(String legendKeyString) {
        this.legendKeyString = legendKeyString;
        legend.setKeyString(legendKeyString);
    }

    /**
     * Returns the legendKeyString attribute of the FunctionGraphsJPanel object
     *
     * @return The legendKeyString value
     */
    public String getLegendKeyString() {
        return legendKeyString;
    }

    /**
     * Sets the legendPosition attribute of the FunctionGraphsJPanel object
     *
     * @param legendPosition The new legendPosition value
     */
    public void setLegendPosition(int legendPosition) {
        legend.setPosition(legendPosition);
    }

    /**
     * Sets the legendFont attribute of the FunctionGraphsJPanel object
     *
     * @param fnt The new legendFont value
     */
    public void setLegendFont(Font fnt) {
        legend.setFont(fnt);
    }

    /**
     * Sets the legendColor attribute of the FunctionGraphsJPanel object
     *
     * @param cl The new legendColor value
     */
    public void setLegendColor(Color cl) {
        legend.setColor(cl);
    }

    /**
     * Sets the legendBackground attribute of the FunctionGraphsJPanel object
     *
     * @param cl The new legendBackground value
     */
    public void setLegendBackground(Color cl) {
        legend.setBackground(cl);
    }

    //----------------------------------------------
    //method updates the limits and number of points
    //in all graphs
    //----------------------------------------------
    /**
     * Description of the Method
     */
    private void updateData() {
        if (graphChoosingYes) {
            unChooseGraph();
        }

        double xMinIn = Double.MAX_VALUE;
        double yMinIn = Double.MAX_VALUE;
        double xMaxIn = -Double.MAX_VALUE;
        double yMaxIn = -Double.MAX_VALUE;

        nTotalGraphPoints = 0;
        nTotalCurvePoints = 0;
        int nColorSurfaceSize = 0;
        if (!graphDataV.isEmpty()
                || colorSurfaceData != null || !curveDataV.isEmpty()) {

            double d;
            BasicGraphData grD = null;
            for (int i = 0; i < graphDataV.size(); i++) {
                grD = graphDataV.get(i);
                if (grD.getNumbOfPoints() > 0) {
                    d = grD.getMinX();
                    if (d < xMinIn) {
                        xMinIn = d;
                    }
                    d = grD.getMinY();
                    if (d < yMinIn) {
                        yMinIn = d;
                    }
                    d = grD.getMaxX();
                    if (d > xMaxIn) {
                        xMaxIn = d;
                    }
                    d = grD.getMaxY();
                    if (d > yMaxIn) {
                        yMaxIn = d;
                    }
                    nTotalGraphPoints = nTotalGraphPoints + grD.getNumbOfPoints();
                }
            }

            if (colorSurfaceData != null) {
                nColorSurfaceSize = colorSurfaceData.getSizeX() * colorSurfaceData.getSizeY();
                d = colorSurfaceData.getMinX();
                if (d < xMinIn) {
                    xMinIn = d;
                }
                d = colorSurfaceData.getMinY();
                if (d < yMinIn) {
                    yMinIn = d;
                }
                d = colorSurfaceData.getMaxX();
                if (d > xMaxIn) {
                    xMaxIn = d;
                }
                d = colorSurfaceData.getMaxY();
                if (d > yMaxIn) {
                    yMaxIn = d;
                }
            }

            if (!curveDataV.isEmpty()) {
                for (int i = 0; i < curveDataV.size(); i++) {
                    CurveData crvD = curveDataV.get(i);
                    if (crvD.getSize() > 0) {
                        d = crvD.getMinX();
                        if (d < xMinIn) {
                            xMinIn = d;
                        }
                        d = crvD.getMinY();
                        if (d < yMinIn) {
                            yMinIn = d;
                        }
                        d = crvD.getMaxX();
                        if (d > xMaxIn) {
                            xMaxIn = d;
                        }
                        d = crvD.getMaxY();
                        if (d > yMaxIn) {
                            yMaxIn = d;
                        }
                        nTotalCurvePoints++;
                    }
                }
            }

            innerGridLimits.initialize();
            innerGridLimits.setXmin(xMinIn);
            innerGridLimits.setYmin(yMinIn);
            innerGridLimits.setXmax(xMaxIn);
            innerGridLimits.setYmax(yMaxIn);
            //set the smart limits for inner grid object
            if (nTotalCurvePoints > 0
                    || nTotalGraphPoints > 0
                    || nColorSurfaceSize > 0) {
                innerGridLimits.setSmartLimits();
            }

        }
        updateGraphJPanel();

    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     */
    @Override
    protected synchronized void paintComponent(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;

        if (offScreenImageOn) {
            if (offScreenImage == null) {
                offScreenImage = createVolatileImage(getWidth(), getHeight());
            }
            if (getWidth() != ((VolatileImage) offScreenImage).getWidth()
                    || getHeight() != ((VolatileImage) offScreenImage).getHeight()) {
                offScreenImage = createVolatileImage(getWidth(), getHeight());
            }

            try {
                do {
                    GraphicsConfiguration gc = getGraphicsConfiguration();
                    int valCode = ((VolatileImage) offScreenImage).validate(gc);
                    if (valCode == VolatileImage.IMAGE_INCOMPATIBLE) {
                        offScreenImage = createVolatileImage(getWidth(), getHeight());
                    }
                    Graphics2D og = (Graphics2D) offScreenImage.getGraphics();

                    drawGraphicsData(og, getWidth(), getHeight());

                    og.dispose();
                    g.drawImage(offScreenImage, 0, 0, this);
                } while (((VolatileImage) offScreenImage).contentsLost());
            } catch (Exception e) {
                LOGGER.log(Level.INFO, "Exception during paintComponent ", e);
            }
            return;
        }

        drawGraphicsData(g2D, getWidth(), getHeight());
    }

    /**
     * Update the graph panel, not data
     */
    private void updateGraphJPanel() {
        repaint(0, 0, 0, this.getWidth(), this.getHeight());
    }

    /**
     * Update data and the graph panel
     */
    public void refreshGraphJPanel() {
        updateData();
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     * @param scrW Description of the Parameter
     * @param scrH Description of the Parameter
     */
    private void drawGraphicsData(Graphics2D g, int scrW, int scrH) {
        screenW = scrW;
        screenH = scrH;
        //-----------------------------------------------
        //background color
        //-----------------------------------------------
        Color backgroundInitial = g.getBackground();

        if (bkgGraphAreaColor != null) {
            g.setBackground(bkgGraphAreaColor);
        } else {
            bkgGraphAreaColor = g.getBackground();
        }
        g.clearRect(0, 0, scrW, scrH);

        //-----------------------------------------------
        //current draw color
        //-----------------------------------------------
        Color colorInitial = g.getColor();

        if (nTotalGraphPoints == 0 && colorSurfaceData == null && nTotalCurvePoints == 0) {
            g.setColor(Color.RED);
            g.drawString("NO DATA", scrW / 2, scrH / 2);
            //===================================
            //you suppose to see only greed lines
            //===================================
        }

        //-----------------------------------------------
        //initial tarnsform
        //-----------------------------------------------
        AffineTransform transInitial = g.getTransform();

        //-----------------------------------------------
        //initial stroke
        //-----------------------------------------------
        Stroke strokeInitial = g.getStroke();

        //-----------------------------------------------
        //initial Font
        //-----------------------------------------------
        Font fontInitial = g.getFont();

        //-----------------------------------------------------
        //definition of the max and min for axises
        //-----------------------------------------------------
        GridLimits currentGridLimitsIn;
        synchronized (this) {
            currentGridLimitsIn = externalGridLimits;
        }
        if (currentGridLimitsIn != null) {
            currentGridLimitsIn.setGridLimitsSwitch(true);
        } else {
            currentGridLimitsIn = innerGridLimits;
        }

        if (!zoomGridLimitsV.isEmpty()) {
            if (mouseDrugged && mouseDraggedTaskType == 0) {
                if (zoomGridLimitsV.size() > 1) {
                    currentGridLimitsIn = zoomGridLimitsV.get(zoomGridLimitsV.size() - 2);
                    currentGridLimitsIn.setGridLimitsSwitch(true);
                }
            } else {
                currentGridLimitsIn = zoomGridLimitsV.lastElement();
                currentGridLimitsIn.setGridLimitsSwitch(true);
            }
        }
        double xMinIn = innerGridLimits.getMinX();
        double yMinIn = innerGridLimits.getMinY();
        double xMaxIn = innerGridLimits.getMaxX();
        double yMaxIn = innerGridLimits.getMaxY();
        if (currentGridLimitsIn != null && currentGridLimitsIn.getGridLimitsSwitch()) {
            if (currentGridLimitsIn.isSetXmin()) {
                xMinIn = currentGridLimitsIn.getMinX();
            }
            if (currentGridLimitsIn.isSetYmin()) {
                yMinIn = currentGridLimitsIn.getMinY();
            }
            if (currentGridLimitsIn.isSetXmax()) {
                xMaxIn = currentGridLimitsIn.getMaxX();
            }
            if (currentGridLimitsIn.isSetYmax()) {
                yMaxIn = currentGridLimitsIn.getMaxY();
            }
        }
        GridLimits currentGridLimits = currentGridLimitsIn;

        if (xMinIn == xMaxIn) {
            xMinIn = xMinIn - 0.5;
            xMaxIn = xMaxIn + 0.5;
        }
        if (yMinIn == yMaxIn) {
            yMinIn = yMinIn - 0.5;
            yMaxIn = yMaxIn + 0.5;
        }

        if (xMinIn == -Double.MAX_VALUE) {
            xMinIn = 0.0;
        }
        if (xMaxIn == Double.MAX_VALUE) {
            xMaxIn = 1.0;
        }
        if (yMinIn == -Double.MAX_VALUE) {
            yMinIn = 0.0;
        }
        if (yMaxIn == Double.MAX_VALUE) {
            yMaxIn = 1.0;
        }

        //set boolean variables about x and y limits changes
        //the call of listeners should occur at the end of this method
        boolean xLimChanged = false;
        boolean yLimChanged = false;

        if (xMin != xMinIn || xMax != xMaxIn) {
            xLimChanged = true;
        }
        if (yMin != yMinIn || yMax != yMaxIn) {
            yLimChanged = true;
        }

        xMin = xMinIn;
        xMax = xMaxIn;
        yMin = yMinIn;
        yMax = yMaxIn;

        //-----------------------------------------------------------
        //definition the graph area bounds (depending on fonts etc.)
        //-----------------------------------------------------------
        int fSizeName = 0;
        if (nameOfGraph != null) {
            g.setFont(nameOfGraphFont);
            fSizeName = g.getFontMetrics().getHeight() + 4;
        }
        if (chooseModeButtonVisible) {
            fSizeName = Math.max(fSizeName, (int) chooseModeButton.getPreferredSize().getHeight());
        }
        if (horLinesModeButtonVisible) {
            fSizeName = Math.max(fSizeName, (int) dragHorLinesModeButton.getPreferredSize().getHeight());
        }
        if (verLinesModeButtonVisible) {
            fSizeName = Math.max(fSizeName, (int) dragVerLinesModeButton.getPreferredSize().getHeight());
        }
        if (legendButtonVisible) {
            fSizeName = Math.max(fSizeName, (int) legendButton.getPreferredSize().getHeight());
        }

        fSizeX = 0;
        if (nameX != null) {
            g.setFont(nameXFont);
            fSizeX = g.getFontMetrics().getHeight() + 4;
        }

        fSizeY = 0;
        if (nameY != null) {
            g.setFont(nameYFont);
            fSizeY = g.getFontMetrics().getHeight() + 4;
        }

        int fSizeNumbXhor = 0;
        if (gridYmarkerOn) {
            int iS1 = 0;
            int iS2 = 0;
            g.setFont(numberFont);
            iS1 = g.getFontMetrics().stringWidth(currentGridLimits.getNumberFormatX().format(xMax));
            iS2 = g.getFontMetrics().stringWidth(currentGridLimits.getNumberFormatX().format(xMin));
            fSizeNumbXhor = Math.max(iS1, iS2) + 4;
        }

        int fSizeNumbXver = 0;
        if (gridXmarkerOn) {
            g.setFont(numberFont);
            fSizeNumbXver = g.getFontMetrics().getHeight();
        }

        int fSizeNumbYhor = 0;
        if (gridYmarkerOn) {
            int iS1 = 0;
            int iS2 = 0;
            g.setFont(numberFont);
            iS1 = g.getFontMetrics().stringWidth(currentGridLimits.getNumberFormatY().format(yMax));
            iS2 = g.getFontMetrics().stringWidth(currentGridLimits.getNumberFormatY().format(yMin));
            fSizeNumbYhor = Math.max(iS1, iS2) + 4;
        }

        int fSizeNumbYver = 0;
        if (gridYmarkerOn) {
            g.setFont(numberFont);
            fSizeNumbYver = g.getFontMetrics().getHeight();
        }

        //---------------------------------
        //definition of the OffSets
        //---------------------------------
        xLOffSet = 2 + fSizeY + 2 + fSizeNumbYhor + 4 + 5;
        xROffSet = 5;
        yUOffSet = 4 + fSizeName;
        yBOffSet = 2 + fSizeX + fSizeNumbXver + 2 + 5;

        if ((xLOffSet + xROffSet + 4) > scrW || (yBOffSet + yUOffSet + 4) > scrH) {
            return;
        }

        //-----------------------------------------------
        //scales and offsets
        //-----------------------------------------------
        int xAxisLength = scrW - xLOffSet - xROffSet;
        int yAxisLength = scrH - yUOffSet - yBOffSet;
        if (xAxisLength < 0 || yAxisLength < 0) {
            return;
        }

        double scaleXOld = scaleX;
        double scaleYOld = scaleY;

        scaleX = xAxisLength / (xMax - xMin);
        scaleY = -yAxisLength / (yMax - yMin);

        if (scaleX != scaleXOld) {
            xLimChanged = true;
        }
        if (scaleY != scaleYOld) {
            yLimChanged = true;
        }

        //------------------------------
        //tick numbers calculation
        //------------------------------
        int nMajorTksX;
        int nMinorTksX = currentGridLimits.getNumMinorTicksX();
        int nMajorTksY;
        int nMinorTksY = currentGridLimits.getNumMinorTicksY();

        if (!currentGridLimits.getMajorTicksOnX()) {
            if (fSizeNumbXhor != 0) {
                nMajorTksX = xAxisLength / (2 * fSizeNumbXhor);
            } else {
                nMajorTksX = 0;
            }
        } else {
            nMajorTksX = currentGridLimits.getNumMajorTicksX();
        }

        if (!currentGridLimits.getMajorTicksOnY()) {
            if (fSizeNumbYver != 0) {
                nMajorTksY = yAxisLength / (2 * fSizeNumbYver);
            } else {
                nMajorTksY = 0;
            }
        } else {
            nMajorTksY = currentGridLimits.getNumMajorTicksY();
        }

        int x1;
        int x2;
        int y1;
        int y2;

        //------------------------------
        //draw the colored surface plot
        //------------------------------
        if (colorSurfaceData != null) {
            int nStripesX = colorSurfaceData.getScreenSizeX();
            int nStripesY = colorSurfaceData.getScreenSizeY();
            double xStripeW = (xMax - xMin) / nStripesX;
            double yStripeW = (yMax - yMin) / nStripesY;
            double x0;
            double y0;
            for (int i = 0; i < nStripesX; i++) {
                for (int j = 0; j < nStripesY; j++) {
                    x0 = i * xStripeW + xMin;
                    y0 = yMax - j * yStripeW;
                    x1 = getScreenX(x0);
                    x2 = getScreenX(x0 + xStripeW);
                    y1 = getScreenY(y0);
                    y2 = getScreenY(y0 - yStripeW);
                    x0 += 0.5 * xStripeW;
                    y0 -= 0.5 * yStripeW;
                    g.setBackground(colorSurfaceData.getColor(x0, y0));
                    g.clearRect(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
                }
            }
        }

        //------------------------------
        //draw grid lines
        //------------------------------
        if (gridLineOnX && nMajorTksX > 1) {
            if (gridLineColor == null) {
                g.setColor(bkgGraphAreaColor.darker());
            } else {
                g.setColor(gridLineColor);
            }
            double step = (xMax - xMin) / (nMinorTksX * (nMajorTksX - 1) + nMajorTksX - 1);
            y1 = yUOffSet;
            y2 = scrH - yBOffSet;
            for (int i = 1, n = nMinorTksX * (nMajorTksX - 1) + nMajorTksX - 1; i < n; i++) {
                x1 = getScreenX(xMin + i * step);
                x2 = x1;
                g.drawLine(x1, y1, x2, y2);
            }
        }

        if (gridLineOnY && nMajorTksY > 1) {
            if (gridLineColor == null) {
                g.setColor(bkgGraphAreaColor.darker());
            } else {
                g.setColor(gridLineColor);
            }
            double step = (yMax - yMin) / (nMinorTksY * (nMajorTksY - 1) + nMajorTksY - 1);
            x1 = xLOffSet;
            x2 = scrW - xROffSet;
            for (int i = 1, n = nMinorTksY * (nMajorTksY - 1) + nMajorTksY - 1; i < n; i++) {
                y1 = getScreenY(yMin + i * step);
                y2 = y1;
                g.drawLine(x1, y1, x2, y2);
            }
        }

        //-----------------------------------------------
        //start draw curves
        //-----------------------------------------------
        if (nTotalCurvePoints > 0) {
            int pointX;
            int pointY;
            int pointH;
            int pointW;
            Stroke strokeIniCurve = g.getStroke();
            for (int i = 0; i < curveDataV.size(); i++) {
                CurveData crvD = curveDataV.get(i);
                g.setStroke(crvD.getStroke());
                g.setColor(crvD.getColor());
                if (crvD.getSize() > 1) {
                    for (int j = 0; j < crvD.getSize() - 1; j++) {
                        x1 = getScreenX(crvD.getX(j));
                        x2 = getScreenX(crvD.getX(j + 1));
                        y1 = getScreenY(crvD.getY(j));
                        y2 = getScreenY(crvD.getY(j + 1));
                        g.drawLine(x1, y1, x2, y2);
                    }
                } else {
                    if (crvD.getSize() == 1) {
                        pointH = crvD.getLineWidth();
                        pointW = crvD.getLineWidth();
                        pointX = getScreenX(crvD.getX(0)) - pointW / 2;
                        pointY = getScreenY(crvD.getY(0)) - pointH / 2;
                        g.fillOval(pointX, pointY, pointW, pointH);
                    }
                }
            }
            g.setStroke(strokeIniCurve);
        }

        //-----------------------------------------------
        //start draw graph points, curves, errors etc.
        //-----------------------------------------------
        //------------------------------
        //draw interpolated points graph
        //------------------------------
        for (int i = 0, n = graphDataV.size(); i < n; i++) {
            BasicGraphData lgd = graphDataV.get(i);
            Object localLockObject = lgd.getLockObject();
            synchronized (localLockObject) {
                if (!lgd.getDrawLinesOn()) {
                    continue;
                }
                if (lgd.getNumbOfPoints() < 2) {
                    continue;
                }
                g.setStroke(lgd.getStroke());
                Color lineColor = graphColorV.get(i);
                synchronized (this) {
                    if (lineColor == null) {
                        if (lgd.getGraphColor() == null) {
                            lineColor = lineDefaultColor;
                        } else {
                            lineColor = lgd.getGraphColor();
                        }
                    }
                    g.setColor(lineColor);
                }
                if (lgd instanceof CubicSplineGraphData) {
                    for (int j = 0, nGrPoint = lgd.getNumbOfInterpPoints() - 1; j < nGrPoint; j++) {
                        x1 = getScreenX(lgd.getInterpX(j));
                        x2 = getScreenX(lgd.getInterpX(j + 1));
                        y1 = getScreenY(lgd.getInterpY(j));
                        y2 = getScreenY(lgd.getInterpY(j + 1));
                        g.drawLine(x1, y1, x2, y2);
                    }
                } else {
                    for (int j = 0, nGrPoint = lgd.getNumbOfPoints() - 1; j < nGrPoint; j++) {
                        x1 = getScreenX(lgd.getX(j));
                        x2 = getScreenX(lgd.getX(j + 1));
                        y1 = getScreenY(lgd.getY(j));
                        y2 = getScreenY(lgd.getY(j + 1));
                        g.drawLine(x1, y1, x2, y2);
                    }
                }
            }
        }

        g.setStroke(strokeInitial);

        //------------------------------
        //draw true points as points
        //------------------------------
        int ovalX;

        //------------------------------
        //draw true points as points
        //------------------------------
        int ovalY;

        //------------------------------
        //draw true points as points
        //------------------------------
        int ovalW;

        //------------------------------
        //draw true points as points
        //------------------------------
        int ovalH;
        for (int i = 0, n = graphDataV.size(); i < n; i++) {
            BasicGraphData lgd = graphDataV.get(i);
            Object localLockObject = lgd.getLockObject();
            synchronized (localLockObject) {
                if (lgd.getNumbOfPoints() < 1) {
                    continue;
                }
                if (!lgd.getDrawPointsOn()) {
                    continue;
                }
                Color lineColor = graphColorV.get(i);
                synchronized (this) {
                    if (lineColor == null) {
                        if (lgd.getGraphColor() == null) {
                            lineColor = lineDefaultColor;
                        } else {
                            lineColor = lgd.getGraphColor();
                        }
                    }
                    g.setColor(lineColor);
                }

                if (lgd.getGraphPointShape() == null) {

                    ovalW = lgd.getGraphPointSize();
                    ovalH = lgd.getGraphPointSize();

                    for (int j = 0, nGrPoint = lgd.getNumbOfPoints(); j < nGrPoint; j++) {
                        ovalX = getScreenX(lgd.getX(j)) - ovalW / 2;
                        ovalY = getScreenY(lgd.getY(j)) - ovalH / 2;
                        g.fillOval(ovalX, ovalY, ovalW, ovalH);
                    }
                } else {
                    for (int j = 0, nGrPoint = lgd.getNumbOfPoints(); j < nGrPoint; j++) {
                        ovalX = getScreenX(lgd.getX(j));
                        ovalY = getScreenY(lgd.getY(j));
                        g.translate(ovalX, ovalY);
                        if (lgd.isGraphPointShapeFilled()) {
                            g.fill(lgd.getGraphPointShape());
                        } else {
                            g.draw(lgd.getGraphPointShape());
                        }
                        g.translate(-ovalX, -ovalY);
                    }
                }
            }
        }

        //------------------------------
        //draw errors for true points
        //------------------------------
        int xPosition;

        //------------------------------
        //draw errors for true points
        //------------------------------
        int yLow;

        //------------------------------
        //draw errors for true points
        //------------------------------
        int yUpp;

        for (int i = 0, n = graphDataV.size(); i < n; i++) {
            BasicGraphData lgd = graphDataV.get(i);
            Object localLockObject = lgd.getLockObject();
            synchronized (localLockObject) {
                if (lgd.getNumbOfPoints() < 1) {
                    continue;
                }
                if (lgd.getMaxErr() == 0.) {
                    continue;
                }
                if (!lgd.getDrawPointsOn()) {
                    continue;
                }
                g.setStroke(lgd.getStroke());
                Color lineColor = graphColorV.get(i);
                synchronized (this) {
                    if (lineColor == null) {
                        if (lgd.getGraphColor() == null) {
                            lineColor = lineDefaultColor;
                        } else {
                            lineColor = lgd.getGraphColor();
                        }
                    }
                    g.setColor(lineColor);
                }
                for (int j = 0, nGrPoint = lgd.getNumbOfPoints(); j < nGrPoint; j++) {
                    if (lgd.getErr(j) == 0.) {
                        continue;
                    }
                    yLow = getScreenY(lgd.getY(j) - lgd.getErr(j));
                    yUpp = getScreenY(lgd.getY(j) + lgd.getErr(j));
                    xPosition = getScreenX(lgd.getX(j));
                    g.drawLine(xPosition, yLow, xPosition, yUpp);
                }
            }
        }

        g.setStroke(strokeInitial);

        //------------------------------------------
        //draw vertical and horizontal lines
        //------------------------------------------
        if (!hLinesV.isEmpty()) {
            double yP;
            x1 = xLOffSet;
            x2 = screenW - xROffSet;
            for (int i = 0, n = hLinesV.size(); i < n; i++) {
                yP = hLinesV.get(i);
                if (yP > yMaxIn) {
                    yP = yMaxIn;
                }
                if (yP < yMinIn) {
                    yP = yMinIn;
                }
                y1 = getScreenY(yP);
                y2 = y1;
                if (y1 < yUOffSet || y1 > (screenH - yBOffSet)) {
                    continue;
                }
                g.setColor(hLinesColorV.get(i));
                g.drawLine(x1, y1, x2, y2);
            }
        }
        if (!vLinesV.isEmpty()) {
            double xP;
            y1 = yUOffSet;
            y2 = screenH - yBOffSet;
            for (int i = 0, n = vLinesV.size(); i < n; i++) {
                xP = vLinesV.get(i);
                if (xP > xMaxIn) {
                    xP = xMaxIn;
                }
                if (xP < xMinIn) {
                    xP = xMinIn;
                }
                x1 = getScreenX(xP);
                x2 = x1;
                if (x1 < xLOffSet || x1 > (screenW - xROffSet)) {
                    continue;
                }
                g.setColor(vLinesColorV.get(i));
                g.drawLine(x1, y1, x2, y2);
            }
        }

        //------------------------------
        //draw Grid Limits lines
        //------------------------------
        if (mouseDrugged && !zoomGridLimitsV.isEmpty()) {
            GridLimits tmpGL = zoomGridLimitsV.lastElement();
            g.setColor(tmpGL.getColor());
            x1 = getScreenX(tmpGL.getMinX());
            y1 = getScreenY(tmpGL.getMaxY());
            x2 = getScreenX(tmpGL.getMaxX());
            y2 = getScreenY(tmpGL.getMinY());
            g.drawRect(x1, y1, x2 - x1, y2 - y1);
        }

        //----------------------------------------------
        //draw the clicked point cross
        //----------------------------------------------
        if (clickedPoint.isDisplayed) {
            if (colorSurfaceData == null) {
                g.setColor(clickedPoint.pointColor);
            } else {
                g.setColor(Color.white);
            }
            x1 = getScreenX(clickedPoint.getX());
            y1 = getScreenY(clickedPoint.getY());
            g.drawLine(xLOffSet, y1, screenW - xROffSet, y1);
            g.drawLine(x1, yUOffSet, x1, screenH - yBOffSet);
        }

        //---------------------------------------------------------
        //restore initial transform and stroke and others parameter
        //---------------------------------------------------------
        g.setBackground(backgroundInitial);
        g.setColor(colorInitial);
        g.setTransform(transInitial);
        g.setStroke(strokeInitial);
        g.setFont(fontInitial);

        //---------------------------------------------------------
        //draw the legend
        //---------------------------------------------------------
        legend.drawLegend(g);

        if (bkgBorderAreaColor == null) {
            bkgBorderAreaColor = getBackground();
        } else {
            g.setBackground(bkgBorderAreaColor);
        }

        //-----------------------------------------------
        //start draw grid ticks, names etc.
        //-----------------------------------------------
        g.clearRect(0, 0, scrW, yUOffSet);
        g.clearRect(0, scrH - yBOffSet, scrW, scrH);
        g.clearRect(0, 0, xLOffSet, scrH);
        g.clearRect(scrW - xROffSet, 0, scrW, scrH);

        g.setColor(numberColor);
        g.drawRect(xLOffSet, yUOffSet, scrW - xROffSet - xLOffSet, scrH - yBOffSet - yUOffSet);

        int xPos;

        int yPos;

        int wLength;

        //draw name of the graph
        if (nameOfGraph != null) {
            g.setColor(nameOfGraphColor);
            g.setFont(nameOfGraphFont);
            yPos = 2 + g.getFontMetrics().getAscent();
            wLength = g.getFontMetrics().stringWidth(nameOfGraph);
            if (wLength < (scrW - 3)) {
                xPos = (scrW - wLength) / 2;
                g.drawString(nameOfGraph, xPos, yPos);
            }
        }

        //draw name of the X axis
        if (nameX != null) {
            g.setColor(nameXColor);
            g.setFont(nameXFont);
            yPos = scrH - 2 - g.getFontMetrics().getDescent();
            wLength = g.getFontMetrics().stringWidth(nameX);
            if (wLength < (scrW - xLOffSet - xROffSet - 3)) {
                xPos = xLOffSet + (scrW - xLOffSet - xROffSet - wLength) / 2;
                g.drawString(nameX, xPos, yPos);
            }
        }

        //the x axis by itself with labels
        if (nMajorTksX > 1) {
            g.setFont(numberFont);
            g.setColor(numberColor);

            double step = (xMax - xMin) / (nMinorTksX * (nMajorTksX - 1) + nMajorTksX - 1);
            double numb;
            String numbS;
            yPos = scrH - yBOffSet;
            for (int k = 0, n = nMinorTksX * (nMajorTksX - 1) + nMajorTksX; k < n; k++) {
                numb = xMin + k * step;
                xPos = getScreenX(numb);
                if (k % (nMinorTksX + 1) == 0) {
                    g.drawLine(xPos, yPos, xPos, yPos + 5);
                    numbS = currentGridLimits.getNumberFormatX().format(numb * numbMarkScaleX);
                    wLength = g.getFontMetrics().stringWidth(numbS);
                    if (k != 0 && k != n - 1 && gridXmarkerOn) {
                        g.drawString(numbS, xPos - wLength / 2, yPos + 10 + g.getFontMetrics().getAscent());
                    }
                    if (k == 0 && gridXmarkerOn) {
                        g.drawString(numbS, xPos, yPos + 10 + g.getFontMetrics().getAscent());
                    }
                    if (k == n - 1 && gridXmarkerOn) {
                        g.drawString(numbS, xPos - wLength, yPos + 10 + g.getFontMetrics().getAscent());
                    }
                } else {
                    g.drawLine(xPos, yPos, xPos, yPos + 2);
                }
            }
        }

        //the y axis by itself with labels
        if (nMajorTksY > 1) {
            g.setFont(numberFont);
            g.setColor(numberColor);

            double step = (yMax - yMin) / (nMinorTksY * (nMajorTksY - 1) + nMajorTksY - 1);
            double numb;
            String numbS;
            xPos = xLOffSet;
            for (int k = 0, n = nMinorTksY * (nMajorTksY - 1) + nMajorTksY; k < n; k++) {
                numb = yMin + k * step;
                yPos = getScreenY(numb);
                if (k % (nMinorTksY + 1) == 0) {
                    g.drawLine(xPos, yPos, xPos - 5, yPos);
                    numbS = currentGridLimits.getNumberFormatY().format(numb * numbMarkScaleY);
                    wLength = g.getFontMetrics().stringWidth(numbS);
                    if (k != 0 && k != n - 1 && gridYmarkerOn) {
                        g.drawString(numbS, xPos - wLength - 10, yPos + g.getFontMetrics().getAscent() / 2);
                    }
                    if (k == 0 && gridYmarkerOn) {
                        g.drawString(numbS, xPos - wLength - 10, yPos);
                    }
                    if (k == n - 1 && gridYmarkerOn) {
                        g.drawString(numbS, xPos - wLength - 10, yPos + g.getFontMetrics().getAscent());
                    }
                } else {
                    g.drawLine(xPos, yPos, xPos - 2, yPos);
                }
            }
        }

        //draw name of the Y axis
        if (nameY != null) {

            AffineTransform transYaxisDraw = new AffineTransform(
                    0.0f, -1.0f, 1.0f, 0.0f, (float) 0, (float) scrH);
            g.setTransform(transYaxisDraw);

            g.setColor(nameYColor);
            g.setFont(nameYFont);
            yPos = g.getFontMetrics().getAscent() + 2;
            wLength = g.getFontMetrics().stringWidth(nameY);
            if (wLength < (scrH - yBOffSet - yUOffSet - 3)) {
                xPos = yBOffSet + (scrH - yBOffSet - yUOffSet - wLength) / 2;
                g.drawString(nameY, xPos, yPos);
            }

            g.setTransform(transInitial);
        }

        //draw the markers for vertical and horizontal lines
        if (dragHorLinesModeYes) {
            double yP;
            if (!hLinesV.isEmpty()) {
                x1 = xLOffSet - 2;
                for (int i = hLinesV.size() - 1; i >= 0; i--) {
                    yP = hLinesV.get(i);
                    if (yP > yMaxIn) {
                        yP = yMaxIn;
                    }
                    if (yP < yMinIn) {
                        yP = yMinIn;
                    }
                    y1 = getScreenY(yP);
                    if (y1 < yUOffSet) {
                        y1 = yUOffSet;
                    }
                    if (y1 > (screenH - yBOffSet)) {
                        y1 = (screenH - yBOffSet);
                    }
                    g.setColor(hLinesColorV.get(i));
                    triangleMarkerLeft.translate(x1, y1);
                    g.fill(triangleMarkerLeft);
                    triangleMarkerLeft.translate(-x1, -y1);
                }
            }
        }
        if (dragVerLinesModeYes) {
            double xP;
            if (!vLinesV.isEmpty()) {
                y1 = screenH - yBOffSet + 2;
                for (int i = vLinesV.size() - 1; i >= 0; i--) {
                    xP = vLinesV.get(i);
                    if (xP > xMaxIn) {
                        xP = xMaxIn;
                    }
                    if (xP < xMinIn) {
                        xP = xMinIn;
                    }
                    x1 = getScreenX(xP);
                    if (x1 < xLOffSet) {
                        x1 = xLOffSet;
                    }
                    if (x1 > (screenW - xROffSet)) {
                        x1 = (screenW - xROffSet);
                    }
                    g.setColor(vLinesColorV.get(i));
                    triangleMarkerRight.translate(x1, y1);
                    g.fill(triangleMarkerRight);
                    triangleMarkerRight.translate(-x1, -y1);
                }
            }
        }

        //---------------------------------------------------------
        //restore initial all parameter
        //---------------------------------------------------------
        g.setColor(colorInitial);
        g.setTransform(transInitial);
        g.setStroke(strokeInitial);
        g.setFont(fontInitial);

        //draw the buttons
        Insets insets = getInsets();
        int ixButton = insets.left;
        int iyButton = insets.top;

        if (chooseModeButtonVisible) {
            chooseModeButton.setBounds(ixButton, iyButton,
                    (int) chooseModeButton.getPreferredSize().getWidth(),
                    (int) chooseModeButton.getPreferredSize().getHeight());
            ixButton += chooseModeButton.getPreferredSize().getWidth();
        }

        if (horLinesModeButtonVisible) {
            dragHorLinesModeButton.setBounds(ixButton, iyButton,
                    (int) dragHorLinesModeButton.getPreferredSize().getWidth(),
                    (int) dragHorLinesModeButton.getPreferredSize().getHeight());
            ixButton += dragHorLinesModeButton.getPreferredSize().getWidth();
        }

        if (verLinesModeButtonVisible) {
            dragVerLinesModeButton.setBounds(ixButton, iyButton,
                    (int) dragVerLinesModeButton.getPreferredSize().getWidth(),
                    (int) dragVerLinesModeButton.getPreferredSize().getHeight());
            ixButton += dragVerLinesModeButton.getPreferredSize().getWidth();
        }
        if (legendButtonVisible) {
            legendButton.setBounds(ixButton, iyButton,
                    (int) legendButton.getPreferredSize().getWidth(),
                    (int) legendButton.getPreferredSize().getHeight());
        }

        //commit actions for x and y-limits changes
        if (xLimChanged) {
            for (int k = 0, n = horLimListenersV.size(); k < n; k++) {
                horLimListenersV.get(k).actionPerformed(horLimEvent);
            }
        }
        if (yLimChanged) {
            for (int k = 0, n = verLimListenersV.size(); k < n; k++) {
                verLimListenersV.get(k).actionPerformed(verLimEvent);
            }
        }
    }

    //---------------------------------------------------------
    //EDN of drawing
    //---------------------------------------------------------
    /**
     * Returns the screenX attribute of the FunctionGraphsJPanel object
     *
     * @param x Description of the Parameter
     * @return The screenX value
     */
    public int getScreenX(double x) {
        return ((int) ((x - xMin) * scaleX)) + xLOffSet;
    }

    /**
     * Returns the screenY attribute of the FunctionGraphsJPanel object
     *
     * @param y Description of the Parameter
     * @return The screenY value
     */
    public int getScreenY(double y) {
        return screenH + ((int) ((y - yMin) * scaleY)) - yBOffSet;
    }

    /**
     * Returns the fromScreenX attribute of the FunctionGraphsJPanel object
     *
     * @param ix Description of the Parameter
     * @return The fromScreenX value
     */
    private double getFromScreenX(int ix) {
        if (scaleX != 0.) {
            return xMin + (ix - xLOffSet) / scaleX;
        } else {
            return (xMin + xMax) / 2.0;
        }
    }

    /**
     * Returns the fromScreenY attribute of the FunctionGraphsJPanel object
     *
     * @param iy Description of the Parameter
     * @return The fromScreenY value
     */
    private double getFromScreenY(int iy) {
        if (scaleY != 0.) {
            return yMax + (iy - yUOffSet) / scaleY;
        } else {
            return (yMin + yMax) / 2.0;
        }
    }

    //------------------------------------------
    //methods realted to the mouse events
    //------------------------------------------
    //MouseListener implementation
    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (nTotalGraphPoints == 0 && colorSurfaceData == null && nTotalCurvePoints == 0) {
            return;
        }
        mouseUsedButton = e.getButton();
        if (mouseUsedButton == MouseEvent.BUTTON1) {
            if (mouseDrugged) {
                return;
            }
            if (e.getClickCount() == 2) {
                int eX = e.getX();
                int eY = e.getY();
                if (eX < xLOffSet && eX > fSizeY
                        && eY > yUOffSet && eY < (screenH - yBOffSet)) {
                    //operates with Y-axis
                    JDialog dialog = getAxisParamDialog();
                    if (dialog == null) {
                        return;
                    }
                    glPanel.setXYchooser(dialog, 1);
                    dialog.pack();
                    dialog.setVisible(true);
                    updateGraphJPanel();
                    return;
                }
                if (eX > xLOffSet && eX < (screenW - xROffSet)
                        && eY < (screenH - fSizeX) && eY > (screenH - yBOffSet)) {
                    //operates with X-axis
                    JDialog dialog = getAxisParamDialog();
                    if (dialog == null) {
                        return;
                    }
                    glPanel.setXYchooser(dialog, 0);
                    dialog.pack();
                    dialog.setVisible(true);
                    updateGraphJPanel();
                    return;
                }
                if (!zoomGridLimitsV.isEmpty()) {
                    zoomGridLimitsV.removeElementAt(zoomGridLimitsV.size() - 1);
                }
                clickedPoint.setDisplayed(false);
                if (graphChoosingYes) {
                    unChooseGraph();
                } else {
                    clickedPoint.setDisplayed(false);
                }
                updateGraphJPanel();
            }
            if (e.getClickCount() == 1) {
                int eX = e.getX();
                int eY = e.getY();
                if (eX < xLOffSet || eX > (screenW - xROffSet)
                        || eY < yUOffSet || eY > (screenH - yBOffSet)) {
                    clickedPoint.setDisplayed(false);
                    if (graphChoosingYes) {
                        unChooseGraph();
                    }
                    updateGraphJPanel();
                    return;
                }

                if (legend.isInside(eX, eY)) {
                    clickedPoint.setDisplayed(false);
                    if (!chooseGraphFromLegend(eX, eY) && graphChoosingYes) {
                        unChooseGraph();
                    }
                    updateGraphJPanel();
                    return;
                }

                double tmpX = getFromScreenX(eX);
                double tmpY = getFromScreenY(eY);
                if (graphChoosingYes) {
                    chooseGraph(tmpX, tmpY);
                } else {
                    if (colorSurfaceData == null) {
                        clickedPoint.updateValues(tmpX, tmpY);
                    } else {
                        clickedPoint.updateValues(tmpX, tmpY, colorSurfaceData.getValue(tmpX, tmpY));
                    }
                    clickedPoint.setDisplayed(true);
                }
                updateGraphJPanel();
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    @Override
    public void mousePressed(MouseEvent e) {
        mouseUsedButton = e.getButton();
        evntIniX = e.getX();
        evntIniY = e.getY();
        if (mouseUsedButton == MouseEvent.BUTTON1) {
            mouseDrugged = false;
        }
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        mouseUsedButton = e.getButton();
        if (mouseUsedButton == MouseEvent.BUTTON1) {
            if (!mouseDrugged) {
                mouseDraggedTaskType = -1;
                return;
            }
            mouseDrugged = false;
            if (mouseDraggedTaskType == 1 || mouseDraggedTaskType == 2) {
                if (mouseDraggedTaskType == 1 && draggedHorLinesListener != null && draggedLinesIndex >= 0) {
                    draggedHorLinesListener.actionPerformed(draggedHorLinesEvent);
                }
                if (mouseDraggedTaskType == 2 && draggedVerLinesListener != null && draggedLinesIndex >= 0) {
                    draggedVerLinesListener.actionPerformed(draggedVerLinesEvent);
                }
            }
            if (mouseDraggedTaskType == 0) {
                GridLimits gL = zoomGridLimitsV.lastElement();
                if (gL != null) {
                    int iX = getScreenX(gL.getMinX());
                    int eX = getScreenX(gL.getMaxX());
                    int iY = getScreenY(gL.getMinY());
                    int eY = getScreenY(gL.getMaxY());
                    if (Math.abs(iX - eX) < 5 && Math.abs(iY - eY) < 5) {
                        zoomGridLimitsV.removeElement(gL);
                    } else {
                        gL.setSmartLimits();
                    }
                }
            }
        }
        mouseDraggedTaskType = -1;
        updateGraphJPanel();
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    @Override
    public void mouseEntered(MouseEvent e) {
        // Do nothing
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    @Override
    public void mouseExited(MouseEvent e) {
        // Do nothing
    }

    //MouseMotionListener implementation
    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        if (mouseUsedButton == MouseEvent.BUTTON1) {
            if (!mouseDrugged) {
                if (evntIniX < xLOffSet || evntIniX > (screenW - xROffSet)
                        || evntIniY < yUOffSet || evntIniY > (screenH - yBOffSet)) {

                    if (!dragHorLinesModeYes && !dragVerLinesModeYes) {
                        return;
                    }

                    if (dragHorLinesModeYes && evntIniX > xLOffSet - 10 && evntIniX < (screenW - xLOffSet)
                            && evntIniY > yUOffSet - 5 && evntIniY < (screenH - yBOffSet + 5)) {
                        //horizontal lines dragging
                        mouseDraggedTaskType = 1;
                        draggedLinesIndex = getNearestHorizontalLineIndex(getFromScreenY(evntIniY));
                    }
                    if (dragVerLinesModeYes && evntIniX > xLOffSet - 5 && evntIniX < (screenW - xROffSet + 3)
                            && evntIniY < (screenH - yBOffSet) + 10 && evntIniY > (screenH - yBOffSet)) {
                        //vertical lines dragging
                        mouseDraggedTaskType = 2;
                        draggedLinesIndex = getNearestVerticalLineIndex(getFromScreenX(evntIniX));
                    }
                } else {
                    if (!legend.isInside(evntIniX, evntIniY)) {
                        if (nTotalGraphPoints == 0 && colorSurfaceData == null && nTotalCurvePoints == 0) {
                            return;
                        }
                        //zoom dragging
                        mouseDraggedTaskType = 0;
                        GridLimits gl = null;
                        if (useSmartGridLimits) {
                            gl = new SmartFormatGridLimits();
                        } else {
                            gl = new GridLimits();
                            synchronized (this) {
                                gl.setNumberFormatX(numberFormatX);
                                gl.setNumberFormatY(numberFormatY);
                            }
                        }
                        zoomGridLimitsV.add(gl);
                        zoomGridLimitsV.lastElement().setXmin(getFromScreenX(evntIniX));
                        zoomGridLimitsV.lastElement().setYmin(getFromScreenY(evntIniY));
                    } else {
                        //legend dragging
                        mouseDraggedTaskType = 3;
                        legend.memorizePosition();
                    }
                }
            }

            mouseDrugged = true;
            int eX = e.getX();
            int eY = e.getY();

            if (mouseDraggedTaskType == 0) {
                GridLimits gL = zoomGridLimitsV.lastElement();
                gL.initialize();

                if (eX > evntIniX) {
                    gL.setXmin(getFromScreenX(evntIniX));
                    gL.setXmax(getFromScreenX(eX));
                } else {
                    gL.setXmin(getFromScreenX(eX));
                    gL.setXmax(getFromScreenX(evntIniX));
                }

                if (eY < evntIniY) {
                    gL.setYmin(getFromScreenY(evntIniY));
                    gL.setYmax(getFromScreenY(eY));
                } else {
                    gL.setYmin(getFromScreenY(eY));
                    gL.setYmax(getFromScreenY(evntIniY));
                }
            }

            if (mouseDraggedTaskType == 1 && draggedLinesIndex >= 0) {
                hLinesV.remove(draggedLinesIndex);
                hLinesV.add(draggedLinesIndex, getFromScreenY(eY));
                if (draggedHorLinesListener != null && draggedHorLinesMotionListenYes) {
                    draggedHorLinesListener.actionPerformed(draggedHorLinesEvent);
                }
            }

            if (mouseDraggedTaskType == 2 && draggedLinesIndex >= 0) {
                vLinesV.remove(draggedLinesIndex);
                vLinesV.add(draggedLinesIndex, getFromScreenX(eX));
                if (draggedVerLinesListener != null && draggedVerLinesMotionListenYes) {
                    synchronized (this) {
                        draggedVerLinesListener.actionPerformed(draggedHorLinesEvent);
                    }
                }
            }

            if (mouseDraggedTaskType == 3) {
                legend.movePosition(eX - evntIniX, eY - evntIniY);
            }

            updateGraphJPanel();
        }
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        // Do nothing
    }

    //-------------------------------------------------------------
    //Inner class with current x and y values of the point
    //clicked by mouse
    //--------------------------------------------------------------
    /**
     * Description of the Class
     *
     * @author shishlo
     * @version July 22, 2004
     */
    public static class ClickedPoint {

        /**
         * Description of the Field
         */
        private JTextField xValueText = new JTextField(5);
        /**
         * Description of the Field
         */
        private JTextField yValueText = new JTextField(5);
        /**
         * Description of the Field
         */
        private JTextField zValueText = new JTextField(5);

        /**
         * Description of the Field
         */
        private NumberFormat xValueFormat = new DecimalFormat();
        /**
         * Description of the Field
         */
        private NumberFormat yValueFormat = new DecimalFormat(STRING_DEC_FORMAT);
        /**
         * Description of the Field
         */
        private NumberFormat zValueFormat = new DecimalFormat(STRING_DEC_FORMAT);

        /**
         * Description of the Field
         */
        private JLabel xValueLabel = new JLabel(" X= ", SwingConstants.CENTER);
        /**
         * Description of the Field
         */
        private JLabel yValueLabel = new JLabel(" Y= ", SwingConstants.CENTER);
        /**
         * Description of the Field
         */
        private JLabel zValueLabel = new JLabel(" Z= ", SwingConstants.CENTER);

        /**
         * Description of the Field
         */
        private Color pointColor = Color.blue;

        private boolean isDisplayed = false;

        private double x = 0.;
        private double y = 0.;

        /**
         * Constructor for the ClickedPoint object
         */
        public ClickedPoint() {
            xValueText.setBackground(Color.white);
            yValueText.setBackground(Color.white);
            zValueText.setBackground(Color.white);

            xValueText.setHorizontalAlignment(SwingConstants.CENTER);
            yValueText.setHorizontalAlignment(SwingConstants.CENTER);
            zValueText.setHorizontalAlignment(SwingConstants.CENTER);

            xValueText.setEditable(false);
            yValueText.setEditable(false);
            zValueText.setEditable(false);

            xValueText.setText(null);
            yValueText.setText(null);
            zValueText.setText(null);
        }

        public JTextField getxValueText() {
            return xValueText;
        }

        public void setxValueText(JTextField xValueText) {
            this.xValueText = xValueText;
        }

        public JTextField getyValueText() {
            return yValueText;
        }

        public void setyValueText(JTextField yValueText) {
            this.yValueText = yValueText;
        }

        public JTextField getzValueText() {
            return zValueText;
        }

        public void setzValueText(JTextField zValueText) {
            this.zValueText = zValueText;
        }

        public NumberFormat getxValueFormat() {
            return xValueFormat;
        }

        public void setxValueFormat(NumberFormat xValueFormat) {
            this.xValueFormat = xValueFormat;
        }

        public NumberFormat getyValueFormat() {
            return yValueFormat;
        }

        public void setyValueFormat(NumberFormat yValueFormat) {
            this.yValueFormat = yValueFormat;
        }

        public NumberFormat getzValueFormat() {
            return zValueFormat;
        }

        public void setzValueFormat(NumberFormat zValueFormat) {
            this.zValueFormat = zValueFormat;
        }

        public JLabel getxValueLabel() {
            return xValueLabel;
        }

        public void setxValueLabel(JLabel xValueLabel) {
            this.xValueLabel = xValueLabel;
        }

        public JLabel getyValueLabel() {
            return yValueLabel;
        }

        public void setyValueLabel(JLabel yValueLabel) {
            this.yValueLabel = yValueLabel;
        }

        public JLabel getzValueLabel() {
            return zValueLabel;
        }

        public void setzValueLabel(JLabel zValueLabel) {
            this.zValueLabel = zValueLabel;
        }

        public Color getPointColor() {
            return pointColor;
        }

        public void setPointColor(Color pointColor) {
            this.pointColor = pointColor;
        }

        public boolean isIsDisplayed() {
            return isDisplayed;
        }

        public void setIsDisplayed(boolean isDisplayed) {
            this.isDisplayed = isDisplayed;
        }

        /**
         * Set the specified decimal format for the X value label
         */
        // suppress cast warning as we check for it explitly before casting
        @SuppressWarnings("cast")
        public void setDecimalFormatX(final String pattern) {
            // if the format is already a DecimalFormat just apply the pattern
            if (xValueFormat instanceof DecimalFormat) {
                ((DecimalFormat) xValueFormat).applyPattern(pattern);
                // create a new DecimalFormat wit the specified pattern
            } else {
                xValueFormat = new DecimalFormat(pattern);
            }
        }

        /**
         * Set the specified decimal format for the Y value label
         */
        // suppress cast warning as we check for it explitly before casting
        @SuppressWarnings("cast")
        public void setDecimalFormatY(final String pattern) {
            // if the format is already a DecimalFormat just apply the pattern
            if (yValueFormat instanceof DecimalFormat) {
                ((DecimalFormat) yValueFormat).applyPattern(pattern);
                // create a new DecimalFormat wit the specified pattern
            } else {
                yValueFormat = new DecimalFormat(pattern);
            }
        }

        /**
         * Set the specified decimal format for the Z value label
         */
        // suppress cast warning as we check for it explitly before casting
        @SuppressWarnings("cast")
        public void setDecimalFormatZ(final String pattern) {
            // if the format is already a DecimalFormat just apply the pattern
            if (zValueFormat instanceof DecimalFormat) {
                ((DecimalFormat) zValueFormat).applyPattern(pattern);
                // create a new DecimalFormat wit the specified pattern
            } else {
                zValueFormat = new DecimalFormat(pattern);
            }
        }

        /**
         * Sets the font attribute of the ClickedPoint object
         *
         * @param fnt The new font value
         */
        public void setFont(Font fnt) {
            xValueText.setFont(fnt);
            yValueText.setFont(fnt);
            zValueText.setFont(fnt);
            xValueLabel.setFont(fnt);
            yValueLabel.setFont(fnt);
            zValueLabel.setFont(fnt);
        }

        /**
         * Description of the Method
         *
         * @param x Description of the Parameter
         * @param y Description of the Parameter
         */
        private void updateValues(double x, double y) {
            xValueText.setText(null);
            yValueText.setText(null);
            zValueText.setText(null);
            xValueText.setText(xValueFormat.format(x));
            yValueText.setText(yValueFormat.format(y));
            this.x = x;
            this.y = y;
        }

        /**
         * Description of the Method
         *
         * @param x Description of the Parameter
         * @param y Description of the Parameter
         * @param z Description of the Parameter
         */
        private void updateValues(double x, double y, double z) {
            xValueText.setText(null);
            yValueText.setText(null);
            zValueText.setText(null);
            xValueText.setText(xValueFormat.format(x));
            yValueText.setText(yValueFormat.format(y));
            zValueText.setText(zValueFormat.format(z));
            this.x = x;
            this.y = y;
        }

        /**
         * Sets the displayed attribute of the ClickedPoint object
         *
         * @param isDisplayedIn The new displayed value
         */
        private void setDisplayed(boolean isDisplayedIn) {
            isDisplayed = isDisplayedIn;
            if (!isDisplayedIn) {
                xValueText.setText(null);
                yValueText.setText(null);
                zValueText.setText(null);
            }
        }

        /**
         * Returns the x attribute of the ClickedPoint object
         *
         * @return The x value
         */
        private double getX() {
            return x;
        }

        /**
         * Returns the y attribute of the ClickedPoint object
         *
         * @return The y value
         */
        private double getY() {
            return y;
        }
    }

    //-------------------------------------------------------------
    //Inner class with grid limits
    //--------------------------------------------------------------
    //----------------------------------------------
    //dialog panel to chose grid limits
    //----------------------------------------------
    /**
     * Description of the Class
     *
     * @author shishlo
     * @version July 22, 2004
     */
    private static class GridLimitsPanel extends JPanel {

        /**
         * serialization ID
         */
        private static final long serialVersionUID = 1L;

        private FunctionGraphsJPanel fgp = null;

        //0 - operates with x 1 - with y
        private int xyChooser = 0;

        private NumberFormat defaultDoubleNumbFormat = new DecimalFormat("0.000E0");
        private NumberFormat defaultIntNumbFormat = new DecimalFormat("###");

        private JButton applyButton = new JButton("APPLY");

        /**
         * Description of the Field
         */
        private JRadioButton customButton = new JRadioButton("CUSTOM", false);
        /**
         * Description of the Field
         */
        private JRadioButton autoButton = new JRadioButton("AUTO", true);

        /**
         * Description of the Field
         */
        private boolean autoScaleOn = true;

        /**
         * Description of the Field
         */
        private JLabel minValLabel = new JLabel("min value=", SwingConstants.RIGHT);
        /**
         * Description of the Field
         */
        private JLabel maxValLabel = new JLabel("max value=", SwingConstants.RIGHT);
        /**
         * Description of the Field
         */
        private JLabel nStepLabel = new JLabel("N step = ", SwingConstants.RIGHT);
        /**
         * Description of the Field
         */
        private JLabel minorTicksLabel = new JLabel(" N minor ticks = ", SwingConstants.RIGHT);

        /**
         * Description of the Field
         */
        private JTextField minValText = new JTextField(8);
        /**
         * Description of the Field
         */
        private JTextField maxValText = new JTextField(8);
        /**
         * Description of the Field
         */
        private JTextField nStepText = new JTextField(8);
        /**
         * Description of the Field
         */
        private JTextField minorTicksText = new JTextField(8);

        /**
         * Constructor for the gridLimitsPanel object
         */
        private GridLimitsPanel() {
            super();

            minValText.setEditable(false);
            maxValText.setEditable(false);
            nStepText.setEditable(false);
            minorTicksText.setEditable(false);

            minValText.setHorizontalAlignment(SwingConstants.CENTER);
            maxValText.setHorizontalAlignment(SwingConstants.CENTER);
            nStepText.setHorizontalAlignment(SwingConstants.CENTER);
            minorTicksText.setHorizontalAlignment(SwingConstants.CENTER);

            customButton.setHorizontalTextPosition(SwingConstants.RIGHT);
            autoButton.setHorizontalTextPosition(SwingConstants.RIGHT);

            applyButton.setHorizontalTextPosition(SwingConstants.CENTER);

            minValText.setFont(new Font(getFont().getFamily(), Font.BOLD, 12));
            maxValText.setFont(new Font(getFont().getFamily(), Font.BOLD, 12));
            nStepText.setFont(new Font(getFont().getFamily(), Font.BOLD, 12));
            minorTicksText.setFont(new Font(getFont().getFamily(), Font.BOLD, 12));

            customButton.setFont(new Font(getFont().getFamily(), Font.BOLD, 12));
            autoButton.setFont(new Font(getFont().getFamily(), Font.BOLD, 12));

            applyButton.setFont(new Font(getFont().getFamily(), Font.BOLD, 12));

            minValLabel.setFont(new Font(getFont().getFamily(), Font.BOLD, 12));
            maxValLabel.setFont(new Font(getFont().getFamily(), Font.BOLD, 12));
            nStepLabel.setFont(new Font(getFont().getFamily(), Font.BOLD, 12));
            minorTicksLabel.setFont(new Font(getFont().getFamily(), Font.BOLD, 12));

            ButtonGroup groupB = new ButtonGroup();
            groupB.add(customButton);
            groupB.add(autoButton);

            JPanel tempPanel = new JPanel();
            tempPanel.setLayout(new GridLayout(0, 2, 1, 1));
            tempPanel.add(customButton);
            tempPanel.add(autoButton);
            tempPanel.add(minValLabel);
            tempPanel.add(minValText);
            tempPanel.add(maxValLabel);
            tempPanel.add(maxValText);
            tempPanel.add(nStepLabel);
            tempPanel.add(nStepText);
            tempPanel.add(minorTicksLabel);
            tempPanel.add(minorTicksText);

            setLayout(new BorderLayout());
            add(tempPanel, BorderLayout.CENTER);
            add(applyButton, BorderLayout.SOUTH);

            //actions
            customButton.addActionListener(e -> {
                minValText.setEditable(true);
                maxValText.setEditable(true);
                nStepText.setEditable(true);
                minorTicksText.setEditable(true);
                autoScaleOn = false;
            });

            autoButton.addActionListener(e -> {
                minValText.setEditable(false);
                maxValText.setEditable(false);
                nStepText.setEditable(false);
                minorTicksText.setEditable(false);
                autoScaleOn = true;
            });

            applyButton.addActionListener(e -> {
                if (autoScaleOn) {
                    GridLimits gl = fgp.getCurrentGL();
                    if (xyChooser == 0) {
                        gl.setXminOn(false);
                        gl.setXmaxOn(false);
                        gl.setMajorTicksOnX(false);
                    }
                    if (xyChooser == 1) {
                        gl.setYminOn(false);
                        gl.setYmaxOn(false);
                        gl.setMajorTicksOnY(false);
                    }
                } else {
                    GridLimits gl = fgp.getCurrentGL();
                    boolean successForMinValue = true;
                    boolean successForMaxValue = true;
                    boolean successForMajorTicks = true;
                    boolean successForMinorTicks = true;
                    double minVal = 0.;
                    double maxVal = 0.;
                    int nStep = 1;
                    int nMinorTicks = 4;

                    try {
                        minVal = Double.parseDouble(minValText.getText());
                    } catch (NumberFormatException exc) {
                        minValText.setText(null);
                        successForMinValue = false;
                    }

                    try {
                        maxVal = Double.parseDouble(maxValText.getText());
                    } catch (NumberFormatException exc) {
                        maxValText.setText(null);
                        successForMaxValue = false;
                    }

                    try {
                        nStep = Integer.parseInt(nStepText.getText());
                    } catch (NumberFormatException exc) {
                        nStepText.setText(null);
                        successForMajorTicks = false;
                    }

                    try {
                        nMinorTicks = Integer.parseInt(minorTicksText.getText());
                    } catch (NumberFormatException exc) {
                        minorTicksText.setText(null);
                        successForMinorTicks = false;
                    }

                    if (successForMinValue) {
                        if (xyChooser == 0) {
                            gl.setXmin(minVal);
                        }
                        if (xyChooser == 1) {
                            gl.setYmin(minVal);
                        }
                    }

                    if (successForMaxValue) {
                        if (xyChooser == 0) {
                            gl.setXmax(maxVal);
                        }
                        if (xyChooser == 1) {
                            gl.setYmax(maxVal);
                        }
                    }

                    if (successForMajorTicks) {
                        if (xyChooser == 0) {
                            gl.setNumMajorTicksX(nStep + 1);
                            gl.setMajorTicksOnX(true);
                        }
                        if (xyChooser == 1) {
                            gl.setNumMajorTicksY(nStep + 1);
                            gl.setMajorTicksOnY(true);
                        }
                    }

                    if (successForMinorTicks) {
                        if (xyChooser == 0) {
                            gl.setNumMinorTicksX(nMinorTicks);
                        }
                        if (xyChooser == 1) {
                            gl.setNumMinorTicksY(nMinorTicks);
                        }
                    }
                }
                fgp.refreshGraphJPanel();
            });

        }

        /**
         * Sets the functionGraphsJPanel attribute of the gridLimitsPanel object
         *
         * @param fgpIn The new functionGraphsJPanel value
         */
        private void setFunctionGraphsJPanel(FunctionGraphsJPanel fgpIn) {
            fgp = fgpIn;
        }

        /**
         * Sets the xYchooser attribute of the gridLimitsPanel object
         *
         * @param axisDialogIn The new xYchooser value
         * @param xyChooserIn The new xYchooser value
         */
        private void setXYchooser(JDialog axisDialogIn, int xyChooserIn) {
            xyChooser = xyChooserIn;
            initSet();
            if (xyChooserIn == 0) {
                axisDialogIn.setTitle("X - Axis Grid");
            }
            if (xyChooserIn == 1) {
                axisDialogIn.setTitle("Y - Axis Grid");
            }
            axisDialogIn.getContentPane().add(this);
        }

        /**
         * Description of the Method
         */
        private void initSet() {
            autoScaleOn = true;

            minValText.setEditable(false);
            maxValText.setEditable(false);
            nStepText.setEditable(false);
            minorTicksText.setEditable(false);

            if (fgp != null && fgp.getCurrentGL() != null) {
                GridLimits gl = fgp.getCurrentGL();
                if (xyChooser == 0) {
                    minValText.setText(defaultDoubleNumbFormat.format(gl.getMinX()));
                    maxValText.setText(defaultDoubleNumbFormat.format(gl.getMaxX()));
                    nStepText.setText(defaultIntNumbFormat.format(gl.getNumMajorTicksX() - 1));
                    minorTicksText.setText(defaultIntNumbFormat.format(gl.getNumMinorTicksX()));
                }
                if (xyChooser == 1) {
                    minValText.setText(defaultDoubleNumbFormat.format(gl.getMinY()));
                    maxValText.setText(defaultDoubleNumbFormat.format(gl.getMaxY()));
                    nStepText.setText(defaultIntNumbFormat.format(gl.getNumMajorTicksY() - 1));
                    minorTicksText.setText(defaultIntNumbFormat.format(gl.getNumMinorTicksY()));
                }
            } else {

                minValText.setText(null);
                maxValText.setText(null);
                nStepText.setText(null);
                minorTicksText.setText(null);
            }

            customButton.setSelected(false);
            autoButton.setSelected(true);
        }
    }

    //--------------------------------------------
    //graph legend class
    //--------------------------------------------
    /**
     * Description of the Class
     *
     * @author shishlo
     * @version July 22, 2004
     */
    private class GraphLegend {

        private String legendName = STRING_KEY_LEGEND;

        private FunctionGraphsJPanel fgp = null;

        private boolean showLegend = true;

        private Font font = null;

        /**
         * Description of the Field
         */
        private int positionArbitrary = 0;
        /**
         * Description of the Field
         */
        private int positionTopLeft = 1;
        /**
         * Description of the Field
         */
        private int positionTopRight = 2;
        /**
         * Description of the Field
         */
        private int positionBottomLeft = 3;
        /**
         * Description of the Field
         */
        private int positionBottomRight = 4;

        private int position;
        private int positionX = 0;
        private int positionY = 0;
        private int memPositionX = 0;
        private int memPositionY = 0;
        private int legendH = 0;
        private int legendW = 0;
        private int lineH = 0;

        //colors
        /**
         * Description of the Field
         */
        private Color backGroundColor = null;
        /**
         * Description of the Field
         */
        private Color borderColor = null;

        //array for data indexes
        /**
         * Description of the Field
         */
        private int[] dataIndA = new int[10];
        /**
         * Description of the Field
         */
        private int nDataInd = 0;

        //constructor should be called inside FunctionGraphsJPanel constructor
        /**
         * Constructor for the graphLegend object
         *
         * @param fgp Description of the Parameter
         */
        private GraphLegend(FunctionGraphsJPanel fgp) {
            this.fgp = fgp;
            font = fgp.getFont();
            position = positionArbitrary;
            positionX = 0;
            positionY = 0;
        }

        /**
         * Sets the keyString attribute of the graphLegend object
         *
         * @param legendKeyString The new keyString value
         */
        private void setKeyString(String legendKeyString) {
            legendName = legendKeyString;
        }

        /**
         * Sets the font attribute of the graphLegend object
         *
         * @param font The new font value
         */
        private void setFont(Font font) {
            this.font = font;
        }

        /**
         * Sets the color attribute of the graphLegend object
         *
         * @param cl The new color value
         */
        private void setColor(Color cl) {
            borderColor = cl;
        }

        /**
         * Sets the background attribute of the graphLegend object
         *
         * @param cl The new background value
         */
        private void setBackground(Color cl) {
            backGroundColor = cl;
        }

        /**
         * Sets the visible attribute of the graphLegend object
         *
         * @param showLegend The new visible value
         */
        private void setVisible(boolean showLegend) {
            this.showLegend = showLegend;
        }

        /**
         * Returns the visible attribute of the graphLegend object
         *
         * @return The visible value
         */
        private boolean isVisible() {
            return showLegend;
        }

        /**
         * Sets the position attribute of the graphLegend object
         *
         * @param pos The new position value
         */
        private void setPosition(int pos) {
            if (pos == positionTopLeft
                    || pos == positionTopRight
                    || pos == positionBottomLeft
                    || pos == positionBottomRight) {
                position = pos;
            } else {
                position = positionArbitrary;
                positionX = 0;
                positionY = 0;
            }
        }

        /**
         * Description of the Method
         */
        private void memorizePosition() {
            memPositionX = positionX;
            memPositionY = positionY;
        }

        /**
         * Description of the Method
         *
         * @param iX Description of the Parameter
         * @param iY Description of the Parameter
         */
        private void movePosition(int iX, int iY) {
            if (position != positionArbitrary) {
                return;
            }
            positionX = memPositionX + iX;
            positionY = memPositionY + iY;
        }

        /**
         * Returns the inside attribute of the graphLegend object
         *
         * @param iX Description of the Parameter
         * @param iY Description of the Parameter
         * @return The inside value
         */
        private boolean isInside(int iX, int iY) {
            if (!showLegend) {
                return false;
            }
            int xLOffSet = fgp.xLOffSet;
            int yUOffSet = fgp.yUOffSet;
            return iX > (xLOffSet + positionX)
                    && iX < (xLOffSet + positionX + legendW)
                    && iY > (yUOffSet + positionY)
                    && iY < (yUOffSet + positionY + legendH);
        }

        /**
         * Returns the choosenGraphIndex attribute of the graphLegend object
         *
         * @param iX Description of the Parameter
         * @param iY Description of the Parameter
         * @return The choosenGraphIndex value
         */
        private Integer getChoosenGraphIndex(int iX, int iY) {
            if (isInside(iX, iY)) {
                int yUOffSet = fgp.yUOffSet;
                int ind = iY - yUOffSet - positionY;
                if (lineH > 0) {
                    ind = (ind / lineH) - 1;
                    if (ind >= 0 && ind < nDataInd) {
                        ind = dataIndA[ind];
                        if (ind < fgp.getNumberOfInstanceOfGraphData()) {
                            return ind;
                        }
                    }
                }
            }
            return null;
        }

        /**
         * Description of the Method
         *
         * @param g Description of the Parameter
         */
        private void drawLegend(Graphics2D g) {

            legendH = 0;
            legendW = 0;

            if (!showLegend) {
                return;
            }

            int ovalX;

            int ovalY;

            int ovalW;

            int ovalH;
            int ovalWMax = 0;
            int ovalHMax = 0;

            int scrH = fgp.getHeight();
            int scrW = fgp.getWidth();
            int xLOffSet = fgp.xLOffSet;
            int xROffSet = fgp.xROffSet;
            int yUOffSet = fgp.yUOffSet;
            int yBOffSet = fgp.yBOffSet;

            //-----------------------------------------------
            //initial Font,Colors,Stroke
            //-----------------------------------------------
            Font fontInitial = g.getFont();
            Color colorInitial = g.getColor();
            Color backgroundInitial = g.getBackground();
            Stroke strokeInitial = g.getStroke();

            //calculation of the region's size
            g.setFont(font);

            int fontMaxH = g.getFontMetrics().getLeading()
                    + g.getFontMetrics().getMaxAscent()
                    + g.getFontMetrics().getMaxDescent();

            //to draw string legendName at the top
            legendH = fontMaxH;
            legendW = g.getFontMetrics().stringWidth(legendName);
            String legendStr;

            int nGraphData = fgp.getNumberOfInstanceOfGraphData();
            BasicGraphData gd;
            int nCount = 0;
            for (int i = 0; i < nGraphData; i++) {
                gd = fgp.getInstanceOfGraphData(i);
                if (gd.getNumbOfPoints() > 0) {

                    if (gd.getGraphPointShape() == null) {
                        ovalW = gd.getGraphPointSize();
                        ovalH = ovalW;
                    } else {
                        ovalW = (int) (gd.getGraphPointShape().getBounds().getWidth());
                        ovalH = (int) (gd.getGraphPointShape().getBounds().getHeight());
                    }

                    ovalWMax = Math.max(ovalWMax, ovalW);
                    ovalHMax = Math.max(ovalHMax, ovalH);

                    legendStr = "";
                    Object legendObj = gd.getGraphProperty(legendName);
                    if (legendObj != null) {
                        legendStr = legendObj.toString();
                    }
                    if (nCount > (dataIndA.length - 1)) {
                        int[] tmp = new int[dataIndA.length + 10];
                        System.arraycopy(dataIndA, 0, tmp, 0, dataIndA.length);
                        dataIndA = tmp;
                    }
                    dataIndA[nCount] = i;
                    if (legendStr != null) {
                        legendW = Math.max(legendW, g.getFontMetrics().stringWidth(legendStr));
                    }

                    nCount++;
                }
            }

            nDataInd = nCount;

            //additional width to draw lines
            int lineW = 7 * ovalWMax;
            lineH = Math.max(fontMaxH, ovalHMax);
            legendH = lineH * (nDataInd + 1);
            legendW += lineW;

            //define position
            if (position != positionArbitrary) {
                if (position == positionTopLeft) {
                    positionX = 0;
                    positionY = 0;
                } else if (position == positionTopRight) {
                    positionX = scrW - xROffSet - xLOffSet - legendW;
                    positionY = 0;
                } else if (position == positionBottomLeft) {
                    positionX = 0;
                    positionY = scrH - yUOffSet - yBOffSet - legendH;
                } else if (position == positionBottomRight) {
                    positionX = scrW - xROffSet - xLOffSet - legendW;
                    positionY = scrH - yUOffSet - yBOffSet - legendH;
                }
            } else {
                if (positionX < 0) {
                    positionX = 0;
                }
                if (positionY < 0) {
                    positionY = 0;
                }
                if (positionX + legendW > scrW - xROffSet - xLOffSet) {
                    positionX = scrW - xROffSet - xLOffSet - legendW;
                    if (positionX < 0) {
                        positionX = 0;
                    }
                }
                if (positionY + legendH > scrH - yUOffSet - yBOffSet) {
                    positionY = scrH - yUOffSet - yBOffSet - legendH;
                    if (positionY < 0) {
                        positionY = 0;
                    }
                }
            }

            //draw the border
            if (backGroundColor != null) {
                g.setBackground(backGroundColor);
            }
            g.clearRect(positionX + xLOffSet, positionY + yUOffSet, legendW, legendH);

            if (borderColor != null) {
                g.setColor(borderColor);
            }
            g.drawRect(positionX + xLOffSet, positionY + yUOffSet, legendW, legendH);

            //draw the string legendName
            int wLength = g.getFontMetrics().stringWidth(legendName);
            g.drawString(legendName, positionX + xLOffSet + legendW / 2 - wLength / 2,
                    positionY + yUOffSet + lineH - g.getFontMetrics().getMaxDescent());

            nGraphData = fgp.getNumberOfInstanceOfGraphData();

            //draw the legend
            nCount = 0;
            for (int i = 0; i < nGraphData; i++) {
                gd = fgp.getInstanceOfGraphData(i);
                if (gd.getNumbOfPoints() > 0) {

                    if (gd.getGraphPointShape() == null) {
                        ovalW = gd.getGraphPointSize();
                        ovalH = ovalW;
                    } else {
                        ovalW = (int) (gd.getGraphPointShape().getBounds().getWidth());
                        ovalH = (int) (gd.getGraphPointShape().getBounds().getHeight());
                    }

                    legendStr = "";
                    Object legendObj = gd.getGraphProperty(legendName);
                    if (legendObj != null) {
                        legendStr = legendObj.toString();
                    }
                    Color lineColor = fgp.getGraphColor(i);
                    synchronized (this) {
                        if (lineColor == null) {
                            if (gd.getGraphColor() == null) {
                                lineColor = lineDefaultColor;
                            } else {
                                lineColor = gd.getGraphColor();
                            }
                        }
                        g.setColor(lineColor);
                    }
                    if (legendStr != null) {
                        g.drawString(legendStr, positionX + xLOffSet + lineW,
                                positionY + yUOffSet
                                + (nCount + 1) * lineH + lineH / 2
                                + g.getFontMetrics().getMaxDescent());
                    }

                    if (gd.getDrawPointsOn()) {
                        if (gd.getGraphPointShape() == null) {
                            ovalX = positionX + xLOffSet + lineW / 2 - ovalW / 2;
                            ovalY = positionY + yUOffSet + (nCount + 1) * lineH + lineH / 2 - ovalH / 2;
                            g.fillOval(ovalX, ovalY, ovalW, ovalH);
                        } else {
                            ovalX = positionX + xLOffSet + lineW / 2;
                            ovalY = positionY + yUOffSet + (nCount + 1) * lineH + lineH / 2;
                            g.translate(ovalX, ovalY);
                            if (gd.isGraphPointShapeFilled()) {
                                g.fill(gd.getGraphPointShape());
                            } else {
                                g.draw(gd.getGraphPointShape());
                            }
                            g.translate(-ovalX, -ovalY);
                        }
                    }

                    if (gd.getDrawLinesOn()) {
                        g.setStroke(gd.getStroke());
                        ovalX = positionX + xLOffSet + ovalWMax;
                        ovalY = positionY + yUOffSet + (nCount + 1) * lineH + lineH / 2;
                        g.drawLine(ovalX, ovalY, ovalX + lineW - 2 * ovalWMax, ovalY);
                        g.setStroke(strokeInitial);
                    }
                    nCount++;
                }
            }
            //-----------------------------------------------
            //restore initial Font and Color
            //-----------------------------------------------
            g.setFont(fontInitial);
            g.setColor(colorInitial);
            g.setBackground(backgroundInitial);
            g.setStroke(strokeInitial);
        }
    }
}
