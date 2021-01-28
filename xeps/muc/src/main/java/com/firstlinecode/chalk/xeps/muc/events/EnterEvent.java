package com.firstlinecode.chalk.xeps.muc.events;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public class EnterEvent extends RoomEvent<Enter> {

	public EnterEvent(Stanza source, JabberId roomJid, Enter enter) {
		super(source, roomJid, enter);
	}

}
