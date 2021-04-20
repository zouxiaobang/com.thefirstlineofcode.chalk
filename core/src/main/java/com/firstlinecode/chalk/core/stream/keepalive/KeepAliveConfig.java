package com.firstlinecode.chalk.core.stream.keepalive;

public class KeepAliveConfig {
	private static int DEFAULT_INTERVAL = 30 * 1000;
	private static int DEFAULT_TIMEOUT = 120 * 1000;
	
	private int interval;
	private int timeout;
	
	public KeepAliveConfig() {
		this(DEFAULT_INTERVAL, DEFAULT_TIMEOUT);
	}
	
	public KeepAliveConfig(int interval, int timeout) {
		if (interval > timeout)
			throw new IllegalArgumentException("Timeout shouldn't be less than interval.");
		
		this.interval = interval;
		this.timeout = timeout;
	}
	
	public int getInterval() {
		return interval;
	}
	
	public int getTimeout() {
		return timeout;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof KeepAliveConfig) {
			KeepAliveConfig other = (KeepAliveConfig)obj;
			return other.interval == this.interval && other.timeout == this.timeout;
		}
		
		return false;
	}
}
