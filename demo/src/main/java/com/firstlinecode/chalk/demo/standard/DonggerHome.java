package com.firstlinecode.chalk.demo.standard;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.LangText;
import com.firstlinecode.basalt.protocol.im.roster.Roster;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.basalt.protocol.im.stanza.Presence.Show;
import com.firstlinecode.chalk.core.AuthFailureException;
import com.firstlinecode.chalk.core.ErrorException;
import com.firstlinecode.chalk.core.stream.StandardStreamConfig;
import com.firstlinecode.chalk.demo.Demo;
import com.firstlinecode.chalk.network.ConnectionException;
import com.firstlinecode.chalk.xeps.muc.IRoom;
import com.firstlinecode.chalk.xeps.muc.events.EnterEvent;
import com.firstlinecode.chalk.xeps.muc.events.Invitation;
import com.firstlinecode.chalk.xeps.muc.events.InvitationEvent;
import com.firstlinecode.chalk.xeps.muc.events.RoomEvent;
import com.firstlinecode.chalk.xeps.muc.events.RoomMessageEvent;
import com.firstlinecode.chalk.xeps.ping.IPing;
import com.firstlinecode.chalk.xeps.ping.PingPlugin;

public class DonggerHome extends StandardClient {

	public DonggerHome(Demo demo) {
		super(demo, "Dongger/home");
	}
	
/*	@Override
	public void received(String message) {
		print(String.format("received: %s", message));
	}

	@Override
	public void sent(String message) {
		print(String.format("sent: %s", message));
	}*/

	@Override
	public void retrieved(Roster roster) {
		super.retrieved(roster);
		
		if (getRunCount().intValue() == 1) {
			ping();
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			im.getSubscriptionService().subscribe(BARE_JID_AGILEST, "Hello!");
			
			demo.startClient(this.getClass(), AgilestMobile.class);
		}
	}

	private void setPresence() {
		Presence presence = new Presence();
		presence.setShow(Show.DND);
		presence.getStatuses().add(new LangText("I'm in a meeting."));
		
		im.send(presence);
	}
	
	@Override
	public void asked(JabberId user, String message) {
		super.asked(user, message);
		
		im.getSubscriptionService().approve(user);
		
		demo.startClient(this.getClass(), DonggerOffice.class);
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setPresence();
	}
	
	@Override
	public void refused(JabberId contact, String reason) {
		super.refused(contact, reason);
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		demo.stopClient(this.getClass(), AgilestMobile.class);
		
		im.getSubscriptionService().subscribe(BARE_JID_AGILEST, "I'm Dongger!");
		
		demo.startClient(this.getClass(), AgilestMobile.class);
		demo.startClient(this.getClass(), AgilestPad.class);
	}

	private void ping() {
		IPing ping = chatClient.createApi(IPing.class);
		
		IPing.Result result = ping.ping();
		print(String.format("Ping result: %s.", result));
	}

	@Override
	protected void configureStreamConfig(StandardStreamConfig streamConfig) {
		streamConfig.setResource("home");
		streamConfig.setTlsPreferred(true);
	}
	
	@Override
	protected void registerPlugins() {
		super.registerPlugins();
		
		chatClient.register(PingPlugin.class);
	}

	@Override
	protected String[] getUserNameAndPassword() {
		return new String[] {"dongger", "a_clever_man"};
	}
	
	@Override
	protected void processAuthFailure(AuthFailureException e) throws AuthFailureException, ConnectionException {
		chatClient.connect("dongger", "a_stupid_man");
	}
	
	@Override
	public void received(Message message) {
		super.received(message);
		
		if (!JID_AGILEST_PAD.equals(message.getFrom())) {
			return;
		}
		
		if (message.getBodies().get(0).getText().indexOf("Hello") != -1) {
			im.send(BARE_JID_AGILEST, new Message("Hello, Agilest!"));
		} else {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			demo.stopClient(this.getClass(), AgilestMobile.class);
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			demo.stopClient(this.getClass(), AgilestPad.class);
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			demo.stopClient(this.getClass(), SmartSheepMobile.class);
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			demo.stopClient(this.getClass(), JellyMobile.class);
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			im.send(JID_AGILEST_MOBILE, new Message("Are you still online?"));
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			demo.startClient(this.getClass(), AgilestMobile.class);
		}
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
		}  else if (event instanceof EnterEvent) {
			EnterEvent enterEvent = (EnterEvent)event;
			if ("first_room_of_agilest".equals(enterEvent.getRoomJid().getNode()) &&
					"dongger".equals(enterEvent.getEventObject().getNick()) &&
					enterEvent.getEventObject().getSessions() == 1) {
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				IRoom room = muc.getRoom(new JabberId("first_room_of_agilest", mucHost.getDomain()));
				room.send(new Message("Hello, everyone!"));
			}
		} else if (event instanceof RoomMessageEvent) {
			RoomMessageEvent messageEvent = ((RoomMessageEvent)event);
			if ("first_room_of_agilest".equals(messageEvent.getRoomJid().getNode()) &&
					"agilest".equals(messageEvent.getEventObject().getNick())) {
			
				IRoom room = muc.getRoom(new JabberId("first_room_of_agilest", mucHost.getDomain()));
				room.send("smartsheep", new Message("How has my nephew been recently?"));
				
				room.send(new Presence(Presence.Show.AWAY));
			}
		}
	}
}
