package com.firstlinecode.chalk.im.stanza;

import com.firstlinecode.basalt.protocol.im.stanza.Presence;

public interface IPresenceListener {
	void received(Presence presence);
}
