package com.firstlinecode.chalk.network;

public abstract class ConnectionListenerAdapter implements IConnectionListener {
	@Override
	public void heartBeatReceived(int length) {}
}
