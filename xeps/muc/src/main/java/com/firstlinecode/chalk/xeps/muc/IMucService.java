package com.firstlinecode.chalk.xeps.muc;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.chalk.core.ErrorException;

public interface IMucService {
	JabberId[] getMucHosts() throws ErrorException;
	IRoom createInstantRoom(JabberId roomJid, String nick) throws ErrorException;
	<T> IRoom createReservedRoom(JabberId roomJid, String nick,
			IRoomConfigurator configurator) throws ErrorException;
	IRoom getRoom(JabberId roomJid);
	void addRoomListener(IRoomListener listener);
	void removeRoomListener(IRoomListener listener);
	int getTotalNumberOfRooms(JabberId hostJid) throws ErrorException;
	PublicRoom[] getPublicRooms(JabberId hostJid);
}
