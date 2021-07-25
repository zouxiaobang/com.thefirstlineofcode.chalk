package com.firstlinecode.chalk.core.stream;

import java.lang.reflect.Array;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.firstlinecode.basalt.oxm.IOxmFactory;
import com.firstlinecode.basalt.oxm.OxmService;
import com.firstlinecode.basalt.oxm.parsing.FlawedProtocolObject;
import com.firstlinecode.basalt.protocol.core.IError;
import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.basalt.protocol.core.stanza.error.InternalServerError;
import com.firstlinecode.basalt.protocol.core.stanza.error.ServiceUnavailable;
import com.firstlinecode.basalt.protocol.core.stanza.error.StanzaError;
import com.firstlinecode.basalt.protocol.core.stream.error.StreamError;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.chalk.core.IErrorListener;
import com.firstlinecode.chalk.core.stanza.IStanzaListener;
import com.firstlinecode.chalk.core.stream.keepalive.IKeepAliveManager;
import com.firstlinecode.chalk.core.stream.keepalive.KeepAliveConfig;
import com.firstlinecode.chalk.core.stream.keepalive.KeepAliveManager;
import com.firstlinecode.chalk.network.ConnectionException;
import com.firstlinecode.chalk.network.ConnectionException.Type;
import com.firstlinecode.chalk.network.IConnection;
import com.firstlinecode.chalk.network.IConnectionListener;

public class Stream implements IStream, IConnectionListener {
	public enum State {
		DONE,
		CLOSED
	}
	
	private IConnection connection;
	private volatile IOxmFactory oxmFactory;
	
	private List<IStanzaListener> stanzaListeners;
	private List<IErrorListener> errorListeners;
	private List<IConnectionListener> connectionListeners;
	private List<IStanzaWatcher> stanzaWatchers;
	
	private JabberId jid;
	private StreamConfig streamConfig;
	private volatile String closeStreamMessage;
	
	private ExecutorService threadPool;
	
	private IKeepAliveManager keepAliveManager;
	
	public Stream(JabberId jid, StreamConfig streamConfig, IConnection connection) {
		this(jid, streamConfig, connection, null);
	}
	
	public Stream(JabberId jid, StreamConfig streamConfig, IConnection connection, IOxmFactory oxmFactory) {
		this.jid = jid;
		this.streamConfig = streamConfig;
		this.connection = connection;
		this.oxmFactory = oxmFactory != null ? oxmFactory : getOxmFactory();
		
		stanzaListeners = new CopyOnWriteArrayList<>();
		errorListeners = new CopyOnWriteArrayList<>();
		connectionListeners = new CopyOnWriteArrayList<>();
		stanzaWatchers = new CopyOnWriteArrayList<>();
		
		connection.addListener(this);
		
		threadPool = Executors.newCachedThreadPool();
		keepAliveManager = new KeepAliveManager(this, getKeepaliveConfig());
	}

	private KeepAliveConfig getKeepaliveConfig() {
		if (streamConfig instanceof StandardStreamConfig)
			return ((StandardStreamConfig)streamConfig).getKeepAliveConfig();
		
		return new KeepAliveConfig();
	}
	
	@Override
	public IOxmFactory getOxmFactory() {
		if (oxmFactory != null)
			return oxmFactory;
		
		synchronized (this) {
			if (oxmFactory != null)
				return oxmFactory;
			
			oxmFactory = createOxmFactory();
			
			return oxmFactory;
		}
	}
	
	protected IOxmFactory createOxmFactory() {
		return OxmService.createStandardOxmFactory();
	}

	@Override
	public void send(Stanza stanza) {
		String message = getOxmFactory().translate(stanza);
		connection.write(message);
		
		for (IStanzaWatcher stanzaWatcher : stanzaWatchers) {
			stanzaWatcher.sent(stanza, message);
		}
	}
	
	@Override
	public void addStanzaListener(IStanzaListener stanzaListener) {
		stanzaListeners.add(stanzaListener);
	}
	
	@Override
	public void removeStanzaListener(IStanzaListener stanzaListener) {
		stanzaListeners.remove(stanzaListener);
	}

	@Override
	public void addErrorListener(IErrorListener errorListener) {
		errorListeners.add(errorListener);
	}

	@Override
	public void removeErrorListener(IErrorListener errorListener) {
		errorListeners.remove(errorListener);
	}

	@Override
	public void close() {
		close(true);
	}
	
	@Override
	public void close(boolean graceful) {
		if (keepAliveManager != null && keepAliveManager.isStarted()) {
			keepAliveManager.stop();
		}
		
		if (connection == null)
			return;
				
		if (graceful) {
			String closeStreamMessage = getCloseStreamMessage();
			connection.write(closeStreamMessage);
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// Ignore.
			}
		}
		
		connection.close();
		connection = null;
		
