package com.thefirstlineofcode.chalk.xeps.muc.events;

import com.thefirstlineofcode.basalt.protocol.core.JabberId;
import com.thefirstlineofcode.basalt.protocol.core.stanza.Stanza;

public class KickedEvent extends RoomEvent<Kicked> {

	public KickedEvent(Stanza source, JabberId roomJid, Kicked eventObject) {
		super(source, roomJid, eventObject);
	}

}
