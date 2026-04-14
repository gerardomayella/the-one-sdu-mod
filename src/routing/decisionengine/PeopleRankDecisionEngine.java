package routing.decisionengine;

import core.*;
import java.util.*;
import routing.*;

public class PeopleRankDecisionEngine implements RoutingDecisionEngine {

    private double dampingFactor;
    private double myPeopleRank;
    private Set<DTNHost> socialNeighbors;
    private Map<DTNHost, ContactRecord> contactInfo;    
    
    private static final double CONTACT_DURATION_THRESHOLD = 100;


    // tuple pribadi untuk menyimpan informasi kontak dengan host lain
    private class ContactRecord {
        double lastStartTime;
        double totalDuration;
        
        public ContactRecord() {
            this.lastStartTime = 0.0;
            this.totalDuration = 0.0;
        }
    }

    public PeopleRankDecisionEngine(Settings s) {
        super();
        this.dampingFactor = s.getDouble("dampingFactor", 0.85);
        this.myPeopleRank = 1.0; 
        this.socialNeighbors = new HashSet<>();
        this.contactInfo = new HashMap<>();
    }

    public PeopleRankDecisionEngine(PeopleRankDecisionEngine other) {
        super();
        this.dampingFactor = other.dampingFactor;
        this.myPeopleRank = other.myPeopleRank;
        
        this.socialNeighbors = new HashSet<>();
        this.contactInfo = new HashMap<>();
    }

    private PeopleRankDecisionEngine getOtherDecisionEngine(DTNHost otherHost) {
        MessageRouter otherRouter = otherHost.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works with other routers of same type";
        return (PeopleRankDecisionEngine) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        ContactRecord record = contactInfo.computeIfAbsent(peer, k -> new ContactRecord());
        record.lastStartTime = SimClock.getTime();
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        ContactRecord record = contactInfo.get(peer);
        if (record != null) {
            double currentContactDuration = SimClock.getTime() - record.lastStartTime;
            record.totalDuration += currentContactDuration;

            // Cek threshold pertemanan
            if (record.totalDuration >= CONTACT_DURATION_THRESHOLD) {
                socialNeighbors.add(peer);
            }
        }
    }

    public double getPeopleRank() {
        return this.myPeopleRank;
    }
    
    // Method untuk mengambil jumlah tetangga sosial
    public int getSocialNeighborCount() {
        return this.socialNeighbors.size();
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        ContactRecord record = contactInfo.get(peer);
        
        if (record != null && record.totalDuration >= CONTACT_DURATION_THRESHOLD) {
            PeopleRankDecisionEngine other = this.getOtherDecisionEngine(peer);
            
            int peerNeighborCount = other.getSocialNeighborCount();
            if (peerNeighborCount == 0) {
                peerNeighborCount = 1; // Mencegah pembagian dengan nol
            }
            // rumus update rank
            this.myPeopleRank = (1 - this.dampingFactor) + this.dampingFactor * (other.getPeopleRank() / peerNeighborCount);
        }
    }

    @Override
    public boolean newMessage(Message m) { return true; }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo().getAddress() == aHost.getAddress();
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return m.getTo() != thisHost; 
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        PeopleRankDecisionEngine other = this.getOtherDecisionEngine(otherHost);
        
        // Kirim jika dia destinasinya, ATAU jika rank dia lebih tinggi dari rank kita
        if (m.getTo() == otherHost) {
            return true;
        } else return other.myPeopleRank > this.myPeopleRank;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return this.getOtherDecisionEngine(otherHost).isFinalDest(m, otherHost) && !this.isFinalDest(m, otherHost);
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return true; 
    }

    @Override
    public void update(DTNHost thisHost) {
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new PeopleRankDecisionEngine(this);
    }
}