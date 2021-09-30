package com.thefirstlineofcode.chalk.xeps.muc.events;

import com.thefirstlineofcode.basalt.protocol.core.JabberId;
import com.thefirstlineofcode.basalt.protocol.core.stanza.Stanza;

public class ChangeAvailabilityStatusEvent extends RoomEvent<ChangeAvailabilityStatus> {

	public ChangeAvailabilityStatusEvent(Stanza source, JabberId roomJid,
			ChangeAvailabilityStatus eventObject) {
		super(source, roomJid, eventObject);
	}

}
