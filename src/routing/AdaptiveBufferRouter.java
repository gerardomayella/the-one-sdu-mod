package routing;

import java.util.Collection;
import core.*;

public class AdaptiveBufferRouter extends ActiveRouter {
    
    public static final String ADAPTIVE_BUFFER_NS = "AdaptiveBufferRouter";
    public static final String BUFFER_RELEASE_METHOD = "bufferReleaseMethod";

    public enum DropPolicy {
        FIFO, SHLI, MOFO
    }

    public DropPolicy dropPolicy;

    public AdaptiveBufferRouter(Settings s) {
        super(s);
        Settings nsSettings = new Settings(ADAPTIVE_BUFFER_NS);
        
        if (nsSettings.contains(BUFFER_RELEASE_METHOD)) {
            String policyStr = nsSettings.getSetting(BUFFER_RELEASE_METHOD);
            try {
                this.dropPolicy = DropPolicy.valueOf(policyStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                this.dropPolicy = DropPolicy.FIFO; // Fallback jika string tidak valid
            }
        } else {
            this.dropPolicy = DropPolicy.FIFO; // Default jika parameter tidak ditulis di .cfg
        }
    }

    protected AdaptiveBufferRouter(AdaptiveBufferRouter r) {
        super(r);
        this.dropPolicy = r.dropPolicy; // Salin enum saat di-replicate
    }

    @Override 
    public void update() {
        super.update();
        if (isTransferring() || !canStartTransfer() || exchangeDeliverableMessages() != null) {
            return;
        }
        this.tryAllMessagesToAllConnections();
    }

    @Override 
    public AdaptiveBufferRouter replicate() {
        return new AdaptiveBufferRouter(this);
    }

    protected Message getLeastTTL(boolean b) {
        Collection<Message> msgs = getMessageCollection();
        Message l = null;
        for (Message m : msgs) {
            if (b && isSending(m.getId())) continue;
            if (l == null || l.getTtl() > m.getTtl()) l = m;
        }
        return l;
    }

    protected Message getMostHop(boolean b) {
        Collection<Message> msgs = getMessageCollection();
        Message h = null;
        for (Message m : msgs) {
            if (b && isSending(m.getId())) continue;
            if (h == null || h.getHopCount() < m.getHopCount()) h = m;
        }
        return h;
    }

    @Override 
    protected boolean makeRoomForMessage(int s) {
        if (s > getBufferSize()) return false;
        int f = getFreeBufferSize();
        
        while (f < s) {
            Message m = null;
            
            // 2. Logika pemilihan dipindahkan menggunakan Switch Case pada Enum
            switch (this.dropPolicy) {
                case SHLI:
                    m = getLeastTTL(true);
                    break;
                case MOFO:
                    m = getMostHop(true);
                    break;
                case FIFO:
                default:
                    m = getOldestMessage(true);
                    break;
            }

            if (m == null) return false;
            
            deleteMessage(m.getId(), true);
            f += m.getSize();
        }
        return true;
    }
}