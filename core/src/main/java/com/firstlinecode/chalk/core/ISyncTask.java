package com.firstlinecode.chalk.core;

import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public interface ISyncTask<K extends Stanza, V> {
	void trigger(IUnidirectionalStream<K> stream);
	V processResult(K stanza);
}
