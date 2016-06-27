package xal.plugin.pvaccess;

import xal.ca.ChannelTimeRecord;

/**
 * ChannelTimeRecord implementation that also provides easy access to other data received 
 * from the Channel, like alarm and limits values, units, the type of value field and number
 * of elements in value array.
 * @author <a href="mailto:blaz.kranjc@cosylab.com">Blaz Kranjc</a>
 */
public class PvAccessChannelRecord extends ChannelTimeRecord {
    
    private final PvAccessDataAdapter adaptor;

    /**
     * Constructor.
     * @param adaptor Data adaptor received from the channel.
     */
    public PvAccessChannelRecord(PvAccessDataAdapter adaptor) {
        super(adaptor);
        this.adaptor = adaptor;
    }

    /**
     * @return Type of elements in value field
     */
    Class<?> getElementType() {
        return adaptor.getValueType();
    }

    /**
     * @return Number of elements in value field
     */
    int getElementCount() {
        return adaptor.getElementCount();
    }

    /**
     * @return Units of data in value field
     */
    String getUnits() {
        return adaptor.getUnits();
    }

    /**
     * @return Upper display limit
     */
    Number getUpperDisplayLimit() {
        return adaptor.getUpperDisplayLimit();
    }

    /**
     * @return Lower display limit
     */
    Number getLowerDisplayLimit() {
        return adaptor.getLowerDisplayLimit();
    }

    /**
     * @return Upper control limit
     */
    Number getUpperControlLimit() {
        return adaptor.getUpperControlLimit();
    }

    /**
     * @return Lower control limit
     */
    Number getLowerControlLimit() {
        return adaptor.getLowerControlLimit();
    }
    
}
