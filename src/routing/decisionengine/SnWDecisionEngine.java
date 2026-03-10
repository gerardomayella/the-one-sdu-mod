package routing.decisionengine;


import java.util.*;

import core.*;
import routing.*;



public class SnWDecisionEngine implements RoutingDecisionEngine{
    public static final String NROF_COPIES_S = "nrofCopies";
    public static final String MSG_COUNT_PROP = "SprayAndWait.copies";
	
	protected int initialNrofCopies;
	protected double transitivityTimerThreshold;

    public SnWDecisionEngine(Settings s) {
        initialNrofCopies = s.getInt(NROF_COPIES_S);

    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer){}

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {}

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {

    }

    @Override
    public boolean newMessage(Message m) {
        m.addProperty(MSG_COUNT_PROP, initialNrofCopies);
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
		return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return m.getTo() != thisHost;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        // 1. DIRECT DELIVERY: Selalu izinkan jika otherHost adalah tujuannya
        if (m.getTo() == otherHost) return true;
        
        // Baca sisa copy
        int nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROP);
        
        // 2. FASE SPRAY: Jika copy > 1, izinkan disebar (Spray) ke node manapun
        if (nrofCopies > 1) return true;
        
        // 3. FASE WAIT: Jika copy == 1 dan bukan tujuan akhir, JANGAN dikirim!
        return false;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        int nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROP);
    
        if (nrofCopies > 1) {
            nrofCopies /= 2;
            m.updateProperty(MSG_COUNT_PROP, nrofCopies);
        
            return false; 
        } else {
            return true;
        }
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        // TODO Auto-generated method stub
		return m.getTo() == hostReportingOld;
    }

    @Override
    public void update(DTNHost thisHost) {}

    @Override
    public RoutingDecisionEngine replicate() {
        return new SnWDecisionEngine(this);
    }

    
    public SnWDecisionEngine(SnWDecisionEngine snw) {
        this.initialNrofCopies = snw.initialNrofCopies;
        this.transitivityTimerThreshold = snw.transitivityTimerThreshold;
    }
}
