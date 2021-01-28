package com.firstlinecode.chalk.leps.im;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.firstlinecode.basalt.leps.im.message.traceable.MessageRead;
import com.firstlinecode.basalt.leps.im.message.traceable.MsgStatus;
import com.firstlinecode.basalt.leps.im.message.traceable.Trace;
import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.basalt.protocol.datetime.DateTime;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.chalk.core.IChatServices;
import com.firstlinecode.chalk.core.stanza.IIqListener;
import com.firstlinecode.chalk.im.roster.IRosterService;
import com.firstlinecode.chalk.im.roster.RosterService;
import com.firstlinecode.chalk.im.stanza.IMessageListener;
import com.firstlinecode.chalk.im.stanza.IPresenceListener;
import com.firstlinecode.chalk.leps.im.subscription.ISubscriptionService2;
import com.firstlinecode.chalk.leps.im.subscription.LepSubscriptionService;
import com.firstlinecode.chalk.leps.im.subscription.StandardSubscriptionService;
import com.firstlinecode.basalt.xeps.delay.Delay;

public class InstantingMessager2 implements IInstantingMessager2,
		IPresenceListener, IMessageListener, IIqListener {
	public static final String TRACE_ID_PREFIX = "trc";
	
	private IChatServices chatServices;
	private volatile LepSubscriptionService lepSubscriptionService;
	private volatile StandardSubscriptionService standardSubscriptionService;
	private IRosterService rosterService;
	private List<IPresenceListener> presenceListeners;
	private List<IMessageListener2> messageListeners;
	
	private Protocol subscriptionProtocol;
	private Protocol messageProtocol;
	
	private volatile JabberId jid;
	
	public InstantingMessager2(IChatServices chatServices) {
		this.chatServices = chatServices;
		rosterService = new RosterService(chatServices);
		presenceListeners = new CopyOnWriteArrayList<>();
		messageListeners = new CopyOnWriteArrayList<>();
		
		chatServices.getPresenceService().addListener(this);
		chatServices.getMessageService().addListener(this);
		chatServices.getIqService().addListener(this);
		
		subscriptionProtocol = Protocol.LEP;
		messageProtocol = Protocol.LEP;
	}

	@Override
	public IRosterService getRosterService() {
		return rosterService;
	}

	@Override
	public ISubscriptionService2 getSubscriptionService() {
		if (subscriptionProtocol == Protocol.LEP) {
			return getLepSubscriptionService();
		} else {
			return getStandardSubscriptionService();
		}
	}

	private StandardSubscriptionService getStandardSubscriptionService() {
		if (standardSubscriptionService != null) {
			return standardSubscriptionService;
		}
		
		synchronized(this) {
			if (standardSubscriptionService != null) {
				return standardSubscriptionService;
			}
			
			standardSubscriptionService = new StandardSubscriptionService(chatServices, rosterService);
		}
		
		return standardSubscriptionService;
	}

	private LepSubscriptionService getLepSubscriptionService() {
		if (lepSubscriptionService != null) {
			return lepSubscriptionService;
		}
		
		synchronized(this) {
			if (lepSubscriptionService != null) {
				return lepSubscriptionService;
			}
			
			lepSubscriptionService = new LepSubscriptionService(chatServices, rosterService);
		}
		
		return lepSubscriptionService;
	}

	@Override
	public String send(Message message) {
		if (messageProtocol == Protocol.LEP) {
			return sendLepMessage(message);
		} else {
			sendStandardMessage(message);
			return null;
		}
	}

	private void sendStandardMessage(Message message) {
		chatServices.getMessageService().send(message);
	}

	private String sendLepMessage(Message message) {
		String id = message.getId();
		if (id == null) {
			id = Stanza.generateId("msg");
			message.setId(id);
		}
		
		message.setObject(new Trace());
		
		chatServices.getMessageService().send(message);
		
		return id;
	}

	@Override
	public void send(Presence presence) {
		chatServices.getPresenceService().send(presence);
	}

	@Override
	public void addMessageListener(IMessageListener2 messageListener) {
		messageListeners.add(messageListener);
	}

	@Override
	public void addPresenceListener(IPresenceListener presenceListener) {
		presenceListeners.add(presenceListener);
	}

	@Override
	public void received(Presence presence) {
		if (presence.getObject() != null)
			return;
		
		if (presence.getType() == null ||
			presence.getType() == Presence.Type.UNAVAILABLE ||
				presence.getType() == Presence.Type.PROBE) {
			for (IPresenceListener listener : presenceListeners) {
				listener.received(presence);
			}
		}
	}

	@Override
	public void removeMessageListener(IMessageListener2 messageListener) {
		messageListeners.remove(messageListener);
	}

	@Override
	public void removePresenceListener(IPresenceListener presenceListener) {
		presenceListeners.remove(presenceListener);
	}

	@Override
	public void received(Message message) {
		if (message.getObject(Trace.class) != null) {
			sendPeerRearchedAck(message);
		}
		
		for (Object object : message.getObjects()) {
			if (object instanceof Trace || object instanceof Delay) {
				continue;
			}
			
			return;
		}
		
		if (message.getType() == Message.Type.GROUPCHAT)
			return;
		
		/*if (message.getFrom() != null && !rosterService.getLocal().exists(
				message.getFrom().getBareId()))
			return;*/
		
		for (IMessageListener listener : messageListeners) {
			listener.received(message);
		}
	}

	private void sendPeerRearchedAck(Message message) {
		Iq peerRearched = new Iq(Iq.Type.SET, Stanza.generateId(TRACE_ID_PREFIX));
		peerRearched.setTo(message.getFrom());
		
		JabberId to = message.getTo() == null ? getJid() : message.getTo();
		peerRearched.setObject(new Trace(new MsgStatus(message.getId(), MsgStatus.Status.PEER_REACHED,
				to, new DateTime())));
		
		chatServices.getIqService().send(peerRearched);
	}

	@Override
	public void received(Iq iq) {
		Trace trace = iq.getObject(Trace.class);
		if (trace != null) {
			processTrace(iq, trace);
			return;
		}
		
		MessageRead read = iq.getObject(MessageRead.class);
		if (read != null) {
			processMessageRead(iq, read);
		}
	}

	private void processTrace(Iq iq, Trace trace) {
		for (IMessageListener2 listener : messageListeners) {
			listener.traced(trace);
		}
		
		// send trace acknowledgement
		Iq ack = new Iq(Iq.Type.RESULT, iq.getId());
		Trace traceResponse = new Trace();
		for (MsgStatus msgStatus : trace.getMsgStatuses()) {
			traceResponse.getMsgStatuses().add(new MsgStatus(msgStatus.getId()));
		}
		ack.setObject(traceResponse);
		
		chatServices.getIqService().send(ack);
	}
	
	private void processMessageRead(Iq iq, MessageRead read) {
		for (IMessageListener2 listener : messageListeners) {
			listener.read(read);
		}
		
		// send message read acknowledgement
		Iq ack = new Iq(Iq.Type.RESULT, iq.getId());
		MessageRead messageReadResponse = new MessageRead(read.getFrom(), read.getStamp());
		ack.setObject(messageReadResponse);
		
		chatServices.getIqService().send(ack);
	}

	@Override
	public String send(JabberId contact, Message message) {
		message.setTo(contact);
		return send(message);
	}

	@Override
	public void send(JabberId contact, Presence presence) {
		presence.setTo(contact);
		send(presence);
	}
	
	@Override
	public void setSubscriptionProtocol(Protocol subscriptionProtocol) {
		this.subscriptionProtocol = subscriptionProtocol;
	}
	
	@Override
	public Protocol getSubscriptionProtocol() {
		return subscriptionProtocol;
	}
	
	@Override
	public void setMessageProtocol(Protocol messageProtocol) {
		this.messageProtocol = messageProtocol;
	}
	
	@Override
	public Protocol getMessageProtocol() {
		return messageProtocol;
	}

	@Override
	public List<IMessageListener2> getMessageListeners() {
		return messageListeners;
	}

	@Override
	public List<IPresenceListener> getPresenceListeners() {
		return presenceListeners;
	}

	@Override
	public void sendMessageReadAck(JabberId contact) {
		Iq ack = new Iq(Iq.Type.SET, Stanza.generateId(TRACE_ID_PREFIX));
		ack.setTo(contact);
		
		ack.setObject(new MessageRead(getJid(), new DateTime()));
		
		chatServices.getIqService().send(ack);
	}

	private JabberId getJid() {
		if (jid != null)
			return jid;
		
		synchronized (this) {
			if (jid != null)
				return jid;
			
			jid = chatServices.getStream().getJid();
			
			return jid;
		}
	}

}
