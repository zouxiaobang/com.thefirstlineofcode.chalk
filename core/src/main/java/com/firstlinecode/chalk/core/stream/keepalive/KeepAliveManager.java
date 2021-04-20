package com.firstlinecode.chalk.core.stream.keepalive;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.protocol.Constants;
import com.firstlinecode.chalk.core.stream.IStream;
import com.firstlinecode.chalk.core.stream.StreamConfig;
import com.firstlinecode.chalk.network.ConnectionException;
import com.firstlinecode.chalk.network.IConnectionListener;

public class KeepAliveManager implements IKeepAliveManager, IConnectionListener {
	private static final Logger logger = LoggerFactory.getLogger(KeepAliveManager.class);
			
	private static final char CHAR_HEART_BEAT = ' ';
	private static final byte BYTE_HEART_BEAT = (byte)CHAR_HEART_BEAT;
	private static final int MIN_HEARTBEATS_CHECKING_INTERVAL = 1000; 
	
	private KeepAliveConfig config;
	private IStream stream;
	private boolean started;
	private boolean useBinaryFormat;
	private KeepAliveThread keepAliveThread;
	
	private IKeepAliveCallback callback;
	private long lastMessageReceivedTime;
	private long lastMessageSentTime;
	private int heartbeatsCheckingInterval;
	
	public KeepAliveManager(IStream stream, KeepAliveConfig config) {
		if (stream == null)
			throw new IllegalArgumentException("Null stream.");
		
		useBinaryFormat = false;
		String messageFormat = System.getProperty(StreamConfig.PROPERTY_NAME_CHALK_MESSAGE_FORMAT);
		if (Constants.MESSAGE_FORMAT_BINARY.equals(messageFormat)) {
			useBinaryFormat = true;
		}
		
		if (config == null)
			throw new IllegalArgumentException("Null keep-alive config.");
		
		this.config = config;
		this.stream = stream;
		
		heartbeatsCheckingInterval = (config.getInterval() > MIN_HEARTBEATS_CHECKING_INTERVAL) ?
				config.getInterval() : MIN_HEARTBEATS_CHECKING_INTERVAL;
		
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
		if (keepAliveThread != null) {
			keepAliveThread = new KeepAliveThread();
		}
		
		keepAliveThread.start();
		if (logger.isInfoEnabled()) {
			logger.info("Keep-alive thread has started.");
		}		
	}
	
	private class KeepAliveThread extends Thread {
		private boolean stop;
		
		KeepAliveThread() {
			super("Client Keep-alive Thread");
		}
		
		@Override
		public void run() {
			started = false;
			
			lastMessageReceivedTime = currentTime();
			while (!stop) {
				if (stream.isClosed()) {
					if (logger.isWarnEnabled()) {
						logger.warn("Keep-alive manager can't work. Stream has closed.");
					}
					break;						
				}
				
				if (getInactiveTime() > config.getInterval()) {
					if (useBinaryFormat)
						stream.getConnection().write(new byte[BYTE_HEART_BEAT]);
					else
						stream.getConnection().write(String.valueOf(CHAR_HEART_BEAT));
					
					if (logger.isTraceEnabled())
						logger.trace("Keep-alive thread has sent a heartbeat.");
				}
				
				try {
					Thread.sleep(heartbeatsCheckingInterval);
				} catch (InterruptedException e) {
					throw new RuntimeException("Keep-alive thread throws an exception.", e);
				}
				
				if (getServerInactiveTime() > config.getTimeout()) {
					if (logger.isWarnEnabled())
						logger.warn("Keeping-alive timeouted. Callback's timeout() will be called.");
					
					callback.timeout();
				}
			}
			
			if (logger.isInfoEnabled()) {
				logger.info("Keep alive thread has stopped.");
			}
			started = false;
		}

		private long getInactiveTime() {
			return currentTime() - lastMessageSentTime;
		}

		private long getServerInactiveTime() {
			return currentTime() - lastMessageReceivedTime;
		}
		
		public void exit() {
			stop = true;
		}
	}
	
	private long currentTime() {
		return Calendar.getInstance().getTime().getTime();
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

	@Override
	public void exceptionOccurred(ConnectionException exception) {
		// Do nothing.
	}

	@Override
	public void messageReceived(String message) {
		if (!stream.isConnected())
			return;
		
		lastMessageReceivedTime = currentTime();
	}

	@Override
	public void messageSent(String message) {
		lastMessageSentTime = currentTime();
	}

	@Override
	public void stop() {
		if (!isStarted())
			return;
		
		if (keepAliveThread == null)
			throw new IllegalStateException("Null keep alive thread.");
		
		keepAliveThread.exit();
		try {
			keepAliveThread.join();
		} catch (InterruptedException e) {
			throw new RuntimeException("Can't exist keep-alive thread correctly.", e);
		}
	}

	@Override
	public void heartBeatsReceived(int length) {
		lastMessageReceivedTime = currentTime();
	}

}
