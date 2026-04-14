package routing.decisionengine;

import core.*;
import java.util.*;
import routing.*;

public class FrequencyDecisionEngine implements RoutingDecisionEngine {

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'connectionUp'");
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'connectionDown'");
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doExchangeForNewConnection'");
    }

    @Override
    public boolean newMessage(Message m) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'newMessage'");
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isFinalDest'");
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'shouldSaveReceivedMessage'");
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'shouldSendMessageToHost'");
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'shouldDeleteSentMessage'");
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'shouldDeleteOldMessage'");
    }

    @Override
    public void update(DTNHost thisHost) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public RoutingDecisionEngine replicate() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'replicate'");
    }

}
