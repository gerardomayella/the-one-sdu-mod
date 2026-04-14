package routing;

import java.util.ArrayList;
import java.util.List;

import core.*;

public class MIMORouter extends ActiveRouter {

    public MIMORouter(Settings s) {
        super(s);
    }

    protected MIMORouter(MIMORouter r) {
        super(r);
    }

    @Override
    public void update() {
        super.update();
        if (isTransferring() || !canStartTransfer()) {
            return;
        }

        if (exchangeDeliverableMessages() != null) {
            return;
        }

        this.tryAllMessagesToAllConnections();
    }

    @Override
    public MIMORouter replicate() {
        return new MIMORouter(this);
    }

    @Override
	protected boolean makeRoomForMessage(int size){ 
        
        return true;
    }
}

    

