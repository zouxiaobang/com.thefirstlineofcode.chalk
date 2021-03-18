package com.firstlinecode.chalk.core.stream;

public class KeepAliveManager implements IKeepAliveManager {
	private KeepAliveConfig config;
	private IStream stream;
	private boolean started;
	
	public KeepAliveManager(KeepAliveConfig config) {
		this.config = config;
		
		started = false;
	}

	@Override
	public KeepAliveConfig getConfig() {
		return config;
	}

	@Override
	public void changeConfig(KeepAliveConfig config) {
		if (started)
			stop();
		
		start(stream);
	}

	@Override
	public void start(IStream stream) {
		if (started)
			stop();
		
		this.stream = stream;
		start();
	}
	
	private void start() {
		// TODO
		
		started = true;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
		started = false;
		
	}

}
