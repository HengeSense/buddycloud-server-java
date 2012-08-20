package org.buddycloud.channelserver.packetprocessor.iq.namespace.pubsub.set;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

import org.buddycloud.channelserver.channel.node.configuration.NodeConfigurationException;
import org.buddycloud.channelserver.db.DataStore;
import org.buddycloud.channelserver.packetprocessor.iq.namespace.pubsub.JabberPubsub;
import org.buddycloud.channelserver.packetprocessor.iq.namespace.pubsub.PubSubElementProcessorAbstract;
import org.buddycloud.channelserver.pubsub.affiliation.Affiliation;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.dom.DOMElement;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.PacketExtension;

public class NodeConfigure extends PubSubElementProcessorAbstract
{	
	private String node;
	
	public NodeConfigure(BlockingQueue<Packet> outQueue, DataStore dataStore)
    {
    	setDataStore(dataStore);
    	setOutQueue(outQueue);
    }

	public void process(Element elm, JID actorJID, IQ reqIQ, Element rsm) 
	    throws Exception
    {
    	element     = elm;
    	response    = IQ.createResultIQ(reqIQ);
    	request     = reqIQ;
    	actor       = actorJID;
        node        = element.attributeValue("node");

        if ((false == nodeProvided())
            || (false == nodeExists())
            || (false == userCanModify())
        ) {
        	outQueue.put(response);
        	return;
        }
        setNodeConfiguration();
    }

	private void setNodeConfiguration() throws Exception
	{
		try {
			getNodeConfigurationHelper().parse(request);
			if (true == getNodeConfigurationHelper().isValid()) {
				HashMap<String, String> configuration = getNodeConfigurationHelper().getValues();
				updateNodeConfiguration(configuration);
	            notifySubscribers();	
	            return;
			}
		} catch (NodeConfigurationException e) {
			setErrorCondition(PacketError.Type.modify, PacketError.Condition.bad_request);
			outQueue.put(response);
		} catch (Exception e) {
			setErrorCondition(PacketError.Type.cancel, PacketError.Condition.internal_server_error);
		}
		setErrorCondition(PacketError.Type.modify, PacketError.Condition.bad_request);
		outQueue.put(response);
	}

	private void updateNodeConfiguration(HashMap<String, String> configuration) throws InterruptedException
	{
		try {
			dataStore.addNodeConf(node, configuration);
		} catch (Exception e) {
			setErrorCondition(PacketError.Type.cancel, PacketError.Condition.internal_server_error);
			outQueue.put(response);
		}
	}

	private void notifySubscribers() {
		// TODO Auto-generated method stub
		
	}

	private boolean userCanModify()
	{
		HashMap<String, String> nodeConfiguration = dataStore.getNodeConf(node);
		String owner = nodeConfiguration.get(Affiliation.OWNER.toString());
		if (true == owner.equals(actor.toString())) {
			return true;
		}
		setErrorCondition(PacketError.Type.auth, PacketError.Condition.forbidden);
		return false;
	}

	private boolean nodeExists()
	{
		if (true == dataStore.nodeExists(node)) {
			return true;
		}
		setErrorCondition(
			PacketError.Type.cancel,
			PacketError.Condition.item_not_found
		);
		return false;
	}

	private boolean nodeProvided()
	{
		if ((null != node) && !node.equals("")) {
		    return true;	
		}
    	response.setType(IQ.Type.error);
    	Element nodeIdRequired = new DOMElement(
            "nodeid-required",
            new Namespace("", JabberPubsub.NS_PUBSUB_ERROR)
        );
    	Element badRequest = new DOMElement(
    	    PacketError.Condition.bad_request.toString(),
            new Namespace("", JabberPubsub.NS_XMPP_STANZAS)
    	);
        Element error = new DOMElement("error");
        error.addAttribute("type", "modify");
        error.add(badRequest);
        error.add(nodeIdRequired);
        response.setChildElement(error);
		return false;
	}

	public boolean accept(Element elm)
	{
		return elm.getName().equals("configure");
	}
}