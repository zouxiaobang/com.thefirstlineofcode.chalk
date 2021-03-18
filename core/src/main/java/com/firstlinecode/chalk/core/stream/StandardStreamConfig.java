package com.firstlinecode.chalk.core.stream;

public class StandardStreamConfig extends StreamConfig {
	private String resource;
	private boolean tlsPreferred;
	private KeepAliveConfig keepAliveConfig;
	
	public StandardStreamConfig(String host, int port) {
		this(host, port, createDefaultKeepAliveConfig());
	}
	
	private static KeepAliveConfig createDefaultKeepAliveConfig() {
		return new KeepAliveConfig();
	}

	public StandardStreamConfig(String host, int port, KeepAliveConfig keepAliveConfig) {
		super(host, port);
		
		this.keepAliveConfig = keepAliveConfig;
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
	
	public KeepAliveConfig getKeepAliveConfig() {
		return keepAliveConfig;
	}

}
