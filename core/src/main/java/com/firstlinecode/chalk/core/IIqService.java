package com.firstlinecode.chalk.core;

import java.util.List;

import com.firstlinecode.basalt.protocol.core.Protocol;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.chalk.core.stanza.IIqListener;

public interface IIqService {
	void send(Iq iq);
	void addListener(IIqListener listener);
	void removeListener(IIqListener listener);
	void addListener(Protocol protocol, IIqListener listener);
	void removeListener(Protocol protocol);
	
	IIqListener getListener(Protocol protocol);
	List<IIqListener> getListeners();
}