		threadPool.shutdown();
	}
	
	private String getCloseStreamMessage() {
		if (closeStreamMessage != null)
			return closeStreamMessage;
		
		synchronized (oxmFactory) {
			if (closeStreamMessage != null)
				return closeStreamMessage;
			
			closeStreamMessage = oxmFactory.translate(new com.firstlinecode.basalt.protocol.core.stream.Stream(true));
			return closeStreamMessage;
		}
	}
	
	@Override
	public synchronized boolean isClosed() {
		if (connection == null)
			return true;
		
		return connection.isClosed();
	}

	@Override
	public synchronized void exceptionOccurred(ConnectionException exception) {
		for (IConnectionListener connectionListener : connectionListeners) {
			connectionListener.exceptionOccurred(exception);
		}
	}
	
	private class MessageProcessingThread implements Runnable {
		private String message;
		
		public MessageProcessingThread(String message) {
			this.message = message;
		}
		
		@Override
		public void run() {
			Object object = null;
			IError error = null;
			try {
				object = getOxmFactory().parse(message);
			} catch (ProtocolException e) {
				error = e.getError();
			} catch (RuntimeException e) {
				error = new InternalServerError(String.format("Error description: '%s', '%s'.", e.getClass().getName(), e.getMessage()));
			}
			
			if (error != null) {
				for (IErrorListener errorListener : errorListeners) {
					errorListener.occurred(error);
				}
				
				return;
			}
			
			if (object instanceof StanzaError) {
				for (IStanzaWatcher stanzaWatcher : stanzaWatchers) {
					stanzaWatcher.received((StanzaError)object, message);
				}
				
				for (IErrorListener errorListener : errorListeners) {
					errorListener.occurred((IError)object);
				}
			} else if (object instanceof StreamError) {
				for (IErrorListener errorListener : errorListeners) {
					errorListener.occurred((IError)object);
				}
			} else if (object instanceof com.firstlinecode.basalt.protocol.core.stream.Stream) {
				com.firstlinecode.basalt.protocol.core.stream.Stream closeStream =
					(com.firstlinecode.basalt.protocol.core.stream.Stream)object;
				
				if (closeStream.isClose()) {
					exceptionOccurred(new ConnectionException(Type.CONNECTION_CLOSED));
				}
			} else if (object instanceof Stanza) {
				Stanza stanza = (Stanza)object;
				boolean flawedFound = removeFlawed(stanza);
				
				if (stanza instanceof Iq) {
					Iq iq = (Iq)stanza;
					
					// (rfc3921 2.4)
					// If an entity receives an IQ stanza of type "get" or "set" containing a child element
					// qualified by a namespace it does not understand, the entity SHOULD return an
					// IQ stanza of type "error" with an error condition of <service-unavailable/>.
					if (flawedFound && iq.getType() == Iq.Type.SET || iq.getType() == Iq.Type.GET) {
						for (IErrorListener errorListener : errorListeners) {
							errorListener.occurred(new ServiceUnavailable());
						}
					}
				} else if (stanza instanceof Message) {
					// (rfc3921 2.4)
					// If an entity receives a message stanza whose only child element is qualified by a
					// namespace it does not understand, it MUST ignore the entire stanza.
					Message messageStanza = (Message)stanza;
					if (flawedFound &&
							messageStanza.getSubjects().size() == 0 &&
							messageStanza.getBodies().size() == 0 &&
							messageStanza.getObjects().size() == 0 &&
							messageStanza.getThread() == null) {
						// ignore the entire stanza
						return;
					}
				}
				
				for (IStanzaWatcher stanzaWatcher : stanzaWatchers) {
					stanzaWatcher.received(stanza, message);
				}
				
				for (IStanzaListener stanzaListener : stanzaListeners) {
					stanzaListener.received(stanza);
				}
			} else {
				// ???
				throw new RuntimeException(String.format("Unknown object[%s] received.", object.getClass().getName()));
			}
		}

		private boolean removeFlawed(Stanza stanza) {
			FlawedProtocolObject flawed = null;
			for (Object protocolObject : stanza.getObjects()) {
				if (protocolObject instanceof FlawedProtocolObject) {
					flawed = (FlawedProtocolObject)protocolObject;
				}
			}
			
			if (flawed == null)
				return false;
			
			stanza.getObjects().remove(flawed);
			stanza.getObjectProtocols().remove(FlawedProtocolObject.class);
			
			return true;
		}
		
	}

	@Override
	public void messageReceived(String message) {
		for (IConnectionListener connectionListener : connectionListeners) {
			connectionListener.messageReceived(message);
		}
		
		threadPool.execute(new MessageProcessingThread(message));
	}
	
	@Override
	public void messageSent(String message) {
		for (IConnectionListener connectionListener : connectionListeners) {
			connectionListener.messageSent(message);
		}
	}

	@Override
	public IStanzaListener[] getStanzaListeners() {
		return listToArray(stanzaListeners, IStanzaListener.class);
	}

	@Override
	public IErrorListener[] getErrorListeners() {
		return listToArray(errorListeners, IErrorListener.class);
	}

	@Override
	public void addConnectionListener(IConnectionListener connectionListener) {
		connectionListeners.add(connectionListener);
	}

	@Override
	public void removeConnectionListener(IConnectionListener connectionListener) {
		connectionListeners.remove(connectionListener);
	}

	@Override
	public IConnectionListener[] getConnectionListeners() {
		return listToArray(connectionListeners, IConnectionListener.class);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T[] listToArray(List<T> list, Class<T> clazz) {
		T[] array = (T[])Array.newInstance(clazz, list.size());
		return list.toArray(array);
	}

	@Override
	public void setOxmFactory(IOxmFactory oxmFactory) {
		this.oxmFactory = oxmFactory;
	}

	@Override
	public JabberId getJid() {
		return jid;
	}

	@Override
	public IConnection getConnection() {
		return connection;
	}

	@Override
	public StreamConfig getStreamConfig() {
		return streamConfig;
	}

	@Override
	public void addStanzaWatcher(IStanzaWatcher stanzaWatcher) {
		stanzaWatchers.add(stanzaWatcher);
	}

	@Override
	public void removeStanzaWatcher(IStanzaWatcher stanzaWatcher) {
		stanzaWatchers.remove(stanzaWatcher);
	}

	@Override
	public IStanzaWatcher[] getStanzaWatchers() {
		return listToArray(stanzaWatchers, IStanzaWatcher.class);
	}

	@Override
	public IKeepAliveManager getKeepAliveManager() {
		return keepAliveManager;
	}

	@Override
	public void heartBeatsReceived(int length) {
		for (IConnectionListener connectionListener : connectionListeners) {
			connectionListener.heartBeatsReceived(length);
		}
	}
}
