package routing;
import java.util.Collection;

import core.*;

public class AdaptiveBufferRouter extends ActiveRouter {
    public static final String ADAPTIVE_BUFFER_NS = "AdaptiveBufferRouter";
    public static final String BUFFER_RELEASE_METHOD = "bufferReleaseMethod";
    public int bufferReleaseMethod;
	
    public AdaptiveBufferRouter(Settings s) {
        super(s);
        Settings adaptiveSettings = new Settings(ADAPTIVE_BUFFER_NS);
        bufferReleaseMethod = adaptiveSettings.getInt(BUFFER_RELEASE_METHOD, 1);        
        if (bufferReleaseMethod < 1 || bufferReleaseMethod > 3) {
            bufferReleaseMethod = 1;
        }
    }

    protected AdaptiveBufferRouter(AdaptiveBufferRouter r) {
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
    public AdaptiveBufferRouter replicate() {
        return new AdaptiveBufferRouter(this);
    }

    protected Message getLeastTTL(boolean excludeMsgBeingSent) {
	    Collection<Message> messages = this.getMessageCollection();		Message leastTTL = null;
	    for (Message m : messages) {
			
		    if (excludeMsgBeingSent && isSending(m.getId())) {
			continue;
		}
			
		if (leastTTL == null ) {
			leastTTL = m;
			}
			else if (leastTTL.getTtl() > m.getTtl()) {
				leastTTL = m;
			}
		}
		return leastTTL;
	}

	protected Message getMostHop(boolean excludeMsgBeingSent) {
		Collection<Message> messages = this.getMessageCollection();
		Message mostHop = null;
		for (Message m : messages) {
			
			if (excludeMsgBeingSent && isSending(m.getId())) {
				continue;
			}
			
			if (mostHop == null ) {
				mostHop = m;
			}
			else if (mostHop.getHopCount() < m.getHopCount()) {
				mostHop = m;
			}
		}
		return mostHop;
	}

    @Override
    protected boolean makeRoomForMessage(int size) {
        if (bufferReleaseMethod == 1) {
            if (size > this.getBufferSize()) {
			    return false; // message too big for the buffer
		    }
			
		    int freeBuffer = this.getFreeBufferSize();
		    /* delete messages from the buffer until there's enough space */
		    while (freeBuffer < size) {
			    Message m = getOldestMessage(true); // don't remove msgs being sent
			
			    if (m == null) {
				    return false; // couldn't remove any more messages
			    }			
			
			    /* delete message from the buffer as "drop" */
			    deleteMessage(m.getId(), true);
			    freeBuffer += m.getSize();
		    }
		
		    return true;
        } else if (bufferReleaseMethod == 2) {
            if (size > this.getBufferSize()) {
			    return false; // message too big for the buffer
		    }
			
		    int freeBuffer = this.getFreeBufferSize();
		    /* delete messages from the buffer until there's enough space */
		    while (freeBuffer < size) {
			    Message m = getLeastTTL(true); // don't remove msgs being sent
			
		    	if (m == null) {
			    	return false; // couldn't remove any more messages
			    }			
			
			    /* delete message from the buffer as "drop" */
			    deleteMessage(m.getId(), true);
			    freeBuffer += m.getSize();
		    }
		
		    return true;
        } else {
            if (size > this.getBufferSize()) {
			    return false; // message too big for the buffer
		    }
			
		    int freeBuffer = this.getFreeBufferSize();
		    /* delete messages from the buffer until there's enough space */
		    while (freeBuffer < size) {
			    Message m = getMostHop(true); // don't remove msgs being sent
			
	    		if (m == null) {
		    		return false; // couldn't remove any more messages
			    }			
			
			    /* delete message from the buffer as "drop" */
		    	deleteMessage(m.getId(), true);
			    freeBuffer += m.getSize();
		    }
		
		    return true;
        }
    }
}
