package com.firstlinecode.chalk.im.stanza;

import com.firstlinecode.basalt.protocol.im.stanza.Message;

public interface IMessageListener {
	void received(Message message);
}
