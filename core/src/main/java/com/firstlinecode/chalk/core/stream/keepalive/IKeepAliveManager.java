package com.firstlinecode.chalk.core.stream.keepalive;

public interface IKeepAliveManager {
	KeepAliveConfig getConfig();
	void changeConfig(KeepAliveConfig config);
	boolean isStarted();
	void setCallback(IKeepAliveCallback callback);
	IKeepAliveCallback getCallback();
	void start();
	void stop();
}
