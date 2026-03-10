package report;

import java.util.HashMap;
import java.util.Map;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.Tuple;

public class DeletedPerNodeReport extends Report implements MessageListener {
    private Map<DTNHost, Tuple<Integer, Integer>> deletedPerNode; // <host, <dropped, removed>>

    public DeletedPerNodeReport() {
        init();
        this.deletedPerNode = new HashMap<DTNHost, Tuple<Integer, Integer>>();

    }

    @Override
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
        if (isWarmupID(m.getId())) {
            return;
        }             
        Tuple<Integer, Integer> counts = this.deletedPerNode.getOrDefault(where, new Tuple<Integer, Integer>(0, 0));
        if (dropped) {
            counts.setKey(counts.getKey() + 1);
            this.deletedPerNode.put(where, counts);
        } else {
            counts.setValue(counts.getValue() + 1);
            this.deletedPerNode.put(where, counts);
        }

    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void done() {
        write("Deleted messages per node for scenario " + getScenarioName() + "\nsim_time: " + format(getSimTime())+"\n");
        for (Map.Entry<DTNHost, Tuple<Integer, Integer>> entry : this.deletedPerNode.entrySet()) {
            DTNHost host = entry.getKey();
            Tuple<Integer, Integer> counts = entry.getValue();
            write(host.getAddress()+ "," + counts.getKey() + "," + counts.getValue());
        }
        super.done();
        

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
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
        // TODO Auto-generated method stub
    }

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
        // TODO Auto-generated method stub
    }
}