package routing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import core.Tuple;

public class ProphetAdaptiveRouter extends ProphetRouter {

    public static final String FORWARDING_POLICY_S = "forwardingPolicy";

    protected ProphetAdaptiveRouter(ProphetRouter r) {
        super(r);
        // TODO Auto-generated constructor stub
    }

    // enum
    private enum forwardingPolicy {
        GRTR, GRTRMax, GRTRSort, COIN;
    }

    private forwardingPolicy forwardingPolicy;

    public ProphetAdaptiveRouter(Settings s) {
        super(s);
        Settings nsSettings = new Settings(PROPHET_NS);
        if (nsSettings.contains(FORWARDING_POLICY_S)) {
            String policyStr = nsSettings.getSetting(FORWARDING_POLICY_S);
            try {
                this.forwardingPolicy = forwardingPolicy.valueOf(policyStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                this.forwardingPolicy = forwardingPolicy.GRTRMax; // Fallback jika string tidak valid
            }
        } else {
            this.forwardingPolicy = forwardingPolicy.GRTRMax; // Default jika parameter tidak ditulis di .cfg
        }
    }

    @Override
    protected Tuple<Message, Connection> tryOtherMessages() {
        List<Tuple<Message, Connection>> messages = new ArrayList<Tuple<Message, Connection>>();

        Collection<Message> msgCollection = getMessageCollection();

        /*
         * for all connected hosts collect all messages that have a higher
         * probability of delivery by the other host
         */
        for (Connection con : getConnections()) {
            DTNHost other = con.getOtherNode(getHost());
            ProphetRouter othRouter = (ProphetRouter) other.getRouter();

            if (othRouter.isTransferring()) {
                continue; // skip hosts that are transferring
            }

            for (Message m : msgCollection) {
                if (othRouter.hasMessage(m.getId())) {
                    continue; // skip messages that the other one has
                }

                // Pemisahan syarat berdasarkan Forwarding Policy
                if (this.forwardingPolicy == forwardingPolicy.COIN) {
                    // Syarat COIN: Lempar koin (50% peluang)
                    if (Math.random() > 0.5) {
                        messages.add(new Tuple<Message, Connection>(m, con));
                    }
                } else {
                    // Syarat GRTR, GRTRMax, GRTRSort: Gunakan probabilitas
                    if (othRouter.getPredFor(m.getTo()) > getPredFor(m.getTo())) {
                        messages.add(new Tuple<Message, Connection>(m, con));
                    }
                }
            }
        }

        if (messages.size() == 0) {
            return null;
        }

        switch (this.forwardingPolicy) {
            case GRTR:
                Collections.sort(messages, new TupleComparator3());
                break;
            case GRTRSort:
                Collections.sort(messages, new TupleComparator2());
                break;
            case COIN:
                Collections.sort(messages, new TupleComparator3());
                break;
            case GRTRMax:
            default:
                Collections.sort(messages, new TupleComparator());
                break;
        }
        return tryMessagesForConnected(messages);
    }
}
