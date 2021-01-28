package com.firstlinecode.chalk.im;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.chalk.core.IChatServices;
import com.firstlinecode.chalk.im.roster.IRosterService;
import com.firstlinecode.chalk.im.roster.RosterService;
import com.firstlinecode.chalk.im.stanza.IMessageListener;
import com.firstlinecode.chalk.im.stanza.IPresenceListener;
import com.firstlinecode.chalk.im.subscription.ISubscriptionService;
import com.firstlinecode.chalk.im.subscription.SubscriptionService;

public class InstantingMessager implements IInstantingMessager,
		IPresenceListener, IMessageListener {
	private IChatServices chatServices;
	private ISubscriptionService subscriptionService;
	private IRosterService rosterService;
	private List<IPresenceListener> presenceListeners;
	private List<IMessageListener> messageListeners;
	
	public InstantingMessager(IChatServices chatServices) {
		this.chatServices = chatServices;
		rosterService = new RosterService(chatServices);
		subscriptionService = new SubscriptionService(chatServices, rosterService);
		presenceListeners = new CopyOnWriteArrayList<>();
		messageListeners = new CopyOnWriteArrayList<>();
		
		chatServices.getPresenceService().addListener(this);
		chatServices.getMessageService().addListener(this);
	}

	@Override
	public IRosterService getRosterService() {
		return rosterService;
	}

	@Override
	public ISubscriptionService getSubscriptionService() {
		return subscriptionService;
	}

	@Override
	public void send(Message message) {
		chatServices.getMessageService().send(message);
	}

	@Override
	public void send(Presence presence) {
		chatServices.getPresenceService().send(presence);
	}

	@Override
	public void addMessageListener(IMessageListener messageListener) {
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
	public void removeMessageListener(IMessageListener messageListener) {
		messageListeners.remove(messageListener);
	}

	@Override
	public void removePresenceListener(IPresenceListener presenceListener) {
		presenceListeners.remove(presenceListener);
	}

	@Override
	public void received(Message message) {
		if (message.getObject() != null)
			return;
		
		if (message.getType() == Message.Type.GROUPCHAT)
			return;
		
		if (message.getFrom() != null && !rosterService.getLocal().exists(
				message.getFrom().getBareId()))
			return;
		
		for (IMessageListener listener : messageListeners) {
			listener.received(message);
		}
	}

	@Override
	public void send(JabberId contact, Message message) {
		message.setTo(contact);
		send(message);
	}

	@Override
	public void send(JabberId contact, Presence presence) {
		presence.setTo(contact);
		send(presence);
	}

}
