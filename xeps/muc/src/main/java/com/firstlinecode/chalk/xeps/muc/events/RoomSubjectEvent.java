package com.firstlinecode.chalk.xeps.muc.events;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public class RoomSubjectEvent extends RoomEvent<RoomSubject> {

	public RoomSubjectEvent(Stanza source, JabberId roomJid, RoomSubject subject) {
		super(source, roomJid, subject);
	}

}
