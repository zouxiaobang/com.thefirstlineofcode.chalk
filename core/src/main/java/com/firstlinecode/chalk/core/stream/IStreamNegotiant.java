package com.firstlinecode.chalk.core.stream;

import com.firstlinecode.chalk.network.ConnectionException;
import com.firstlinecode.chalk.network.IConnectionListener;

public interface IStreamNegotiant extends IConnectionListener {
	void negotiate(INegotiationContext context) throws ConnectionException, NegotiationException;
}
