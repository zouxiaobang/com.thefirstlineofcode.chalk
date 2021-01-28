package com.firstlinecode.chalk;

import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public interface ITimeoutHandler {
	void process(Stanza stanza);
}
