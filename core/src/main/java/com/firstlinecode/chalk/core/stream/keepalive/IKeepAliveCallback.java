package com.firstlinecode.chalk.core.stream.keepalive;

import java.util.Date;

public interface IKeepAliveCallback {
	void received(Date time, boolean isHeartbeats);
	void timeout();
}
