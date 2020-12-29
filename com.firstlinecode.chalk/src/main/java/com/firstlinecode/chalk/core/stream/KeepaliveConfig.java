package com.firstlinecode.chalk.core.stream;

public class KeepaliveConfig {
	private static int DEFAULT_INTERVAL = 30;
	private static int DEFAULT_TIMEOUT = 120;
	private int interval;
	private int timeout;
	
	public KeepaliveConfig() {
		this(DEFAULT_INTERVAL, DEFAULT_TIMEOUT);
	}
	
	public KeepaliveConfig(int interval, int timeout) {
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
		// TODO Auto-generated method stub
		return super.equals(obj);
	}
}
