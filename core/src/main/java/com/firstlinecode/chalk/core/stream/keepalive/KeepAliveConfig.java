package com.firstlinecode.chalk.core.stream.keepalive;

public class KeepAliveConfig {
	private static int DEFAULT_CHECK_INTERVAL = 500;
	private static int DEFAULT_INTERVAL = 30 * 1000;
	private static int DEFAULT_TIMEOUT = 120 * 1000;
	
	private int checkingInterval;
	private int interval;
	private int timeout;
	
	public KeepAliveConfig() {
		this(DEFAULT_CHECK_INTERVAL, DEFAULT_INTERVAL, DEFAULT_TIMEOUT);
	}
	
	public KeepAliveConfig(int interval, int timeout) {
		this(DEFAULT_CHECK_INTERVAL, interval, timeout);
	}
	
	public KeepAliveConfig(int checkingInterval, int interval, int timeout) {
		if (checkingInterval > interval)
			throw new IllegalArgumentException("Interval shouldn't be less than checking interval");
		
		if (interval > timeout)
			throw new IllegalArgumentException("Timeout shouldn't be less than interval.");
		
		this.interval = interval;
		this.timeout = timeout;
	}
	
	public int getCheckingInterval() {
		return checkingInterval;
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
