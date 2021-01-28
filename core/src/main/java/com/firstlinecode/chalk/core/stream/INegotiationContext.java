package com.firstlinecode.chalk.core.stream;

import java.util.Set;

import com.firstlinecode.chalk.network.IConnection;


public interface INegotiationContext extends IConnection {
	void setAttribute(Object key, Object value);
	Object getAttribute(Object key);
	boolean removeAttribute(Object key);
	Set<Object> getAttributeKeys();
	
	IConnection getConnection();
}
