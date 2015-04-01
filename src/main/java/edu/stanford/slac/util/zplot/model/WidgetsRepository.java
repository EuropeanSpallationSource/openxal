package edu.stanford.slac.util.zplot.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import org.jfree.chart.plot.DefaultDrawingSupplier;

import edu.stanford.slac.util.SLAColors;
import edu.stanford.slac.util.zplot.cartoon.model.widget.ProfileMonitorWidget;
import edu.stanford.slac.util.zplot.cartoon.model.widget.VacuumValveWidget;
import edu.stanford.slac.util.zplot.cartoon.model.widget.WireScannerWidget;

public final class WidgetsRepository {

	public final static Shape[] DEFAULT_SHAPES = DefaultDrawingSupplier
			.createStandardSeriesShapes();
	
	private static Shape createLargeCircle(){
		final int SIZE = 16;//48;
		//return new Ellipse2D.Double(-SIZE/2, -SIZE/2, SIZE, SIZE);
		return new Ellipse2D.Double(-SIZE, -SIZE, SIZE, SIZE);
	}
	
	public final static Shape LARGE_CIRCLE = createLargeCircle();
	
	private static Shape createLargeDispersionBpmShape() {
		final int SIZE = 8;
		final int HOLE_SIZE = SIZE - 2;

		Shape square = new Rectangle2D.Double(-SIZE / 2, -SIZE / 2, SIZE, SIZE);
		Shape hole = new Rectangle2D.Double(-HOLE_SIZE / 2, -HOLE_SIZE / 2,
				HOLE_SIZE, HOLE_SIZE);

		Area result = new Area(square);
		result.subtract(new Area(hole));
		return result;
	}

	public final static Shape LARGE_DISPERSION_BPM_SHAPE = createLargeDispersionBpmShape();

	private static Shape[] createMagnetShapes() {
		Shape[] result = new Shape[6];
		int REF_HEIGHT = 12;
		// (alarmed magnet) XCOR shape
		result[0] = new Polygon(
				new int[] { 0, REF_HEIGHT / 4, -REF_HEIGHT / 4 }, new int[] {
						-REF_HEIGHT / 2, 0, 0 }, 3);

		// (alarmed magnet) YCOR shape
		result[1] = new Polygon(
				new int[] { 0, REF_HEIGHT / 4, -REF_HEIGHT / 4 }, new int[] {
						REF_HEIGHT / 2, 0, 0 }, 3);

		// (alarmed magnet) SOLN shape
		result[2] = new Rectangle2D.Double(0, -REF_HEIGHT / 2, 1, REF_HEIGHT);

		// (alarmed magnet) Focusing Quad shape
		result[3] = new Rectangle2D.Double(0, -REF_HEIGHT * 2, 1,
				REF_HEIGHT * 2);

		// (alarmed magnet) DeFocusing Quad shape
		result[4] = new Rectangle2D.Double(0, 0, 1, REF_HEIGHT * 2);

		// (alarmed magnet) Dipole (BEND) shape
		result[5] = new Rectangle2D.Double(0, -REF_HEIGHT * 2 / 2, 1,
				REF_HEIGHT * 2);
		return result;

	}

	public final static Shape[] MAGNET_SHAPES = createMagnetShapes();

