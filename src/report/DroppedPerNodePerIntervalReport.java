package report;

import java.util.List;
import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.UpdateListener;
import core.SimClock;
import core.Settings;
import java.util.Map;
import java.util.HashMap;

public class DroppedPerNodePerIntervalReport extends Report implements MessageListener, UpdateListener {
    
    // Perbaikan 1: Gunakan 0.0 alih-alih Double.MIN_VALUE yang secara teknis > 0
    private double lastRecord = 0.0; 
    private int interval;
    public static final String DROPPED_REPORT_INTERVAL = "droppedReportInterval";
    public static final int DEFAULT_DROPPED_REPORT_INTERVAL = 5;
    
    private Map<DTNHost, Integer> droppedBuffer; 
    
    /**
     * Creates a new DroppedPerNodePerIntervalReport instance.
     */
    public DroppedPerNodePerIntervalReport() {
        super();        
        init();
        droppedBuffer = new HashMap<DTNHost, Integer>();
        
        // Perbaikan 2: Penghapusan HashMap/ArrayList yang tidak perlu
        
        Settings settings = getSettings();
        if (settings.contains(DROPPED_REPORT_INTERVAL)) {
            interval = settings.getInt(DROPPED_REPORT_INTERVAL);
        } else {
            interval = DEFAULT_DROPPED_REPORT_INTERVAL;
        }
    }

    @Override
    public void init() {
        super.init();
        // Perbaikan 3: Tulis header file sejak awal init, bukan di done()
        write("Time\tNodeID\tDroppedCount");
    }

    @Override
    public void newMessage(Message m) {}

    @Override
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {}

    @Override
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
        if (isWarmupID(m.getId())) {
            return;
        }              
        
        // Sedikit optimasi: Cek boolean dropped terlebih dahulu sebelum hitung count
        if (dropped) {
            int count = this.droppedBuffer.getOrDefault(where, 0);
            this.droppedBuffer.put(where, count + 1);
        }
    }

    @Override
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {}

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {}

    @Override
    public void updated(List<DTNHost> hosts) {
        // Jika selisih waktu dari rekaman terakhir sudah melewati interval
        if (SimClock.getTime() - lastRecord >= interval) {
            int currentTime = (int) SimClock.getTime();
            
            for (DTNHost host : hosts) {
                int droppedCount = this.droppedBuffer.getOrDefault(host, 0);
                
                if (droppedCount > 0) {
                    write(currentTime + "\t" + host.getAddress() + "\t" + droppedCount);

                }
                // Perbaikan 4: Langsung Write ke text file agar memori RAM lega!
                
                // Reset nilai drop ke 0 untuk interval berikutnya
                this.droppedBuffer.put(host, 0); 
            }
            
            // Catat waktu perekaman terakhir
            lastRecord = SimClock.getTime();
        }
    }

    @Override
    public void done() {
        // Karena semua sudah di-write saat update(), done() tinggal ditutup
        super.done();
    }
}