package com.firstlinecode.chalk.xeps.muc.events;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;

public class ChangeNickEvent extends RoomEvent<ChangeNick> {

	public ChangeNickEvent(Stanza source, JabberId roomJid, ChangeNick changeNick) {
		super(source, roomJid, changeNick);
	}

}
