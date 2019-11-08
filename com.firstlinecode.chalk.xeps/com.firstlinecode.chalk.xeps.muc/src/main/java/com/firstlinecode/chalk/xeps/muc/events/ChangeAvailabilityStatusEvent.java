package com.firstlinecode.chalk.xeps.muc.events;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public class ChangeAvailabilityStatusEvent extends RoomEvent<ChangeAvailabilityStatus> {

	public ChangeAvailabilityStatusEvent(Stanza source, JabberId roomJid,
			ChangeAvailabilityStatus eventObject) {
		super(source, roomJid, eventObject);
	}

}
