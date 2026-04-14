package routing.decisionengine;

import core.*;
import java.util.*;
import routing.*;


public class ContactTimeDecisionEngine implements RoutingDecisionEngine {
    // menyimpan panjang waktu kontak dengan host lain paling akhir
    private Map<DTNHost, Double> contactTimes;
    // menyimpan kapan kontak terakhir dengan host lain
    private Map<DTNHost, Double> startContactTimes;

    public ContactTimeDecisionEngine(Settings s) {
        super();
        this.contactTimes = new HashMap<>();
        this.startContactTimes = new HashMap<>();
    }

    public ContactTimeDecisionEngine(ContactTimeDecisionEngine other) {
        super();
        this.contactTimes = new HashMap<>(other.contactTimes);
        this.startContactTimes = new HashMap<>(other.startContactTimes);
    }

    public Map<DTNHost, Double> getContactTimes() {
        return contactTimes;
    }
    
    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        double currentTime = core.SimClock.getTime();
        startContactTimes.put(peer, currentTime);
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        double currentTime = core.SimClock.getTime();
        if (startContactTimes.containsKey(peer)) {
            double startTime = startContactTimes.get(peer);
            double currentSessionDuration = currentTime - startTime;
            double previousTotalDuration = contactTimes.getOrDefault(peer, 0.0);
            contactTimes.put(peer, previousTotalDuration + currentSessionDuration);
        }

        
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        // No specific exchange needed for this decision engine
    }

    @Override
    public boolean newMessage(Message m) {
        return true;
    }

    @Override       
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo().getAddress() == aHost.getAddress();
    }



    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        // TODO Auto-generated method stub
        ContactTimeDecisionEngine de = this.getOtherDecisionEngine(otherHost);
        return de.isFinalDest(m, otherHost) && !this.isFinalDest(m, otherHost);
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void update(DTNHost thisHost) {
    }

    @Override
    public RoutingDecisionEngine replicate() {
        // TODO Auto-generated method stub
        return new ContactTimeDecisionEngine(this);    
    }


    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return m.getTo() != thisHost;
        
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        if (m.getTo() == otherHost) {
            return true;
        }

        DTNHost dest = m.getTo();
        ContactTimeDecisionEngine de = getOtherDecisionEngine(otherHost);

        double otherContactTime = de.getContactTimes().getOrDefault(dest, 0.0);
        double thisContactTime = this.contactTimes.getOrDefault(dest, 0.0);

        return otherContactTime > thisContactTime;
    }

    private ContactTimeDecisionEngine getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (ContactTimeDecisionEngine) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

}
