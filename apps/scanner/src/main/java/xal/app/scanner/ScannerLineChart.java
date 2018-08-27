/*
 * Copyright (c) 2017, Open XAL Collaboration
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package xal.app.scanner;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.NamedArg;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;

/**
 * Need auto range to ignore invisible lines
 *
 * This can probably be refactored into a common plotting class later
 *
 * @author yngvelevinsen
 */
public class ScannerLineChart<X, Y> extends LineChart<X, Y> {

  public ScannerLineChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis) {
    super(xAxis, yAxis);
  }

  @Override
  protected void updateAxisRange() {
    Logger.getLogger(ScannerLineChart.class.getName()).log(Level.FINER, "Updating Axis Range");
    final Axis<X> xa = getXAxis();
    final Axis<Y> ya = getYAxis();
    List<X> xData = null;
    List<Y> yData = null;
    if (xa.isAutoRanging()) xData = new ArrayList<X>();
    if (ya.isAutoRanging()) yData = new ArrayList<Y>();
    if (xData != null || yData != null) {
      for (Series<X, Y> series : getData()) {
        if (series.getNode().isVisible()) { // consider only visible series
          Logger.getLogger(ScannerLineChart.class.getName()).log(Level.FINEST, "Including series {}",series.getName());
          for (Data<X, Y> data : series.getData()) {
            if (xData != null) xData.add(data.getXValue());
            if (yData != null) yData.add(data.getYValue());
          }
        }
      }
      // RT-32838 No need to invalidate range if there is one data item - whose value is zero.
      if (xData != null && !(xData.size() == 1 && getXAxis().toNumericValue(xData.get(0)) == 0)) {
        xa.invalidateRange(xData);
      }
      if (yData != null && !(yData.size() == 1 && getYAxis().toNumericValue(yData.get(0)) == 0)) {
        ya.invalidateRange(yData);
      }

    }
  }
}