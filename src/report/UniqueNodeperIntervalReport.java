package report;

import java.util.*;

import core.*;
import routing.*;
import routing.community.CentralityDetectionEngine;

public class UniqueNodeperIntervalReport extends Report {

    public UniqueNodeperIntervalReport() {
        init();
    }

    @Override
    public void done() {
        // mengimplementasikan centrality per interval waktu 24 jam
        List<DTNHost> nodes = SimScenario.getInstance().getHosts();

        for (DTNHost h : nodes) {
            MessageRouter r = h.getRouter();
            if (!(r instanceof DecisionEngineRouter)) {
                continue;
            }
            RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
            if (!(de instanceof CentralityDetectionEngine)) {
                continue;
            }
            CentralityDetectionEngine cd = (CentralityDetectionEngine) de;
            int[] nodeCentrality = cd.getArrayCentrality();

            // mengambil tiap tiap getArrayCentrality dari tiap node
            String printText = h.toString() + ":";
            for (int i = 0; i < nodeCentrality.length; i++) {
                printText = printText + "\t" + nodeCentrality[i];

            }
            write(printText);
        }

        super.done();

    }
}
