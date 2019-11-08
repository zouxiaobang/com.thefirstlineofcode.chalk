package com.firstlinecode.chalk.im;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.chalk.im.roster.IRosterService;
import com.firstlinecode.chalk.im.stanza.IMessageListener;
import com.firstlinecode.chalk.im.stanza.IPresenceListener;
import com.firstlinecode.chalk.im.subscription.ISubscriptionService;

public interface IInstantingMessager {
	IRosterService getRosterService();
	ISubscriptionService getSubscriptionService();
	
	void send(Message message);
	void send(JabberId contact, Message message);
	void send(Presence presence);
	void send(JabberId contact, Presence presence);
	
	void addMessageListener(IMessageListener messageListener);
	void removeMessageListener(IMessageListener messageListener);
	void addPresenceListener(IPresenceListener presenceListener);
	void removePresenceListener(IPresenceListener presenceListener);
}
