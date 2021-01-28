package com.firstlinecode.chalk.demo.standard;

import com.firstlinecode.chalk.core.ErrorException;
import com.firstlinecode.chalk.core.stream.StandardStreamConfig;
import com.firstlinecode.chalk.demo.Demo;
import com.firstlinecode.chalk.xeps.muc.events.Invitation;
import com.firstlinecode.chalk.xeps.muc.events.InvitationEvent;
import com.firstlinecode.chalk.xeps.muc.events.RoomEvent;

public class JellyMobile extends StandardClient {

	public JellyMobile(Demo demo) {
		super(demo, "Jelly/mobile");
	}

	@Override
	protected void configureStreamConfig(StandardStreamConfig streamConfig) {
		streamConfig.setResource("mobile");
		streamConfig.setTlsPreferred(true);
	}

	@Override
	protected String[] getUserNameAndPassword() {
		return new String[] {"jelly", "another_pretty_girl"};
	}
	
	@Override
	public void received(RoomEvent<?> event) {
		super.received(event);
		
		if (event instanceof InvitationEvent) {
			Invitation invitation = (Invitation)event.getEventObject();
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				joinRoom(invitation);
			} catch (ErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
