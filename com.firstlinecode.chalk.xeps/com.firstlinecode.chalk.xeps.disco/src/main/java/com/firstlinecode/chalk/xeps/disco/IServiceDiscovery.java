package com.firstlinecode.chalk.xeps.disco;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.chalk.core.ErrorException;

public interface IServiceDiscovery {
	boolean discoImServer() throws ErrorException;
	boolean discoAccount(JabberId account) throws ErrorException;
	JabberId[] discoAvailableResources(JabberId account) throws ErrorException;
}
