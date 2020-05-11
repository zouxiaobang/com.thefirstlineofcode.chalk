package com.firstlinecode.chalk.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.protocol.Constants;
import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.oxm.binary.BinaryUtils;
import com.firstlinecode.basalt.oxm.binary.IBinaryXmppProtocolConverter;
import com.firstlinecode.basalt.oxm.binary.IBinaryXmppProtocolFactory;
import com.firstlinecode.basalt.oxm.preprocessing.IMessagePreprocessor;
import com.firstlinecode.basalt.oxm.preprocessing.OutOfMaxBufferSizeException;
import com.firstlinecode.basalt.oxm.preprocessing.XmlMessagePreprocessorAdapter;
import com.firstlinecode.chalk.core.stream.StreamConfig;

public class SocketConnection implements IConnection, HandshakeCompletedListener {
	private static final Logger logger = LoggerFactory.getLogger(SocketConnection.class);
	
	private static final int DEFAULT_READ_QUEUE_SIZE = 64;
	private static final int DEFAULT_WRITE_QUEUE_SIZE = 64;
	private static final int DEFAULT_BLOCKING_TIMEOUT = 20;
	
	private Socket socket;
	private BlockingQueue<String> sendingQueue;
	private BlockingQueue<byte[]> receivingQueue;
	
	private Thread receivingThread;
	private Thread sendingThread;
	private Thread processingThread;
	
	private List<IConnectionListener> listeners;
	
	private HandshakeCompletedEvent handshakeCompletedEvent;
	
	private volatile boolean stopThreadsFlag;
	private int blockingTimeout;
	
	private boolean useBinaryFormat = false;
	private IBinaryXmppProtocolFactory bxmppProtocolFactory;
	private IBinaryXmppProtocolConverter bxmppProtocolConverter;
	
	public SocketConnection() {
		this(null);
	}
	
	public SocketConnection(Socket socket) {
		this(socket, DEFAULT_READ_QUEUE_SIZE, DEFAULT_WRITE_QUEUE_SIZE);
	}
	
	public SocketConnection(Socket socket, int readQueueSize, int writeQueueSize) {
		this(socket, readQueueSize, writeQueueSize, DEFAULT_BLOCKING_TIMEOUT);
	}
	
	public SocketConnection(Socket socket, int readQueueSize, int writeQueueSize, int blockingTimeout) {
		this.socket = socket;
		stopThreadsFlag = false;
		this.blockingTimeout = blockingTimeout;
		
		sendingQueue = new ArrayBlockingQueue<>(writeQueueSize);
		receivingQueue = new ArrayBlockingQueue<>(readQueueSize);
		listeners = new CopyOnWriteArrayList<>();
		
		String messageFormat = System.getProperty(StreamConfig.PROPERTY_NAME_CHALK_MESSAGE_FORMAT);
		if (Constants.MESSAGE_FORMAT_BINARY.equals(messageFormat)) {
			useBinaryFormat = true;
			try {
				bxmppProtocolFactory = createBxmppProtocolConverterFactory();
				bxmppProtocolConverter = bxmppProtocolFactory.createConverter();
			} catch (Exception e) {
				logger.warn("Can't create BXMPP protocol converter. Please add gem bxmpp libraries to your classpath. Ignore to configure message format to binary. Still use XML message format.");
				useBinaryFormat = false;
			}
		}
	}
	
