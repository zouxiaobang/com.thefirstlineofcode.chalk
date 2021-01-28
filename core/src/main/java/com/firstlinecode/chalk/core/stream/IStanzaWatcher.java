package com.firstlinecode.chalk.core.stream;

import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public interface IStanzaWatcher {
	void sent(Stanza stanza, String message);
	void received(Stanza stanza, String message);
}
