package com.firstlinecode.chalk.xeps.muc;

import com.firstlinecode.chalk.xeps.muc.events.RoomEvent;

public interface IRoomListener {
	void received(RoomEvent<?> event);
}
