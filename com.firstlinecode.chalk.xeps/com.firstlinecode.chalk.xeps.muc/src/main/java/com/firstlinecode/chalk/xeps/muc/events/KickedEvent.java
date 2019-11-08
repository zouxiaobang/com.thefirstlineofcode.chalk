package com.firstlinecode.chalk.xeps.muc.events;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public class KickedEvent extends RoomEvent<Kicked> {

	public KickedEvent(Stanza source, JabberId roomJid, Kicked eventObject) {
		super(source, roomJid, eventObject);
	}

}
