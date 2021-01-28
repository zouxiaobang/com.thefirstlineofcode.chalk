package com.firstlinecode.chalk.xeps.muc.events;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public class DiscussionHistoryEvent extends RoomEvent<RoomMessage> {

	public DiscussionHistoryEvent(Stanza source, JabberId roomJid, RoomMessage eventObject) {
		super(source, roomJid, eventObject);
	}

}
