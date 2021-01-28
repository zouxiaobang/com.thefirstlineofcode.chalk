package com.firstlinecode.chalk.xeps.muc.events;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public class ExitEvent extends RoomEvent<Exit> {

	public ExitEvent(Stanza source, JabberId roomJid, Exit exit) {
		super(source, roomJid, exit);
	}

}
