package com.firstlinecode.chalk;

import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public interface IUnidirectionalStream<T extends Stanza> {
	void send(T stanza);
	void send(T stanza, int timeout);
}