	private static Shape createStopperShape() {
		final int HEIGHT = 48;
		final int WIDTH = 20;
		final int HALF_OCTAGON_SIDE = 4;

		Shape stick = new Rectangle2D.Double(-1, -HEIGHT, 2, HEIGHT);

		Shape octagon = new Polygon(new int[] { HALF_OCTAGON_SIDE, WIDTH / 2,
				WIDTH / 2, HALF_OCTAGON_SIDE, -HALF_OCTAGON_SIDE, -WIDTH / 2,
				-WIDTH / 2, -HALF_OCTAGON_SIDE }, new int[] { -WIDTH / 2,
				-HALF_OCTAGON_SIDE, HALF_OCTAGON_SIDE, WIDTH / 2, WIDTH / 2,
				HALF_OCTAGON_SIDE, -HALF_OCTAGON_SIDE, -WIDTH / 2 }, 8);

		octagon = AffineTransform.getTranslateInstance(0, -HEIGHT + WIDTH / 2)
				.createTransformedShape(octagon);

		Graphics2D g2 = (Graphics2D) new BufferedImage(WIDTH, WIDTH,
				BufferedImage.TYPE_INT_RGB).getGraphics();

		Font f = g2.getFont();
		f = f.deriveFont(Font.BOLD).deriveFont((float) WIDTH);
		GlyphVector glyphVector = f.createGlyphVector(
				g2.getFontRenderContext(), new char[] { 's' });
		Shape s = glyphVector.getOutline();

		s = AffineTransform.getTranslateInstance(-WIDTH / 4,
				-HEIGHT + 3 * WIDTH / 4).createTransformedShape(s);

		Area result = new Area(stick);
		result.add(new Area(octagon));
		result.subtract(new Area(s));
		return result;
	}

	public final static Shape STOPPER_SHAPE = createStopperShape();
	
	public final static Stroke[] DEFAULT_STROKES = new Stroke[] {
			DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE[0],
			// dashed - - -
			new BasicStroke(1.0f, // Width
					BasicStroke.CAP_SQUARE, // End cap
					BasicStroke.JOIN_MITER, // Join style
					5, // Miter limit
					new float[] { 1, 3 }, // Dash pattern
					0.0f) };

	public final static Widget UNKNOWN_WIDGET = new Widget(Color.RED,
			DEFAULT_SHAPES[0], DEFAULT_STROKES[0]);

	private final static HashMap<Object, Widget> bpmsMap = new HashMap<Object, Widget>();

	/**
	 * @param o
	 *            an object.
	 * @return the appropriate widget for the specified parameter
	 */
	public static Widget getBPMWidget(Object o) {
		Widget w = bpmsMap.get(o);

		if (w == null) {
			Color color = null;

			if (o instanceof Color) {
				// o is Color
				color = (Color)o;
			} 

			else if (o.equals(0)) {
				// o is status
				color = SLAColors.BPM.NOT_OK;
			} 

			else {
				color = SLAColors.BPM.OK;
			}

			w = new Widget(color, DEFAULT_SHAPES[0], DEFAULT_STROKES[0]);
			bpmsMap.put(o, w);
		}
		return w;
	}

	private final static HashMap<Object, Widget> largeDispersionBpmsMap = new HashMap<Object, Widget>();

	/**
	 * @param o
	 *            an object.
	 * @return the appropriate widget for the specified parameter
	 */
	public static Widget getLargeDispersionBPMWidget(Object o) {
		Widget w = largeDispersionBpmsMap.get(o);
		if (w == null) {
			Color color = null;

			if (o instanceof Color) {
				// o is Color
				color = (Color)o;
			} 

			else if (o.equals(0)) {
				// o is status
				color = SLAColors.BPM.NOT_OK;
			} 

			else {
				color = SLAColors.BPM.OK;
			}

			w = new Widget(color, LARGE_DISPERSION_BPM_SHAPE, DEFAULT_STROKES[0]);
			largeDispersionBpmsMap.put(o, w);
		}
		return w;

	}

	private final static HashMap<Object, Widget> fittedDevicesMap = new HashMap<Object, Widget>();

	/**
	 * @param o
	 *            an object.
	 * @return the appropriate widget for the specified parameter
	 */
	public static Widget getFittedDeviceWidget(Object o) {
		Widget w = fittedDevicesMap.get(o);
		if (w == null) {
			// o is status
			Stroke stroke = null;
			if (o.equals(0)) {
				stroke = DEFAULT_STROKES[1];
			} else {
				stroke = DEFAULT_STROKES[0];
			}

			w = new Widget(Color.CYAN, DEFAULT_SHAPES[0], stroke);
			fittedDevicesMap.put(o, w);
		}
		return w;

	}

