package com.firstlinecode.chalk.core.stanza;

import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public interface IStanzaListener {
	void received(Stanza stanza);
}
