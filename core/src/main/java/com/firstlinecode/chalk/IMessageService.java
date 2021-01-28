package com.firstlinecode.chalk;

import java.util.List;

import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.chalk.im.stanza.IMessageListener;

public interface IMessageService {
	void send(Message message);
	void addListener(IMessageListener listener);
	void removeListener(IMessageListener listener);
	List<IMessageListener> getListeners();
}
