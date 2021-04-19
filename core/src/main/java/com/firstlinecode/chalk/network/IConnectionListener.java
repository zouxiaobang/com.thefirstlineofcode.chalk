package com.firstlinecode.chalk.network;

public interface IConnectionListener {
	void exceptionOccurred(ConnectionException exception);
	void messageReceived(String message);
	void heartBeatReceived(int length);
	void messageSent(String message);
}
