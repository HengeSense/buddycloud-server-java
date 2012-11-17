package org.buddycloud.channelserver.packetprocessor.message;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.buddycloud.channelserver.channel.ChannelManager;
import org.buddycloud.channelserver.packetprocessor.PacketProcessor;
import org.buddycloud.channelserver.packetprocessor.iq.namespace.pubsub.JabberPubsub;
import org.buddycloud.channelserver.pubsub.model.NodeSubscription;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

public class MessageProcessor implements PacketProcessor<Message> {

	private static final Logger logger = Logger
			.getLogger(MessageProcessor.class);

	private final BlockingQueue<Packet> outQueue;
	private ChannelManager channelManager;

	public MessageProcessor(BlockingQueue<Packet> outQueue, Properties conf,
			ChannelManager channelManager) {
		this.outQueue = outQueue;
		this.channelManager = channelManager;
	}

	@Override
	public void process(Message packet) throws Exception {

		if (false == packet.getType().equals(Message.Type.headline)) {
			return;
		}
		Element event = (Element) packet.getElement().element("event").elements().get(0);
	}
}
