/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package routing;

import core.Settings;
import core.DTNHost;
import core.Message;

/**
 * Epidemic message router with drop-oldest buffer and only single transferring
 * connections at a time.
 */
public class EpidemicRouter extends ActiveRouter {

	/**
	 * Constructor. Creates a new message router based on the settings in
	 * the given Settings object.
	 * 
	 * @param s The settings object
	 */

	public static final int VACCINE_TTL = 3600;
	public EpidemicRouter(Settings s) {
		super(s);
		// TODO: read&use epidemic router specific settings (if any)
	}

	/**
	 * Copy constructor.
	 * 
	 * @param r The router prototype where setting values are copied from
	 */
	protected EpidemicRouter(EpidemicRouter r) {
		super(r);
		// TODO: copy epidemic settings here (if any)
	}

	@Override
	public void update() {
		super.update();
		if (isTransferring() || !canStartTransfer()) {
			return; // transferring, don't try other connections yet
		}

		// Try first the messages that can be delivered to final recipient
		if (exchangeDeliverableMessages() != null) {
			return; // started a transfer, don't try others (yet)
		}

		// then try any/all message to any/all connection
		this.tryAllMessagesToAllConnections();
	}

	@Override
	public EpidemicRouter replicate() {
		return new EpidemicRouter(this);
	}

	// @Override
	// public int receiveMessage(Message m, DTNHost from) {

	// // menghapus dari
	// if (m.getProperty("Type") != null && m.getProperty("Type").equals("VACCINE"))
	// {
	// String targetId = (String) m.getProperty("IdToDelete");
	// if (this.hasMessage(targetId)) {
	// this.deleteMessage(targetId, false);
	// }
	// }

	// int recvCheck = checkReceiving(m);

	// if (m.getTo() == this.getHost()) {

	// if (m.getProperty("Type") == null ||
	// !m.getProperty("Type").equals("VACCINE")) {

	// Message ackMessage = new Message(this.getHost(), m.getFrom(), "ACK_" +
	// m.getId(), 10);

	// ackMessage.addProperty("Type", "VACCINE");
	// ackMessage.addProperty("IdToDelete", m.getId());

	// this.createNewMessage(ackMessage);
	// }
	// }

	// if (recvCheck != RCV_OK) {
	// return recvCheck;
	// }

	// // seems OK, start receiving the message
	// return super.receiveMessage(m, from);
	// }

	@Override
	public int receiveMessage(Message m, DTNHost from) {

		if (m.getProperty("Type") != null && m.getProperty("Type").equals("VACCINE")) {
			String targetId = (String) m.getProperty("IdToDelete");
			if (this.hasMessage(targetId)) {
				this.deleteMessage(targetId, false);
				System.out.println(
						"Message " + targetId + " deleted from host " + this.getHost() + " due to VACCINE received");
			}
		}
		int recvCheck = checkReceiving(m);
		if (recvCheck != RCV_OK) {
			return recvCheck;
		}

		if (m.getTo() == this.getHost()) {
			if (m.getProperty("Type") == null || !m.getProperty("Type").equals("VACCINE")) {
				Message ackMessage = new Message(this.getHost(), m.getFrom(), "ACK_" + m.getId(), 1);
				ackMessage.addProperty("Type", "VACCINE");
				ackMessage.addProperty("IdToDelete", m.getId());
				ackMessage.setTtl(m.getTtl());
				this.createNewMessage(ackMessage);
				System.out.println("ACK sent for message " + m.getId() + " from " + this.getHost());
			}
		}
		return super.receiveMessage(m, from);

	}
}