	private final static HashMap<Object, Widget> markersMap = new HashMap<Object, Widget>();

	/**
	 * Try to reuse the returned <code>Widget</code>.
	 * 
	 * @param o
	 *            an object.
	 * @return the appropriate widget for the specified parameter
	 */
	public static Widget getMarkerWidget(Object o) {
		Widget w = markersMap.get(o);
		if (w == null) {
			// o is status
			Color color = null;
			if (o.equals(0)) {
				color = SLAColors.Marker.NOT_OK;
			} else {
				color = SLAColors.Marker.OK;
			}

			w = new Widget(color, DEFAULT_SHAPES[0], DEFAULT_STROKES[0]);
			markersMap.put(o, w);
		}
		return w;
	}
	private final static HashMap<Object, Widget> valvesMap = new HashMap<Object, Widget>();

	/**
	 * Try to reuse the returned <code>Widget</code>.
	 * 
	 * @param o
	 *            an object.
	 * @return the appropriate widget for the specified parameter
	 */
	public static Widget getVacuumValveWidget(Object o) {
		Widget w = valvesMap.get(o);
		if (w == null) {
			w = new VacuumValveWidget();//new Widget(Color.ORANGE, new ProfileMonitorWidget().getShape(), DEFAULT_STROKES[0]);
			valvesMap.put(o, w);
		}
		return w;
	}

	private final static HashMap<Object, Widget> profMonScreensMap = new HashMap<Object, Widget>();

	/**
	 * @param o
	 *            an object.
	 * @return the appropriate widget for the specified parameter
	 */
	public static Widget getProfMonScreenWidget(Object o) {
		Widget w = profMonScreensMap.get(o);
		if (w == null) {
			w = new ProfileMonitorWidget();//new Widget(Color.ORANGE, new ProfileMonitorWidget().getShape(), DEFAULT_STROKES[0]);
			profMonScreensMap.put(o, w);
		}
		return w;
	}
	

	private final static HashMap<Object, Widget> stoppersMap = new HashMap<Object, Widget>();
	
	public static Widget getStopperWidget(Object o){
		Widget w = stoppersMap.get(o);
		if (w == null) {
			w = new Widget(Color.RED, STOPPER_SHAPE, DEFAULT_STROKES[0]);
			stoppersMap.put(o, w);
		}
		return w;
	}

	private final static HashMap<Object, Widget> toroidsMap = new HashMap<Object, Widget>();

	/**
	 * @param o
	 *            an object.
	 * @return the appropriate widget for the specified parameter
	 */
	public static Widget getToroidWidget(Object o) {
		Widget w = toroidsMap.get(o);
		if (w == null) {
			// o is status
			Color color = null;
			if (o.equals(0)) {
				color = SLAColors.Toroid.NOT_OK;
			} else {
				color = SLAColors.Toroid.OK;
			}

			w = new Widget(color, DEFAULT_SHAPES[0], DEFAULT_STROKES[1]);
			toroidsMap.put(o, w);
		}
		return w;
	}
	
	private final static HashMap<Object, Widget> wiresMap = new HashMap<Object, Widget>();

	/**
	 * @param o
	 *            an object.
	 * @return the appropriate widget for the specified parameter
	 */
	public static Widget getWireWidget(Object o) {
		Widget w = wiresMap.get(o);
		if (w == null) {
			w = new WireScannerWidget(); // new Widget(Color.YELLOW, LARGE_CIRCLE, DEFAULT_STROKES[0]);
			wiresMap.put(o, w);
		}
		return w;
	}

