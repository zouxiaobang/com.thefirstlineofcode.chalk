package com.firstlinecode.chalk.core.stream;

public class StandardStreamConfig extends StreamConfig {
	private String resource;
	private boolean tlsPreferred;
	private KeepaliveConfig keepaliveConfig;
	
	public StandardStreamConfig(String host, int port) {
		this(host, port, createDefaultKeepaliveConfig());
	}
	
	private static KeepaliveConfig createDefaultKeepaliveConfig() {
		return new KeepaliveConfig(30, 120);
	}

	public StandardStreamConfig(String host, int port, KeepaliveConfig keepaliveConfig) {
		super(host, port);
		
		this.keepaliveConfig = keepaliveConfig;
		this.resource = null;
		this.tlsPreferred = false;
	}
	
	public boolean isTlsPreferred() {
		return tlsPreferred;
	}

	public void setTlsPreferred(boolean tlsPreferred) {
		this.tlsPreferred = tlsPreferred;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}
	
	public KeepaliveConfig getKeepaliveConfig() {
		return keepaliveConfig;
	}

}
