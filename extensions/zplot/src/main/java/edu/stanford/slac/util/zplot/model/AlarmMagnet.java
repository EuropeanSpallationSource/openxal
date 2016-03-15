/**
 * AlarmMagnet
 * 
 * @author Debbie Rogind
 * @version %I%, %G%
 * @since
 * This class extends Device in order to provide a different tooltip for the alarmed magnet devices
 */
package edu.stanford.slac.util.zplot.model;

/**
 * @author softegr
 *
 */
public class AlarmMagnet extends Device {

	protected Object bact;
	protected Object statusMessage;

	/**
	 * @param name from MAD; or alias if Non-MAD, or CA device name can be used
	 * @param z = z position from MAD 
	 * @param widget = widget from WidgetsRepository
	 * @param bact = MagnetBactRecord whose toString() method is overwritten to provide the magnet's bact value
	 * @param statusMessage = MagnetStatMsgRecord whose toString() method is overwritten to provide STATMSG string
	 */
	public AlarmMagnet(String name, double z, Widget widget, Object bact, Object statusMessage) {
		super(name, z, 0, widget);
		this.bact = bact;
		this.statusMessage = statusMessage;
	}

	public String getTooltip() {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><p>");
		sb.append(String.format("%s (%s) %s", getName(),  bact, statusMessage) );
		sb.append("</p>");
		sb.append("</html>");
		return sb.toString();
	}
	
}
