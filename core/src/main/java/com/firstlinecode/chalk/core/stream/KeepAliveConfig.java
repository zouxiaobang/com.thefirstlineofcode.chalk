package com.firstlinecode.chalk.core.stream;

public class KeepAliveConfig {
	private static int DEFAULT_INTERVAL = 30;
	private static int DEFAULT_TIMEOUT = 120;
	private int interval;
	private int timeout;
	
	public KeepAliveConfig() {
		this(DEFAULT_INTERVAL, DEFAULT_TIMEOUT);
	}
	
	public KeepAliveConfig(int interval, int timeout) {
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
