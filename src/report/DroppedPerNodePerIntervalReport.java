package report;

import java.util.List;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.UpdateListener;
import core.SimClock;
import core.Settings;
import java.util.Map;
import java.util.ArrayList;
import core.Tuple;
import java.util.HashMap;


public class DroppedPerNodePerIntervalReport extends Report implements MessageListener, UpdateListener {
    
    private double lastRecord = Double.MIN_VALUE;
    private int interval;
    public static final String DROPPED_REPORT_INTERVAL = "droppedReportInterval";
    public static final int DEFAULT_DROPPED_REPORT_INTERVAL = 5;
    private Map<DTNHost, ArrayList<Tuple<Integer, Integer>>> droppedPerNodePerInterval; // <host, list of <interval, dropped count>>
    private Map<DTNHost, Integer> droppedBuffer; // <host, dropped count in current interval>
    
    /**
     * Creates a new BufferOccupancyReport instance.
     */
    public DroppedPerNodePerIntervalReport() {
        super();        
        init();
        droppedBuffer = new HashMap<DTNHost, Integer>();
        droppedPerNodePerInterval = new HashMap<DTNHost, ArrayList<Tuple<Integer, Integer>>>();
        Settings settings = getSettings();
        if (settings.contains(DROPPED_REPORT_INTERVAL)) {
            interval = settings.getInt(DROPPED_REPORT_INTERVAL);
        } else {
            interval = -1; /* not found; use default */
        }

        if (interval < 0) { /* not found or invalid value -> use default */
            interval = DEFAULT_DROPPED_REPORT_INTERVAL;
        }
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void newMessage(Message m) {
        // TODO Auto-generated method stub
    }

    @Override
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
        // TODO Auto-generated method stub
    }

    @Override
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
        if (isWarmupID(m.getId())) {
            return;
        }              
        int count = this.droppedBuffer.getOrDefault(where, 0);

        if (dropped) {
            this.droppedBuffer.put(where, count + 1);
        }
    }

    @Override
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
        // TODO Auto-generated method stub
    }

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
        // TODO Auto-generated method stub
    }

    @Override
    public void updated(List<DTNHost> hosts) {
        if (SimClock.getTime() - lastRecord >= interval) {
            lastRecord = SimClock.getTime();
            for (DTNHost host : hosts) {
                int droppedCount = this.droppedBuffer.getOrDefault(host, 0);
                ArrayList<Tuple<Integer, Integer>> intervals = this.droppedPerNodePerInterval.getOrDefault(host, new ArrayList<Tuple<Integer, Integer>>());
                intervals.add(new Tuple<Integer, Integer>((int) SimClock.getTime(), droppedCount));
                this.droppedPerNodePerInterval.put(host, intervals);
                this.droppedBuffer.put(host, 0); // reset count for next interval
            }
        }
    }

    @Override
    public void done() {
        write("NodeID\tTime\tDroppedCount");

        for (Map.Entry<DTNHost, ArrayList<Tuple<Integer, Integer>>> entry : droppedPerNodePerInterval.entrySet()) {
            DTNHost host = entry.getKey();
            int nodeId = host.getAddress(); // Mengambil ID integer dari node (misal: 0, 1, 2...)
            
            ArrayList<Tuple<Integer, Integer>> records = entry.getValue();
            
            for (Tuple<Integer, Integer> record : records) {
                int time = record.getKey();      // Nilai dari tuple pertama (waktu interval)
                int dropped = record.getValue(); // Nilai dari tuple kedua (jumlah drop)

                write(nodeId + "\t" + time + "\t" + dropped);
            }
        }

         super.done();

    }
    
}
