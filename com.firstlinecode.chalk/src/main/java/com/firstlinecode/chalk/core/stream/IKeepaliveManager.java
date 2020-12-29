package com.firstlinecode.chalk.core.stream;

public interface IKeepaliveManager {
	KeepaliveConfig getConfig();
	void changeConfig(KeepaliveConfig config);
	void start(IStream stream);
	void stop();
}
