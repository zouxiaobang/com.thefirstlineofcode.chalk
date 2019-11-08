package com.firstlinecode.chalk.im.roster;

import com.firstlinecode.basalt.protocol.im.roster.Roster;

public interface IRosterListener {
	void retrieved(Roster roster);
	void occurred(RosterError error);
	void updated(Roster roster);
}
