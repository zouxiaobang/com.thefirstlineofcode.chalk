package com.firstlinecode.chalk.core.stream.keepalive;

import java.util.Date;

import com.firstlinecode.chalk.core.stream.IStream;

public class KeepAliveManager implements IKeepAliveManager {
	private KeepAliveConfig config;
	private IStream stream;
	private boolean started;
	
	private IKeepAliveCallback callback;
	
	public KeepAliveManager(IStream stream, KeepAliveConfig config) {
		if (stream == null)
			throw new IllegalArgumentException("Null stream.");
		
		if (config == null)
			throw new IllegalArgumentException("Null keep alive config.");
		
		this.config = config;
		this.stream = stream;
		
		this.callback = createDefaultCallback();
		started = false;
	}
	
	protected IKeepAliveCallback createDefaultCallback() {
		return new DefaultKeepAliveCallback();
	}
	
	private class DefaultKeepAliveCallback implements IKeepAliveCallback {

		@Override
		public void received(Date time, boolean isHeartbeats) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void timeout() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	@Override
	public KeepAliveConfig getConfig() {
		return config;
	}

	@Override
	public void changeConfig(KeepAliveConfig config) {
		this.config = config;
		
		if (!isStarted())
			return;
		
		stop();
		start();
	}

	@Override
	public void start() {
		if (isStarted())
			return;
		
		doStart();
	}
	
	protected void doStart() {
		// TODO
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				started = true;
				
				while (started) {
					
				}
			}
		}).start();

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
		started = false;
		
	}

	@Override
	public void setKeepAliveCallback(IKeepAliveCallback callback) {
		this.callback = callback;
	}

	@Override
	public IKeepAliveCallback getKeepAliveCallback() {
		return callback;
	}

	@Override
	public boolean isStarted() {
		return started;
	}

}
