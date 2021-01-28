package com.firstlinecode.chalk.core;

import java.util.List;

import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.chalk.im.stanza.IPresenceListener;

public interface IPresenceService {
	void send(Presence presence);
	void addListener(IPresenceListener listener);
	void removeListener(IPresenceListener listener);
	List<IPresenceListener> getListeners();
	
	Presence getCurrent();
}
