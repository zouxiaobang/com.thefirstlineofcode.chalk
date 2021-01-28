package com.firstlinecode.chalk.xeps.muc.events;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public class KickEvent extends RoomEvent<Kick> {

	public KickEvent(Stanza source, JabberId roomJid, Kick eventObject) {
		super(source, roomJid, eventObject);
	}

}
