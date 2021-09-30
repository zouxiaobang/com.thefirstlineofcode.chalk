package com.thefirstlineofcode.chalk.xeps.muc.events;

import com.thefirstlineofcode.basalt.protocol.core.JabberId;
import com.thefirstlineofcode.basalt.protocol.core.stanza.Stanza;

public class DiscussionHistoryEvent extends RoomEvent<RoomMessage> {

	public DiscussionHistoryEvent(Stanza source, JabberId roomJid, RoomMessage eventObject) {
		super(source, roomJid, eventObject);
	}

}