	private static Color getAlarmColor(Object o) {
		// o is status
		// store string description in an instance field
		// EPICS_ALARM_SEVR_NO_ALARM (0, "NO ALARM", Color.GREEN),
		// EPICS_ALARM_SEVR_MINOR (1, "MINOR", Color.YELLOW),
		// EPICS_ALARM_SEVR_MAJOR (2, "MAJOR", Color.RED),
		// EPICS_ALARM_SEVR_INVALID (3, "INVALID", Color.WHITE);
		Color color = null;
		if (o.equals(0)) {
			color = SLAColors.NO_ALARM;
		} else if (o.equals(1)) {
			color = SLAColors.MINOR_ALARM;
		} else if (o.equals(2)) {
			color = SLAColors.MAJOR_ALARM;
		} else if (o.equals(3)) {
			color = SLAColors.INVALID_ALARM;
		}
		return color;
	}

	private final static HashMap<Object, Widget> xcorMap = new HashMap<Object, Widget>();

	/**
	 * @param o
	 *            an object.
	 * @return the appropriate widget for the specified parameter
	 */
	public static Widget getAlarmedXCORWidget(Object o) {
		Widget w = xcorMap.get(o);
		if (w == null) {
			Color color = getAlarmColor(o);
			w = new Widget(color, MAGNET_SHAPES[0], DEFAULT_STROKES[1]);
			xcorMap.put(o, w);
		}
		return w;
	}

	private final static HashMap<Object, Widget> ycorMap = new HashMap<Object, Widget>();

	/**
	 * @param o
	 *            an object.
	 * @return the appropriate widget for the specified parameter
	 */
	public static Widget getAlarmedYCORWidget(Object o) {
		Widget w = ycorMap.get(o);
		if (w == null) {
			Color color = getAlarmColor(o);
			w = new Widget(color, MAGNET_SHAPES[1], DEFAULT_STROKES[1]);
			ycorMap.put(o, w);
		}
		return w;
	}

	private final static HashMap<Object, Widget> solnMap = new HashMap<Object, Widget>();

	/**
	 * @param o
	 *            an object.
	 * @return the appropriate widget for the specified parameter
	 */
	public static Widget getAlarmedSolnWidget(Object o) {
		Widget w = solnMap.get(o);
		if (w == null) {
			Color color = getAlarmColor(o);
			w = new Widget(color, MAGNET_SHAPES[2], DEFAULT_STROKES[1]);
			solnMap.put(o, w);
		}
		return w;
	}

	private final static HashMap<Object, Widget> fquadMap = new HashMap<Object, Widget>();

	/**
	 * @param o
	 *            an object.
	 * @return the appropriate widget for the specified parameter
	 */
	public static Widget getAlarmedFQuadWidget(Object o) {
		Widget w = fquadMap.get(o);
		if (w == null) {
			Color color = getAlarmColor(o);
			w = new Widget(color, MAGNET_SHAPES[3], DEFAULT_STROKES[1]);
			fquadMap.put(o, w);
		}
		return w;
	}

	private final static HashMap<Object, Widget> dquadMap = new HashMap<Object, Widget>();

	/**
	 * @param o
	 *            an object.
	 * @return the appropriate widget for the specified parameter
	 */
	public static Widget getAlarmedDQuadWidget(Object o) {
		Widget w = dquadMap.get(o);
		if (w == null) {
			Color color = getAlarmColor(o);
			w = new Widget(color, MAGNET_SHAPES[4], DEFAULT_STROKES[1]);
			dquadMap.put(o, w);
		}
		return w;
	}

	private final static HashMap<Object, Widget> dipoleMap = new HashMap<Object, Widget>();

	/**
	 * @param o
	 *            an object.
	 * @return the appropriate widget for the specified parameter
	 */
	public static Widget getAlarmedDipoleWidget(Object o) {
		Widget w = dquadMap.get(o);
		if (w == null) {
			Color color = getAlarmColor(o);
			w = new Widget(color, MAGNET_SHAPES[5], DEFAULT_STROKES[1]);
			dipoleMap.put(o, w);
		}
		return w;
	}

}
