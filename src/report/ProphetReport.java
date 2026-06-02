package report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.UpdateListener;
import core.SimClock;
import core.Settings;
import routing.ActiveRouter;

//report khusus untuk menangkap message prophet, untuk melihat bagaimana predictability berubah seiring waktu, dan bagaimana pengaruhnya terhadap pemilihan forwarder dan delivery status
public class ProphetReport extends Report implements MessageListener, UpdateListener {
    String print = "";
    int count = 0;

    public ProphetReport() {
    }

    @Override
    public void newMessage(Message m) {
        // TODO Auto-generated method stub
    }

    @Override
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
    }

    @Override
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
        // TODO Auto-generated method stub
    }

    @Override
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
        // TODO Auto-generated method stub
    }

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {

    }

    // update
    @Override
    public void init() {
        super.init();
    }

    @Override
    public void done() {
        super.done();

    }

    public void update(DTNHost thisHost) {

    }

    @Override
    public void updated(List<DTNHost> hosts) {
        if (count == 0) {
            write(
                    "Key\tCurrent_Node\tDestination_Node\tneighbour_Nodes\tMessage_ID\tSelected_Forwarder\tDelivery_Status\tPredictability_of_the_current_node\tPredictability_of_the_selected_forwarder\n");
            count++;
        }
        try {
            for (DTNHost host : hosts) {
                ActiveRouter r = (ActiveRouter) host.getRouter();

                if (r instanceof routing.ProphetRouter) {
                    routing.ProphetRouter pr = (routing.ProphetRouter) r;
                    // print: key(waktu), current_node, destination_node, neighbour_nodes,
                    // message_id, selected_forwarder, delivery_status, predictability_of
                    // _the_current_node, predictability_of _the_selected_ forwarder

                    for (Message m : r.getMessageCollection()) {
                        write(SimClock.getTime() + "\t" + host.toString());
                        // message yang mau dikirim ke neighbour, maka predictability yang dilihat
                        // adalah predictability ke neighbour tersebut, bukan ke destination message

                        write("\t" + m.getTo().toString() + "\t");
                        for (Connection neighbour : r.getConnections()) {
                            // to node nya apa
                            // mendatar
                            write(neighbour.getOtherNode(host).toString() + ",");
                        }
                        write("\t" + m.getId() + "\t" + pr.getSelectedForwarder(m).toString());
                        DTNHost selectedForwarder = pr.getSelectedForwarder(m);

                        if (selectedForwarder == null) {
                            continue;
                        } else {
                            write("Forwarded to " + selectedForwarder.toString());
                        }

                        routing.ProphetRouter selectedForwarderRouter = (routing.ProphetRouter) selectedForwarder
                                .getRouter();
                        write("\t" + pr.getDeliveryPreds().getOrDefault(m.getTo(), 0.0) + "\t"
                                + selectedForwarderRouter.getDeliveryPreds().getOrDefault(m.getTo(), 0.0));

                        write("\n");
                    }
                }
            }
        } catch (Exception e) {
        }
        write(print);

    }

}
