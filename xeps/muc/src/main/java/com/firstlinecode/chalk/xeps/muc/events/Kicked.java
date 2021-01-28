package com.firstlinecode.chalk.xeps.muc.events;

import com.firstlinecode.basalt.xeps.muc.Affiliation;
import com.firstlinecode.basalt.xeps.muc.Role;

public class Kicked extends Kick {

	public Kicked(String nick, Affiliation affiliation, Role role) {
		super(nick, affiliation, role);
	}

}