	private IBinaryXmppProtocolFactory createBxmppProtocolConverterFactory() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<?> factoryClass = Class.forName("com.firstlinecode.gem.client.bxmpp.BinaryXmppProtocolFactory");
		return (IBinaryXmppProtocolFactory)factoryClass.newInstance();
	}
	
	@Override
	public void connect(String host, int port) throws ConnectionException {
		try {
			InetSocketAddress address = new InetSocketAddress(host, port);
			if (address.isUnresolved()) {
				throw new ConnectionException(ConnectionException.Type.ADDRESS_IS_UNRESOLVED);
			}
			
			if (socket == null) {
				socket = createSocket();
			}
			
			socket.connect(address);
		} catch (IOException e) {
			throw new ConnectionException(ConnectionException.Type.IO_ERROR, e);
		}
			
		startThreads();
	}

	private void startThreads() {
		stopThreadsFlag = false;
		
		sendingThread = new SendingThread();
		sendingThread.start();
		
		receivingThread = new ReceivingThread();
		receivingThread.start();
		
		processingThread = new ProcessingThread();
		processingThread.start();
	}

	protected Socket createSocket() throws IOException {
		Socket socket = new Socket();
		socket.setSoTimeout(blockingTimeout);
		
		return socket;
	}

	private void processException(ConnectionException exception) {
		for (IConnectionListener listener : listeners) {
			listener.occurred(exception);
		}
		
		// Method processException() is called by SendingThread and ReceivingThread.
		// So we need to run close() method in another thread to avoid dead lock.
		new Thread(new Runnable() {
			@Override
			public void run() {
				close();
			}
		}).start();
	}
	
	@Override
	public void close() {
		stopThreads();
		
		if (socket.isConnected()) {
			try {
				socket.close();
			} catch (IOException e) {
				// ignore
			}
		}
		
		sendingQueue.clear();
		receivingQueue.clear();
		listeners.clear();
	}

	private void stopThreads() {
		stopThreadsFlag = true;
		
		if (processingThread != null) {
			try {
				processingThread.join(blockingTimeout * 2, 0);
			} catch (InterruptedException e) {
				// ignore
			}
			
			processingThread = null;
		}
		
		if (sendingThread != null) {
			try {
				sendingThread.join(blockingTimeout * 2, 0);
			} catch (InterruptedException e) {
				// ignore
			}
			
			sendingThread = null;
		}
		
		if (receivingThread != null) {
			try {
				receivingThread.join(blockingTimeout * 2, 0);
			} catch (InterruptedException e) {
				// ignore
			}
			
			receivingThread = null;
		}
	}

	@Override
	public void write(String message) {
		if (stopThreadsFlag || isClosed()) {
			if (logger.isTraceEnabled()) {
				logger.trace("Can't write message. stopThreadsFlag is {}. isClosed() is {}.",
						stopThreadsFlag, isClosed());
			}
			
			return;
		}
		
		try {
			sendingQueue.put(message);
		} catch (InterruptedException e) {
			// ignore, connection is closed
		}
	}

	@Override
	public void addListener(IConnectionListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public boolean removeListener(IConnectionListener listener) {
		return listeners.remove(listener);
	}
	
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	private class ProcessingThread extends Thread {
		private IMessagePreprocessor messagePreprocessor;
		
		public ProcessingThread() {
			super("Socket Connection Processing Thread");
			
			if (useBinaryFormat) {
				messagePreprocessor = bxmppProtocolFactory.createPreprocessor();
			} else {
				messagePreprocessor = new XmlMessagePreprocessorAdapter();
			}
		}

		public void run() {
			while (true) {
				try {
					byte[] bytes = null;
					bytes = receivingQueue.poll(blockingTimeout, TimeUnit.MILLISECONDS);
					
					if (stopThreadsFlag) {
						break;
					}
					
					if (bytes == null)
						continue;
					
					if (logger.isTraceEnabled()) {
						logger.trace(String.format("Received %d bytes: %s", bytes.length, BinaryUtils.getHexStringFromBytes(bytes)));
					}
					
					String[] messages = processMessages(bytes);
					
					if (messages.length != 0) {
						for (String message : messages) {
							for (IConnectionListener listener : listeners) {
								listener.received(message);
							}
						}
					}
				} catch (InterruptedException e) {
					break;
				}
				
			}
		}

		private String[] processMessages(byte[] bytes) {
			String[] messages;
			try {
				messages = messagePreprocessor.process(bytes);
			} catch (ProtocolException e) {
				messages = new String[0];
				messagePreprocessor.clear();
				processException(new ConnectionException(ConnectionException.Type.BAD_PROTOCOL_MESSAGE, e));
			} catch (OutOfMaxBufferSizeException e) {
				messages = new String[0];
				messagePreprocessor.clear();
				processException(new ConnectionException(ConnectionException.Type.OUT_OF_BUFFER, e));
			}
			
			return messages;
		}
	}
	
	private class ReceivingThread extends Thread {
		private static final int DEFAULT_BUFFER_SIZE = 1024 * 16;
		private InputStream input;
		
		public ReceivingThread() {
			super("Socket Connection Receiving Thread");
		}
		
		@Override
		public void run() {
			try {
				input = new BufferedInputStream(socket.getInputStream());
			} catch (IOException e) {
				processException(new ConnectionException(ConnectionException.Type.IO_ERROR, e));
				return;
			}
			
			byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
			while (true) {
				try {
					int num = input.read(buf, 0, DEFAULT_BUFFER_SIZE);
					
					if (stopThreadsFlag) {
						break;
					}
					
					if (num == -1) {
						// server interrupts the connection?
						// processException(new ConnectionException(ConnectionException.Type.END_OF_STREAM));
						// break;
						continue;
					}
					
					receivingQueue.add(Arrays.copyOf(buf, num));
				} catch (SocketTimeoutException e) {
					if (stopThreadsFlag) {
						break;
					}
				} catch (IOException e) {
					processException(new ConnectionException(ConnectionException.Type.IO_ERROR, e));
					break;
				}
			}
		}
		
	}
	
	private class SendingThread extends Thread {
		private OutputStream output;
		
		public SendingThread() {
			super("Socket Connection Sending Thread");
		}
		
		@Override
		public void run() {
			try {
				output = new BufferedOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				processException(new ConnectionException(ConnectionException.Type.IO_ERROR, e));
				return;
			}
			
			String message = null;
			byte[] bytes = null;
			while (true) {
				try {
					message = sendingQueue.poll(blockingTimeout, TimeUnit.MILLISECONDS);
					
					if (stopThreadsFlag) {
						break;
					}
					
					if (message != null) {
						if (useBinaryFormat) {
							bytes = bxmppProtocolConverter.toBinary(message);
						} else {
							bytes = message.getBytes(Constants.DEFAULT_CHARSET);
						}
						
						output.write(bytes);
						output.flush();
						
						if (logger.isTraceEnabled()) {
							logger.trace(String.format("Sent %d bytes: %s", bytes.length, BinaryUtils.getHexStringFromBytes(bytes)));
						}
						
						for (IConnectionListener listener : listeners) {
							listener.sent(message);
						}
					}
				} catch (InterruptedException e) {
					break;
				} catch (IOException e) {
					processException(new ConnectionException(ConnectionException.Type.IO_ERROR, e));
					break;
				}
			}
		}
	}

	@Override
	public boolean isTlsSupported() {
		return true;
	}

	@Override
	public boolean isTlsStarted() {
		return handshakeCompletedEvent != null;
	}

	@Override
	public X509Certificate[] startTls() throws ConnectionException {
		stopThreads();
		
		try {
			SSLSocket sslSocket = getSslSocket();
			
			sslSocket.setSoTimeout(0);
			sslSocket.addHandshakeCompletedListener(this);
			sslSocket.startHandshake();
			
			sslSocket.setSoTimeout(blockingTimeout);
			
			socket = sslSocket;
		} catch (IOException e) {
			throw new ConnectionException(ConnectionException.Type.IO_ERROR, e);
		} catch (KeyManagementException e) {
			throw new ConnectionException(ConnectionException.Type.TLS_FAILURE, e);
		} catch (NoSuchAlgorithmException e) {
			throw new ConnectionException(ConnectionException.Type.TLS_FAILURE, e);
		}
		
		try {
			synchronized (this) {
				if (handshakeCompletedEvent == null)
					this.wait();
			}
		} catch (InterruptedException e) {
			// ignore
		}
		
		if (handshakeCompletedEvent == null) {
			throw new RuntimeException("handshakeCompletedEvent is null???");
		}
		
		startThreads();
		
		try {
			return handshakeCompletedEvent.getPeerCertificateChain();
		} catch (SSLPeerUnverifiedException e) {
			throw new RuntimeException("Can't get peer certificates.", e);
		}
	}

	private SSLSocket getSslSocket() throws IOException, KeyManagementException, NoSuchAlgorithmException {
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("TLS");
		} catch (NoSuchAlgorithmException e) {
			throw e;
		}
		
		TrustManager[] trustAllCerts = new TrustManager[] {
				new X509ExtendedTrustManager() {
					
					@Override
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}
					
					@Override
					public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws CertificateException {
					}
					
					@Override
					public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws CertificateException {
					}
					
					@Override
					public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1, SSLEngine arg2)
							throws CertificateException {
					}
					
					@Override
					public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1, Socket arg2)
							throws CertificateException {
					}
					
					@Override
					public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1, SSLEngine arg2)
							throws CertificateException {
					}
					
					@Override
					public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1, Socket arg2)
							throws CertificateException {
					}
				}
		};
		
		try {
			sslContext.init(null, trustAllCerts, new SecureRandom());
		} catch (KeyManagementException e) {
			throw e;
		}
		
		SSLSocket sslSocket = (SSLSocket)sslContext.getSocketFactory().createSocket(socket,
				socket.getInetAddress().getHostName(), socket.getPort(), true);
		sslSocket.setUseClientMode(true);
		return sslSocket;
	}

	@Override
	public void handshakeCompleted(HandshakeCompletedEvent event) {
		synchronized (this) {
			handshakeCompletedEvent = event;
			this.notify();
		}
	}

	@Override
	public boolean isClosed() {
		return socket.isClosed();
	}
	
	@Override
	public boolean isConnected() {
		return socket.isConnected();
	}

}
