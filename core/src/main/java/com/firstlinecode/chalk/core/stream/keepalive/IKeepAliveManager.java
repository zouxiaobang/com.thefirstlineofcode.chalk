package com.firstlinecode.chalk.core.stream.keepalive;

public interface IKeepAliveManager {
	KeepAliveConfig getConfig();
	void changeConfig(KeepAliveConfig config);
	boolean isStarted();
	void start();
	void stop();
	void setKeepAliveCallback(IKeepAliveCallback callback);
	IKeepAliveCallback getKeepAliveCallback();
}
