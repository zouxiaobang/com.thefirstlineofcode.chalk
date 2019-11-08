package com.firstlinecode.chalk.xeps.muc.events;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public class RoomMessageEvent extends RoomEvent<RoomMessage> {

	public RoomMessageEvent(Stanza source, JabberId roomJid, RoomMessage message) {
		super(source, roomJid, message);
	}

}
