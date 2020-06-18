package com.firstlinecode.chalk;

import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.chalk.core.ErrorException;

public interface ITaskService {
	void execute(ITask<?> task);
	void setDefaultTimeout(int timeout);
	int getDefaultTimeout();
	void setDefaultTimeoutHandler(ITimeoutHandler timeoutHandler);
	<K extends Stanza, V> V execute(ISyncTask<K, V> task) throws ErrorException;
}
