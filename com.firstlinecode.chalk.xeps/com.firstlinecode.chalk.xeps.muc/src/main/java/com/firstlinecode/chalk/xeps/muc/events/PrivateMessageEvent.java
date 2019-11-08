package com.firstlinecode.chalk.xeps.muc.events;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public class PrivateMessageEvent extends RoomEvent<RoomMessage> {

	public PrivateMessageEvent(Stanza source, JabberId roomJid, RoomMessage message) {
		super(source, roomJid, message);
	}
	
}
