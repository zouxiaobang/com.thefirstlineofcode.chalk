package com.firstlinecode.chalk.network;

public interface IConnectionListener {
	void occurred(ConnectionException exception);
	void received(String message);
	void sent(String message);
}
