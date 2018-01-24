package xal.plugin.pvaccess;

import java.util.concurrent.CountDownLatch;

import xal.ca.Channel;
import xal.ca.ChannelTimeRecord;
import xal.ca.IEventSinkValTime;

/**
 * Simple EventSinkValTime implementation with a latch for synchronized
 * operations.
 *
 * @author <a href="mailto:blaz.kranjc@cosylab.com">Blaz Kranjc</a>
 */
class EventSinkValTimeWithLatch implements IEventSinkValTime {

    private final CountDownLatch latch;
    private PvAccessChannelRecord record;

    EventSinkValTimeWithLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void eventValue(ChannelTimeRecord record, Channel chan) {
        this.record = (PvAccessChannelRecord) record;
        latch.countDown();
    }

    PvAccessChannelRecord getRecord() {
        return record;
    }
}
