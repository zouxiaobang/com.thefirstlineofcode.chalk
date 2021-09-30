package com.thefirstlineofcode.chalk.im.stanza;

import com.thefirstlineofcode.basalt.protocol.im.stanza.Message;

public interface IMessageListener {
	void received(Message message);
}
