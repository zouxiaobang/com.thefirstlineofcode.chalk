package com.firstlinecode.chalk;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.stream.Stream;
import com.firstlinecode.basalt.protocol.oxm.IOxmFactory;
import com.firstlinecode.basalt.protocol.oxm.OxmService;
import com.firstlinecode.basalt.protocol.oxm.parsing.IParserFactory;
import com.firstlinecode.basalt.protocol.oxm.translating.ITranslatorFactory;
import com.firstlinecode.chalk.core.stream.IAuthenticationToken;
import com.firstlinecode.chalk.core.stream.INegotiationListener;
import com.firstlinecode.chalk.core.stream.IStream;
import com.firstlinecode.chalk.core.stream.IStreamNegotiant;
import com.firstlinecode.chalk.core.stream.IStreamer;
import com.firstlinecode.chalk.core.stream.NegotiationException;
import com.firstlinecode.chalk.core.stream.StreamConfig;
import com.firstlinecode.chalk.network.ConnectionException;
import com.firstlinecode.chalk.network.IConnectionListener;

public abstract class AbstractChatClient implements IChatClient, IConnectionListener,
		INegotiationListener {
	protected StreamConfig streamConfig;
	protected volatile State state;
	private volatile IStreamer streamer;
	protected volatile IStream stream;
	private Exception exception;
	private IErrorHandler errorHandler;
	private IExceptionHandler exceptionHandler;
	
	private List<IConnectionListener> connectionListeners;
	
	protected IOxmFactory oxmFactory;
	
	protected IChatSystem chatSystem;
	
	private Map<Class<? extends IPlugin>, CounterPluginWrapper> plugins;
	
	private ConcurrentMap<Class<?>, Api> apis;
	
	protected ChatServices chatServices;
	
	private List<INegotiationListener> negotiationListeners;
	
	private volatile String closeStreamMessage;
	
	public AbstractChatClient(StreamConfig streamConfig) {
		this.streamConfig = streamConfig;
		state = State.CLOSED;
		
		connectionListeners = new OrderedList<>(new CopyOnWriteArrayList<IConnectionListener>());
		
		negotiationListeners = new OrderedList<>(new CopyOnWriteArrayList<INegotiationListener>());
		
		oxmFactory = createOxmFactory();
		
		chatSystem = new ChatSystem();
		
		plugins = new HashMap<>();
		
		apis = new ConcurrentHashMap<>();
		
		chatServices = new ChatServices(this);
	}
	
	public State getState() {
		return state;
	}
	
	protected IOxmFactory createOxmFactory() {
		return OxmService.createStandardOxmFactory();
	}
	
	private String getCloseStreamMessage() {
		if (closeStreamMessage != null)
			return closeStreamMessage;
		
		synchronized (oxmFactory) {
			if (closeStreamMessage != null)
				return closeStreamMessage;
			
			closeStreamMessage = oxmFactory.translate(new Stream(true));
			
			return closeStreamMessage;
		}
	}
	
	@Override
	public void setStreamConfig(StreamConfig streamConfig) {
		this.streamConfig = streamConfig;
	}

	@Override
	public StreamConfig getStreamConfig() {
		return streamConfig;
	}

	@Override
	public synchronized void connect(IAuthenticationToken authToken) throws ConnectionException,
				AuthFailureException {
		if (state == State.CONNECTED) {
			throw new IllegalStateException("Client has already connected.");
		}
		
		state = State.CONNECTING;
		
		doConnect(authToken);
		
		try {
			wait();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		if (exception != null) {
			try {
				if (exception instanceof NegotiationException) {
					throw new RuntimeException("Negotiation failed.", exception);
				} else if (exception instanceof AuthFailureException) {
					throw (AuthFailureException)exception;
				} else if (exception instanceof ConnectionException) {
					throw (ConnectionException)exception;
				} else {
					throw new RuntimeException("Unexpected exception.", exception);
				}
			} finally {
				exception = null;
			}
			
		}
	}

	protected void doConnect(IAuthenticationToken authToken) {
		if (streamConfig == null) {
			throw new IllegalStateException("Null stream config.");
		}
		
		exception = null;
		stream = null;
		
		streamer = createStreamer(streamConfig);
		streamer.negotiate(authToken);
	}
	
	protected abstract IStreamer createStreamer(StreamConfig streamConfig);
	
	@Override
	public void register(Class<? extends IPlugin> pluginClass) {
		register(pluginClass, null);
	}
	
	@Override
	public void register(Class<? extends IPlugin> pluginClass, Properties properties) {
		synchronized (plugins) {
			CounterPluginWrapper pluginWrapper;
			if (plugins.containsKey(pluginClass)) {
				pluginWrapper = plugins.get(pluginClass);
			} else {
				pluginWrapper = new CounterPluginWrapper(pluginClass, properties);
			}
			
			pluginWrapper.register();
		}
	}

	@Override
	public void unregister(Class<? extends IPlugin> pluginClass) {
		synchronized (plugins) {
			if (plugins.containsKey(pluginClass)) {
				plugins.get(pluginClass).unregister();
			}
		}
	}
	
	private class CounterPluginWrapper {
		private int count = 0;
		private Class<? extends IPlugin> pluginClass;
		private IPlugin plugin;
		private Properties properties;
		
		public CounterPluginWrapper(Class<? extends IPlugin> pluginClass, Properties properties) {
			this.pluginClass = pluginClass;
			this.properties = properties;
		}
		
		public void register() {
			if (count == 0) {
				try {
					plugin = pluginClass.newInstance();
				} catch (Exception e) {
					throw new RuntimeException(String.format("Can't initialize plugin %s.", pluginClass), e);
				}
				
				plugin.init(chatSystem, properties);
				
				plugins.put(pluginClass, this);
			}
			
			count++;
		}
		
		public void unregister() {
			count--;
			
			if (count == 0) {
				plugin.destroy(chatSystem);
				
				plugins.remove(pluginClass);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T createApi(Class<T> apiType) {
		Api api = apis.get(apiType);
		
		if (api == null)
			throw new RuntimeException(String.format("Api %s not registered.", apiType.getName()));
		
		if (api.singleton) {
			if (api.singletonObject != null) {
				return (T)api.singletonObject;
			}
			
			synchronized (api) {
				if (api.singletonObject != null)
					return (T)api.singletonObject;
				
				api.singletonObject = doCreateApiObject(api);
				
				return (T)api.singletonObject;
			}
		}
		
		return (T)doCreateApiObject(api);
	}

	private Object doCreateApiObject(Api api) {
		Object object = null;
		
		Constructor<?> constructor = null;
		try {
			constructor = api.implClass.getConstructor(IChatServices.class);
		} catch (Exception e) {
			// ignore
		}
		
		if (constructor != null) {
			try {
				object = constructor.newInstance(chatServices);
			} catch (Exception e) {
				throw new RuntimeException("Can't create api object.", e);
			}
		}
		
		if (object == null) {
			try {
				object = api.implClass.newInstance();
			} catch (Exception e) {
				throw new RuntimeException("Can't create api object.", e);
			}
			
			setChatServicesToApiObject(api, object);
		}
		
		for (Object key : api.properties.keySet()) {
			Object value = api.properties.get(key);
			
			try {
				Method writer = getWriter(key.toString(), api.implClass, value.getClass());
				if (writer != null) {
					writer.invoke(object, new Object[] {value});
				} else {
					Field field = api.implClass.getDeclaredField(key.toString());
					if (field != null) {
						boolean oldAccessible = field.isAccessible();
						try {
							field.setAccessible(true);
							field.set(object, value);
						} finally {
							field.setAccessible(oldAccessible);
						}
					}
				}
			} catch (Exception e) {
				// ignore
			}
		}
		
		return object;
	}

	private void setChatServicesToApiObject(Api api, Object object) {
		Method method = null;
		try {
			method = api.implClass.getDeclaredMethod("setChatServices", IChatServices.class);
		} catch (Exception e) {
			// ignore
		}
			
		if (method != null) {
			try {
				method.invoke(object, chatServices);
			} catch (Exception e) {
				throw new RuntimeException("Can't set chat services.", e);
			}
			
			return;
		}
		
		Field field = null;
		try {
			field = api.implClass.getDeclaredField("chatServices");
		} catch (Exception e1) {
			// ignore
		}
		
		if (field != null) {
			if (IChatServices.class.isAssignableFrom(field.getType())) {
				boolean oldAccessible = field.isAccessible();
				field.setAccessible(true);
				try {
					field.set(object, chatServices);
				} catch (Exception e) {
					throw new RuntimeException("Can't set chat services.", e);
				} finally {
					field.setAccessible(oldAccessible);
				}
			}
		}
	}

	private Method getWriter(String propertyName, Class<?> implClass, Class<?> type) {		
		String methodName = "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
		
		try {
			return implClass.getDeclaredMethod(methodName, new Class<?>[] {type});
		} catch (Exception e) {
			if (type == Boolean.class) {
				type = boolean.class;
				return getWriter(propertyName, implClass, type);
			} else if (type == Integer.class) {
				type = int.class;
				return getWriter(propertyName, implClass, type);
			} else if (type == Long.class) {
				type = long.class;
				return getWriter(propertyName, implClass, type);
			} else if (type == Double.class) {
				type = double.class;
				return getWriter(propertyName, implClass, type);
			}
			
			return null;
		}
	}

	@Override
	public void close() {
		close(true);
	}
	
	protected synchronized void close(boolean graceful) {
		if (state == State.CLOSED)
			return;
		
		exception = null;
		
		chatServices.stop();
		
		if (stream != null) {
			if (graceful) {
				stream.getConnection().write(getCloseStreamMessage());
				
				try {
					wait(500);
				} catch (InterruptedException e) {
					// ignore;
				}
			}
			
			if (stream != null && !stream.isClosed()) {
				stream.close();
			}
			
			stream = null;
		}
		
		state = State.CLOSED;
	}

	@Override
	public void before(IStreamNegotiant source) {
		for (INegotiationListener negotiationListener : negotiationListeners) {
			negotiationListener.before(source);
		}
	}

	@Override
	public void after(IStreamNegotiant source) {
		for (INegotiationListener negotiationListener : negotiationListeners) {
			negotiationListener.after(source);
		}
	}

	@Override
	public synchronized void occurred(NegotiationException exception) {
		for (INegotiationListener listener : negotiationListeners) {
			listener.occurred(exception);
		}
		
		this.exception = exception;
		notify();
	}

	@Override
	public synchronized void done(IStream stream) {
		this.stream = stream;
		
		stream.setOxmFactory(oxmFactory);
		
		stream.addConnectionListener(this);
		stream.addErrorListener(chatServices);
		stream.addStanzaListener(chatServices);
		
		chatServices.start();
		
		state = State.CONNECTED;
		
		for (INegotiationListener listener : negotiationListeners) {
			listener.done(stream);
		}
		
		notify();
	}

	@Override
	public void occurred(ConnectionException exception) {
		if (state == State.CONNECTED) {
			if (exception.getType() == ConnectionException.Type.CONNECTION_CLOSED ||
					exception.getType() == ConnectionException.Type.END_OF_STREAM) {
				synchronized (this) {
					close(false);
					
					notify();
				}
			}
			
			for (IConnectionListener connectionListener : connectionListeners) {
				connectionListener.occurred(exception);
			}
		} else if (state == State.CONNECTING) {
			processNegotiationConnectionError(exception);
		} else { // state == State.CLOSED
			// ignore
		}
	}

	private synchronized void processNegotiationConnectionError(ConnectionException exception) {
		this.exception = exception;
		notify();
	}

	@Override
	public void received(String message) {
		for (IConnectionListener connectionListener : connectionListeners) {
			connectionListener.received(message);
		}
	}

	@Override
	public void sent(String message) {
		for (IConnectionListener connectionListener : connectionListeners) {
			connectionListener.sent(message);
		}
	}

	@Override
	public boolean isConnected() {
		return state == State.CONNECTED;
	}

	@Override
	public boolean isClosed() {
		return state == State.CLOSED;
	}
	
	@Override
	public void addNegotiationListener(INegotiationListener negotiationListener) {
		negotiationListeners.add(negotiationListener);
	}

	@Override
	public void removeNegotiationListener(INegotiationListener negotiationListener) {
		negotiationListeners.remove(negotiationListener);
	}
	
	@Override
	public List<INegotiationListener> getNegotiationListeners() {
		return negotiationListeners;
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
	public List<IConnectionListener> getConnectionListeners() {
		return connectionListeners;
	}

	@Override
	public void setDefaultErrorHandler(IErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	@Override
	public IErrorHandler getDefaultErrorHandler() {
		return errorHandler;
	}
	
	public void setDefaultExceptionHandler(IExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}
	
	public IExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}
	
	private class ChatSystem implements IChatSystem {
		@Override
		public void registerParser(ProtocolChain protocolChain, IParserFactory<?> parserFactory) {
			oxmFactory.register(protocolChain, parserFactory);
		}

		@Override
		public void unregisterParser(ProtocolChain protocolChain) {
			oxmFactory.unregister(protocolChain);
		}

		@Override
		public <T> void registerTranslator(Class<T> type, ITranslatorFactory<T> translatorFactory) {
			oxmFactory.register(type, translatorFactory);
		}

		@Override
		public void unregisterTranslator(Class<?> type) {
			oxmFactory.unregister(type);
		}
		
		@Override
		public void registerApi(Class<?> apiType) {
			registerApi(apiType, true);
		}

		@Override
		public <T> void registerApi(Class<T> apiType, Class<? extends T> apiImplType) {
			registerApi(apiType, apiImplType, true);
		}

		@Override
		public void unregisterApi(Class<?> apiType) {
			apis.remove(apiType);
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void registerApi(Class<?> apiType, boolean singleton) {
			registerApi((Class)apiType, apiType, null, singleton);
		}

		@Override
		public <T> void registerApi(Class<T> apiType, Class<? extends T> apiImplType, boolean singleton) {
			registerApi(apiType, apiImplType, null, singleton);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void registerApi(Class<?> apiType, Properties properties) {
			registerApi((Class)apiType, apiType, properties, true);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void registerApi(Class<?> apiType, Properties properties, boolean singleton) {
			registerApi((Class)apiType, apiType, properties, singleton);
		}

		@Override
		public <T> void registerApi(Class<T> apiType, Class<? extends T> apiImplType, Properties properties) {
			registerApi(apiType, apiImplType, properties, true);
		}

		@Override
		public <T> void registerApi(Class<T> apiType, Class<? extends T> apiImplType,
				Properties properties, boolean singleton) {
			apis.putIfAbsent(apiType, new Api(apiImplType, properties, singleton));
		}

		@Override
		public void register(Class<? extends IPlugin> pluginClass) {
			AbstractChatClient.this.register(pluginClass);
		}

		@Override
		public void unregister(Class<? extends IPlugin> pluginClass) {
			AbstractChatClient.this.unregister(pluginClass);
		}

		@Override
		public StreamConfig getStreamConfig() {
			return AbstractChatClient.this.streamConfig;
		}
		
	}
	
	private class Api {
		public Class<?> implClass;
		public Properties properties;
		public boolean singleton;
		public volatile Object singletonObject;
		
		public Api(Class<?> implClass, Properties properties, boolean singleton) {
			this.implClass = implClass;
			this.properties = properties;
			if (this.properties == null) {
				this.properties = new Properties();
			}
			
			this.singleton = singleton;
		}
	}

	public ExecutorService createTaskThreadPool() {
		return Executors.newCachedThreadPool();
	}

	@Override
	public IStream getStream() {
		return stream;
	}
	
	@Override
	public IChatServices getChatServices() {
		return chatServices;
	}
	
	private class OrderComparator<T> implements Comparator<T> {
		@Override
		public int compare(T arg0, T arg1) {
			return getAcceptableOrder(arg0) - getAcceptableOrder(arg1);
		}
		
		private int getAcceptableOrder(Object object) {
			if (object instanceof IOrder) {
				int order = ((IOrder)object).getOrder();
				
				if (order >= IOrder.ORDER_MIN && order <= IOrder.ORDER_MAX) {
					return order;
				} else if (order < IOrder.ORDER_MIN) {
					return IOrder.ORDER_MIN;
				} else {
					return IOrder.ORDER_MAX;
				}
			}
			
			return IOrder.ORDER_NORMAL;
		}
		
	}
	
	private class OrderedList<T> implements List<T> {
		private volatile List<T> original;
		
		public OrderedList(List<T> original) {
			this.original = original;
		}

		@Override
		public synchronized boolean add(T e) {
			List<T> tmp = new ArrayList<>();
			
			tmp.addAll(original);
			boolean result = tmp.add(e);
			Collections.sort(tmp, new OrderComparator<>());
			
			original = new CopyOnWriteArrayList<>(tmp);
			
			return result;
		}

		@Override
		public void add(int index, T element) {
			original.add(index, element);
		}

		@Override
		public boolean addAll(Collection<? extends T> c) {
			return original.addAll(c);
		}

		@Override
		public boolean addAll(int index, Collection<? extends T> c) {
			return original.addAll(index, c);
		}

		@Override
		public void clear() {
			original.clear();
		}

		@Override
		public boolean contains(Object o) {
			return original.contains(o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return original.containsAll(c);
		}

		@Override
		public T get(int index) {
			return original.get(index);
		}

		@Override
		public int indexOf(Object o) {
			return original.indexOf(o);
		}

		@Override
		public boolean isEmpty() {
			return original.isEmpty();
		}

		@Override
		public Iterator<T> iterator() {
			return original.iterator();
		}

		@Override
		public int lastIndexOf(Object o) {
			return original.lastIndexOf(o);
		}

		@Override
		public ListIterator<T> listIterator() {
			return original.listIterator();
		}

		@Override
		public ListIterator<T> listIterator(int index) {
			return original.listIterator(index);
		}

		@Override
		public boolean remove(Object o) {
			return original.remove(o);
		}

		@Override
		public T remove(int index) {
			return original.remove(index);
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return original.removeAll(c);
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return original.retainAll(c);
		}

		@Override
		public T set(int index, T element) {
			return original.set(index, element);
		}

		@Override
		public int size() {
			return original.size();
		}

		@Override
		public List<T> subList(int fromIndex, int toIndex) {
			return original.subList(fromIndex, toIndex);
		}

		@Override
		public Object[] toArray() {
			return original.toArray();
		}

		@Override
		public <E> E[] toArray(E[] a) {
			return original.toArray(a);
		}
		
	}
}
