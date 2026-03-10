package report;

import java.util.List;

import core.DTNHost;
import core.Settings;
import core.SimClock;
import core.UpdateListener;

public class BufferOccupancyPerNodePerIntervalReport extends Report implements UpdateListener {
    private int interval;
    public static final String BUFFER_REPORT_INTERVAL = "occupancyInterval";
    public static final int DEFAULT_BUFFER_REPORT_INTERVAL = 5;
    private double lastRecord = Double.MIN_VALUE;
    
            private boolean isHeaderWritten = false; 

    public BufferOccupancyPerNodePerIntervalReport() {
        super();
        Settings settings = getSettings();
        if (settings.contains(BUFFER_REPORT_INTERVAL)) {
            interval = settings.getInt(BUFFER_REPORT_INTERVAL);
        } else {
            interval = -1; /* not found; use default */
        }

        if (interval < 0) { /* not found or invalid value -> use default */
            interval = DEFAULT_BUFFER_REPORT_INTERVAL;
        }
    }

    @Override
    public void updated(List<DTNHost> hosts) {
        if (SimClock.getTime() - lastRecord >= interval) {
            lastRecord = SimClock.getTime();
            printLine(hosts);
        }
    }

    private void printLine(List<DTNHost> hosts) {
        if (!isHeaderWritten) {
            StringBuilder header = new StringBuilder("Time ");
            for (DTNHost host : hosts) {
                header.append("Node").append(host.getAddress()).append("(%) ");
            }
            header.append("Average(%)");
            write(header.toString());
            isHeaderWritten = true;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(format(SimClock.getTime())).append(" ");

        double totalOccupancyPercentage = 0.0;
        int nodeCount = hosts.size();

        for (DTNHost host : hosts) {
            long maxBuffer = host.getRouter().getBufferSize();
            long freeBuffer = host.getRouter().getFreeBufferSize();
            
            long usedBuffer = maxBuffer - freeBuffer;
            
            double occupancyPercent = 0.0;
            if (maxBuffer > 0) {
                occupancyPercent = ((double) usedBuffer / maxBuffer) * 100.0;
            }
            
            totalOccupancyPercentage += occupancyPercent;
            
            sb.append(String.format(java.util.Locale.US, "%.2f ", occupancyPercent));
        }

        double averageOccupancy = (nodeCount > 0) ? (totalOccupancyPercentage / nodeCount) : 0.0;
        sb.append(String.format(java.util.Locale.US, "%.2f", averageOccupancy));

        write(sb.toString());
    }
}