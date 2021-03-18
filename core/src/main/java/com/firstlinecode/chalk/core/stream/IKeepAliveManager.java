package com.firstlinecode.chalk.core.stream;

public interface IKeepAliveManager {
	KeepAliveConfig getConfig();
	void changeConfig(KeepAliveConfig config);
	void start(IStream stream);
	void stop();
}
