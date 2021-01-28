package com.firstlinecode.chalk.leps.im;

import com.firstlinecode.basalt.leps.im.message.traceable.MessageRead;
import com.firstlinecode.basalt.leps.im.message.traceable.Trace;
import com.firstlinecode.chalk.im.stanza.IMessageListener;

public interface IMessageListener2 extends IMessageListener {
	void traced(Trace trace);
	void read(MessageRead read);
}
