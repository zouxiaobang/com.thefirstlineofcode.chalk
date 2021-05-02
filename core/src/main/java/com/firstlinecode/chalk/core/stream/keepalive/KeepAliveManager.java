package com.firstlinecode.chalk.core.stream.keepalive;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.protocol.Constants;
import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.chalk.core.stream.IStream;
import com.firstlinecode.chalk.core.stream.StreamConfig;
import com.firstlinecode.chalk.network.ConnectionException;
import com.firstlinecode.chalk.network.IConnectionListener;

public class KeepAliveManager implements IKeepAliveManager, IConnectionListener {
	private static final Logger logger = LoggerFactory.getLogger(KeepAliveManager.class);
			
	public static final char CHAR_HEART_BEAT = ' ';
	public  static final byte BYTE_HEART_BEAT = (byte)CHAR_HEART_BEAT;
	
	protected static final String XML_CLOSE_STREAM = "</stream:stream>";
	protected static final byte[] BINARY_CLOSE_STREAM = new byte[] {-1, -4, -1};
	
	protected KeepAliveConfig config;
	protected IStream stream;
	protected boolean started;
	protected boolean useBinaryFormat;
	protected KeepAliveThread keepAliveThread;
	
	protected IKeepAliveCallback callback;
	protected long lastMessageReceivedTime;
	protected long lastMessageSentTime;
	
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
		
		this.callback = createDefaultCallback();
		started = false;
	}
	
	protected IKeepAliveCallback createDefaultCallback() {
		return new DefaultKeepAliveCallback();
	}
	
	private class DefaultKeepAliveCallback implements IKeepAliveCallback {

		@Override
		public void received(Date currentTime, boolean isHeartbeats) {
			lastMessageReceivedTime = currentTime.getTime();
			
			if (logger.isTraceEnabled())
				logger.trace(String.format("Keep-alive thread has received a heartbeat at %s.", currentTime.toString()));
		}
		
		@Override
		public void sent(Date currentTime, boolean isHeartbeats) {
			lastMessageSentTime = currentTime.getTime();
			
			if (logger.isTraceEnabled())
				logger.trace(String.format("Keep-alive thread has sent a heartbeat at %s.", currentTime.toString()));
		}

		@Override
		public void timeout(final IStream stream) {
			new Thread() {
				@Override
				public void run() {
					stream.close(true);
				}
			}.start();
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
		if (keepAliveThread == null) {
			keepAliveThread = new KeepAliveThread(stream.getJid());
		}
		
		keepAliveThread.start();
		if (logger.isInfoEnabled()) {
			logger.info("Keep-alive thread of client connection({}) has started.", stream.getJid());
		}		
	}
	
	protected class KeepAliveThread extends Thread {
		private boolean stop;
		private JabberId jid;
		
		KeepAliveThread(JabberId jid) {
			super("Client Keep-alive Thread");
			this.jid = jid;
		}
		
		@Override
		public void run() {
			started = true;
			
			lastMessageSentTime = currentTime().getTime();
			lastMessageReceivedTime = currentTime().getTime();
			while (!stop) {
				if (stream.isClosed()) {
					if (logger.isWarnEnabled()) {
						logger.warn("Keep-alive thread of client connection({}) can't work. The stream has closed.", jid);
					}
					break;
				}
				
				if (getInactiveTime() > config.getInterval()) {
					if (useBinaryFormat) {
						stream.getConnection().write(new byte[BYTE_HEART_BEAT]);
					} else {
						stream.getConnection().write(String.valueOf(CHAR_HEART_BEAT));
					}
					
					if (logger.isTraceEnabled()) {
						logger.trace("Keep-alive thread of client connection({}) sent a heart beat.", jid);
					}
					
					callback.sent(currentTime(), true);
				}
				
				try {
					Thread.sleep(config.getCheckingInterval());
				} catch (InterruptedException e) {
					throw new RuntimeException("Keep-alive thread of client connection({}) throws an exception.", e);
				}
				
				if (getServerInactiveTime() > config.getTimeout()) {
					if (logger.isWarnEnabled())
						logger.warn("Keeping-alive thread of client connection({}) has timeouted. Keep-alive callback's timeout() will be called.", jid);
					
					callback.timeout(stream);
				}
			}
			
			if (logger.isInfoEnabled()) {
				logger.info("Keep-alive thread of client connection({}) has stopped.", jid);
			}
			started = false;
		}

		protected long getInactiveTime() {
			return currentTime().getTime() - lastMessageSentTime;
		}

		protected long getServerInactiveTime() {
			return currentTime().getTime() - lastMessageReceivedTime;
		}
		
		public void exit() {
			stop = true;
		}
	}
	
	protected Date currentTime() {
		return Calendar.getInstance().getTime();
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
		callback.received(currentTime(), false);
	}

	@Override
	public void messageSent(String message) {
		callback.sent(currentTime(), false);
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
			throw new RuntimeException("Is thread interrupted???", e);
		}
	}

	@Override
	public void heartBeatsReceived(int length) {
		callback.received(currentTime(), true);
	}
	
	@Override
	public void setCallback(IKeepAliveCallback callback) {
		this.callback = callback;
	}
	
	@Override
	public IKeepAliveCallback getCallback() {
		return callback;
	}

}
