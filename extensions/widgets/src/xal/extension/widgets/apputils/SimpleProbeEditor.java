/*
 * SimpleProbeEditor.java
 *
 * Created on June 17, 2013, 8:51 AM
 *
 * @author Tom Pelaia
 * @author Patrick Scruggs
 */

package xal.extension.widgets.apputils;

import java.awt.Frame;

import xal.extension.widgets.beaneditor.SimpleBeanEditor;
import xal.model.probe.Probe;



/** SimpleProbeEditor */
public class SimpleProbeEditor extends SimpleBeanEditor<Probe<?>> {
	private static final long serialVersionUID = 1L;

	public SimpleProbeEditor(Frame owner, Probe<?> bean) {
		super(owner, "Probe Editor", "Probe", bean);
	}

	public SimpleProbeEditor(Frame owner, Probe<?> bean, boolean visible) {
		super(owner, "Probe Editor", "Probe", bean, true, visible);
	}

	/** 
	 * Get the probe to edit
     * @return probe associated with this editor
     */
    public Probe<?> getProbe() {
        return getBean();
    }
}


