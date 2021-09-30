package com.thefirstlineofcode.chalk.leps.im;

import com.thefirstlineofcode.basalt.leps.im.message.traceable.MessageRead;
import com.thefirstlineofcode.basalt.leps.im.message.traceable.Trace;
import com.thefirstlineofcode.chalk.im.stanza.IMessageListener;

public interface IMessageListener2 extends IMessageListener {
	void traced(Trace trace);
	void read(MessageRead read);
}
