package com.firstlinecode.chalk.core.stream;

public class KeepaliveManager implements IKeepaliveManager {
	private KeepaliveConfig config;
	private IStream stream;
	private boolean started;
	
	public KeepaliveManager(KeepaliveConfig config) {
		this.config = config;
		
		started = false;
	}

	@Override
	public KeepaliveConfig getConfig() {
		return config;
	}

	@Override
	public void changeConfig(KeepaliveConfig config) {
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
