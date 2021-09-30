package com.thefirstlineofcode.chalk.xeps.muc.events;

import com.thefirstlineofcode.basalt.protocol.core.JabberId;
import com.thefirstlineofcode.basalt.protocol.core.stanza.Stanza;

public class InvitationEvent extends RoomEvent<Invitation> {

	public InvitationEvent(Stanza source, JabberId roomJid, Invitation invitation) {
		super(source, roomJid, invitation);
	}

}
