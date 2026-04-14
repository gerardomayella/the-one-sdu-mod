package routing.decisionengine;

import core.*;
import java.util.*;
import routing.*;

public class IntercontactDecisionEngine implements RoutingDecisionEngine {
    
    private Map<DTNHost, Double> intercontactTimes;
    private Map<DTNHost, Double> lastContactTimes;
    
    public IntercontactDecisionEngine(Settings s) {
        super();
        this.intercontactTimes = new HashMap<>();
        this.lastContactTimes = new HashMap<>();
    }

    public IntercontactDecisionEngine(IntercontactDecisionEngine other) {
        super();
        this.intercontactTimes = other.intercontactTimes;
        this.lastContactTimes = other.lastContactTimes;
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        double currentTime = core.SimClock.getTime();
        if (lastContactTimes.containsKey(peer)) {
            double lastContactTime = lastContactTimes.get(peer);
            double currentIntercontactTime = currentTime - lastContactTime;
            intercontactTimes.put(peer, currentIntercontactTime);
        }
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        double currentTime = core.SimClock.getTime();
        lastContactTimes.put(peer, currentTime);
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
    }

    @Override
    public boolean newMessage(Message m) {
        // TODO Auto-generated method stub
        return true;    
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        // TODO Auto-generated method stub
        return m.getTo().getAddress() == aHost.getAddress();
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return m.getTo() != thisHost;        
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        // TODO Auto-generated method stub
        if (m.getTo() == otherHost) {
            return true;
        }

        DTNHost dest = m.getTo();
        IntercontactDecisionEngine de = this.getOtherDecisionEngine(otherHost);
        double otherIntercontactTime = de.intercontactTimes.getOrDefault(thisHost, Double.MAX_VALUE);
        double thisIntercontactTime = this.intercontactTimes.getOrDefault(dest, Double.MAX_VALUE);

        return otherIntercontactTime < thisIntercontactTime;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        // TODO Auto-generated method stub
        IntercontactDecisionEngine de = this.getOtherDecisionEngine(otherHost);
        return de.isFinalDest(m, otherHost) && !this.isFinalDest(m, otherHost);
    }

    private IntercontactDecisionEngine getOtherDecisionEngine(DTNHost otherHost) {
        // TODO Auto-generated method stub
        MessageRouter otherRouter = otherHost.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (IntercontactDecisionEngine) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void update(DTNHost thisHost) {}    

    @Override
    public RoutingDecisionEngine replicate() {
        // TODO Auto-generated method stub
        return new IntercontactDecisionEngine(this);
    }

}
