package com.firstlinecode.chalk.core.stanza;

import com.firstlinecode.basalt.protocol.core.stanza.Iq;

public interface IIqListener {
	void received(Iq iq);
}
