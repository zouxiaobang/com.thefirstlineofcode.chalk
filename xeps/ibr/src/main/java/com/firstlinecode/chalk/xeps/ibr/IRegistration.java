package com.firstlinecode.chalk.xeps.ibr;

import com.firstlinecode.chalk.core.stream.INegotiationListener;
import com.firstlinecode.chalk.network.IConnectionListener;

public interface IRegistration {
	void register(IRegistrationCallback callback) throws RegistrationException;
	void remove();
	void addConnectionListener(IConnectionListener listener);
	void removeConnectionListener(IConnectionListener listener);
	void addNegotiationListener(INegotiationListener listener);
	void removeNegotiationListener(INegotiationListener listener);
}
