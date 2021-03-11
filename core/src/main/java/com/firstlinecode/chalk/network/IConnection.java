package com.firstlinecode.chalk.network;

import javax.security.cert.X509Certificate;

public interface IConnection {
	void connect(String host, int port) throws ConnectionException;
	void connect(String host, int port, int timeout) throws ConnectionException;
	void close();
	void write(String message);
	
	boolean isConnected();
	boolean isClosed();
	
	void addListener(IConnectionListener listener);
	boolean removeListener(IConnectionListener listener);
	
	boolean isTlsSupported();
	boolean isTlsStarted();
	X509Certificate[] startTls() throws ConnectionException;
}
