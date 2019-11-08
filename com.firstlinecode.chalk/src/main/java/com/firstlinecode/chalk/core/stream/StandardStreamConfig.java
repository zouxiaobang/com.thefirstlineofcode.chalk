package com.firstlinecode.chalk.core.stream;

public class StandardStreamConfig extends StreamConfig {
	private String resource;
	private boolean tlsPreferred;
	
	public StandardStreamConfig(String host, int port) {
		super(host, port);
		
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

}
