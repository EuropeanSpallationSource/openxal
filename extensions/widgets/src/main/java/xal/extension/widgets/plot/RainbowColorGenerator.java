package xal.extension.widgets.plot;

import java.awt.*;
import java.util.*;

/**
 * RainbowColorGenerator class keeps the map between color and range 0.0 - 1.0
 * based on the Rainbow approach. This class is based on the singleton pattern.
 *
 * @version 1.0
 * @author A. Shishlo
 */
public class RainbowColorGenerator implements ColorGenerator {

    private Vector<Color> colorV;
    private int nColorMax;
    private int index;

    private static RainbowColorGenerator cgInstance = null;

    //private constructor for ColorGenerator class
    public static RainbowColorGenerator getColorGenerator() {
        if (cgInstance == null) {
            cgInstance = new RainbowColorGenerator();
        }
        return cgInstance;
    }

    //private constructor for ColorGenerator class
    private RainbowColorGenerator() {

        Color cl;

        nColorMax = 1000;
        colorV = new Vector<>(nColorMax);

        int nColorMaxInner = 65;
        Vector<Color> colorInnerV = new Vector<>(nColorMaxInner);
        float xh;
        for (int i = 0; i < nColorMaxInner; i++) {
            xh = ((float) (nColorMaxInner - i)) / 100.0f;
            cl = Color.getHSBColor(xh, 1.0f, 1.0f);
            colorInnerV.add(cl);
        }

        float stepInner = 1.0f / (nColorMaxInner - 1);
        float step = 1.0f / (nColorMax - 1);

        float val;

        for (int i = 0; i < nColorMax; i++) {

            val = i * step;
            int i0 = (int) (val / stepInner);
            if (i0 > (nColorMaxInner - 2)) {
                i0 = nColorMaxInner - 2;
            }
            int i1 = i0 + 1;
            float cof = (val - i0 * stepInner) / stepInner;
            int rI0 = colorInnerV.get(i0).getRed();
            int gI0 = colorInnerV.get(i0).getGreen();
            int bI0 = colorInnerV.get(i0).getBlue();
            int rI1 = colorInnerV.get(i1).getRed();
            int gI1 = colorInnerV.get(i1).getGreen();
            int bI1 = colorInnerV.get(i1).getBlue();
            int rI = (int) ((1.f - cof) * rI0 + (cof) * rI1);
            int gI = (int) ((1.f - cof) * gI0 + (cof) * gI1);
            int bI = (int) ((1.f - cof) * bI0 + (cof) * bI1);
            cl = new Color(rI, gI, bI);
            colorV.add(cl);
        }
    }

    /**
     * Implementation of the ColorGenerator interface. Returns the Color
     * corresponding the input value. The value should be more than 0. and less
     * than 1.0
     */
    @Override
    public Color getColor(float value) {
        index = (int) (nColorMax * value);
        if (index < 0) {
            return colorV.get(0);
        } else {
            if (index < nColorMax) {
                return colorV.get(index);
            }
        }
        return colorV.get(nColorMax - 1);
    }

    /**
     * Implementation of the ColorGenerator interface. Returns the Color
     * corresponding the input value. The value should be more than 0. and less
     * than 1.0
     */
    @Override
    public Color getColor(double value) {
        index = (int) (nColorMax * value);
        if (index < 0) {
            return colorV.get(0);
        } else {
            if (index < nColorMax) {
                return colorV.get(index);
            }
        }
        return colorV.get(nColorMax - 1);
    }
}
