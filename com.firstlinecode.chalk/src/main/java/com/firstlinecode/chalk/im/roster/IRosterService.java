package com.firstlinecode.chalk.im.roster;

import com.firstlinecode.basalt.protocol.im.roster.Roster;

public interface IRosterService {
	void retrieve();
	void retrieve(int timeout);
	void add(Roster roster);
	void update(Roster roster);
	void delete(Roster roster);
	void addRosterListener(IRosterListener listener);
	void removeRosterListener(IRosterListener listener);
	IRosterListener[] getRosterListeners();
	
	Roster getLocal();
	
}
