package com.firstlinecode.chalk.leps.im.subscription;

import java.util.ArrayList;
import java.util.List;

import com.firstlinecode.basalt.leps.im.subscription.Subscribe;
import com.firstlinecode.basalt.leps.im.subscription.Subscribed;
import com.firstlinecode.basalt.leps.im.subscription.Unsubscribe;
import com.firstlinecode.basalt.leps.im.subscription.Unsubscribed;
import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.error.StanzaError;
import com.firstlinecode.basalt.protocol.im.roster.Item;
import com.firstlinecode.basalt.protocol.im.roster.Roster;
import com.firstlinecode.chalk.IChatServices;
import com.firstlinecode.chalk.ITask;
import com.firstlinecode.chalk.IUnidirectionalStream;
import com.firstlinecode.chalk.core.stanza.IIqListener;
import com.firstlinecode.chalk.im.roster.IRosterService;
import com.firstlinecode.chalk.leps.im.subscription.SubscriptionError2.Reason;

public class LepSubscriptionService implements ISubscriptionService2, IIqListener {
	private IChatServices chatServices;
	private List<ISubscriptionListener2> listeners;
	private IRosterService rosterService;
	
	public LepSubscriptionService(IChatServices chatServices, IRosterService rosterService) {
		this.chatServices = chatServices;
		this.rosterService = rosterService;
		this.listeners = new ArrayList<>();
		
		chatServices.getIqService().addListener(this);
	}

	@Override
	public void subscribe(JabberId contact, String message) {
		if (rosterService.getLocal().exists(contact)) {
			ask(contact, message);
		} else {
			rosterSetAndAsk(contact, message);
		}
	}

	private void rosterSetAndAsk(final JabberId contact, final String message) {
		chatServices.getTaskService().execute(new ITask<Iq>() {

			@Override
			public void trigger(IUnidirectionalStream<Iq> stream) {
				Item item = new Item();
				item.setJid(contact);
				
				Roster roster = new Roster();
				roster.addOrUpdate(item);
				
				Iq iq = new Iq();
				iq.setType(Iq.Type.SET);
				iq.setObject(roster);
				
				stream.send(iq);
			}

			@Override
			public void processResponse(IUnidirectionalStream<Iq> stream, Iq iq) {
				ask(contact, message);
			}

			@Override
			public boolean processError(IUnidirectionalStream<Iq> stream, StanzaError error) {
				for (ISubscriptionListener2 listener : listeners) {
					listener.occurred(new SubscriptionError2(Reason.ROSTER_SET_ERROR, error));
				}
				
				return true;
			}

			@Override
			public boolean processTimeout(IUnidirectionalStream<Iq> stream, Iq iq) {
				for (ISubscriptionListener2 listener : listeners) {
					listener.occurred(new SubscriptionError2(Reason.ROSTER_SET_TIMEOUT, iq));
				}
				
				return true;
			}

			@Override
			public void interrupted() {}
			
		});		
	}

	private void ask(final JabberId contact, final String message) {
		chatServices.getTaskService().execute(new ITask<Iq>() {

			@Override
			public void trigger(IUnidirectionalStream<Iq> stream) {
				Iq iq = new Iq(Iq.Type.SET);
				iq.setTo(contact);
				iq.setObject(new Subscribe(message));
				
				stream.send(iq);
			}

			@Override
			public void processResponse(IUnidirectionalStream<Iq> stream, Iq iq) {}

			@Override
			public boolean processError(IUnidirectionalStream<Iq> stream, StanzaError error) {
				for (ISubscriptionListener2 listener : listeners) {
					listener.occurred(new SubscriptionError2(Reason.SUBSCRIBE_ERROR, error));
				}
				
				return true;
			}

			@Override
			public boolean processTimeout(IUnidirectionalStream<Iq> stream, Iq iq) {
				for (ISubscriptionListener2 listener : listeners) {
					listener.occurred(new SubscriptionError2(Reason.SUBSCRIBE_TIMEOUT, iq));
				}
				
				return true;
			}

			@Override
			public void interrupted() {}
			
		});
	}
	
	@Override
	public void unsubscribe(final JabberId contact) {
		chatServices.getTaskService().execute(new ITask<Iq>() {

			@Override
			public void trigger(IUnidirectionalStream<Iq> stream) {
				Unsubscribe unsubscribe = new Unsubscribe();
				
				Iq iq = new Iq(Iq.Type.SET);
				iq.setTo(contact);
				iq.setObject(unsubscribe);
				
				stream.send(iq);
			}

			@Override
			public void processResponse(IUnidirectionalStream<Iq> stream, Iq iq) {}

			@Override
			public boolean processError(IUnidirectionalStream<Iq> stream, StanzaError error) {
				for (ISubscriptionListener2 listener : listeners) {
					listener.occurred(new SubscriptionError2(Reason.UNSUBSCRIBE_ERROR, error));
				}
				
				return true;
			}

			@Override
			public boolean processTimeout(IUnidirectionalStream<Iq> stream, Iq iq) {
				for (ISubscriptionListener2 listener : listeners) {
					listener.occurred(new SubscriptionError2(Reason.UNSUBSCRIBE_TIMEOUT, iq));
				}
				
				return true;
			}

			@Override
			public void interrupted() {}
			
		});

	}

	@Override
	public void approve(final JabberId user) {
		chatServices.getTaskService().execute(new ITask<Iq>() {

			@Override
			public void trigger(IUnidirectionalStream<Iq> stream) {
				Subscribed subscribed = new Subscribed();
				
				Iq iq = new Iq(Iq.Type.RESULT);
				iq.setTo(user);
				iq.setObject(subscribed);
				
				stream.send(iq);
			}

			@Override
			public void processResponse(IUnidirectionalStream<Iq> stream, Iq iq) {}

			@Override
			public boolean processError(IUnidirectionalStream<Iq> stream, StanzaError error) {
				for (ISubscriptionListener2 listener : listeners) {
					listener.occurred(new SubscriptionError2(Reason.SUBSCRIBED_ERROR, error));
				}
				
				return true;
			}

			@Override
			public boolean processTimeout(IUnidirectionalStream<Iq> stream, Iq iq) {
				for (ISubscriptionListener2 listener : listeners) {
					listener.occurred(new SubscriptionError2(Reason.SUBSCRIBED_TIMEOUT, iq));
				}
				
				return true;
			}

			@Override
			public void interrupted() {}
			
		});
	}

	@Override
	public void refuse(final JabberId user, final String reason) {
		chatServices.getTaskService().execute(new ITask<Iq>() {

			@Override
			public void trigger(IUnidirectionalStream<Iq> stream) {
				Iq iq = new Iq(Iq.Type.RESULT);
				iq.setTo(user);
				iq.setObject(new Unsubscribed(reason));
				
				stream.send(iq);
			}

			@Override
			public void processResponse(IUnidirectionalStream<Iq> stream, Iq iq) {}

			@Override
			public boolean processError(IUnidirectionalStream<Iq> stream, StanzaError error) {
				for (ISubscriptionListener2 listener : listeners) {
					listener.occurred(new SubscriptionError2(Reason.UNSUBSCRIBED_ERROR, error));
				}
				
				return true;
			}

			@Override
			public boolean processTimeout(IUnidirectionalStream<Iq> stream, Iq iq) {
				for (ISubscriptionListener2 listener : listeners) {
					listener.occurred(new SubscriptionError2(Reason.UNSUBSCRIBED_TIMEOUT, iq));
				}
				
				return true;
			}

			@Override
			public void interrupted() {}
			
		});
	}

	@Override
	public void addSubscriptionListener(ISubscriptionListener2 listener) {
		listeners.add(listener);
	}

	@Override
	public void removeSubscriptionListener(ISubscriptionListener2 listener) {
		listeners.remove(listener);
	}

	@Override
	public void received(final Iq iq) {
		if (iq.getObject() == null) {
			return;
		}
		
		if (!isSubscriptionObject(iq.getObject())) {
			return;
		}
		
		Iq affirm = new Iq(Iq.Type.RESULT, iq.getId());
		chatServices.getIqService().send(affirm);
		
		if (iq.getObject() instanceof Subscribe) {
			Subscribe subscribe = (Subscribe)iq.getObject();
			for (ISubscriptionListener2 listener : listeners) {
				listener.asked(iq.getFrom(), subscribe.getMessage());
			}
		} else if (iq.getObject() instanceof Unsubscribe) {
			chatServices.getTaskService().execute(new ITask<Iq>() {

				@Override
				public void trigger(IUnidirectionalStream<Iq> stream) {
					Iq autoReply = new Iq(Iq.Type.RESULT);
					autoReply.setTo(iq.getFrom().getBareId());
					autoReply.setObject(new Unsubscribed());
					
					stream.send(autoReply);
				}

				@Override
				public void processResponse(IUnidirectionalStream<Iq> stream, Iq iq) {}

				@Override
				public boolean processError(IUnidirectionalStream<Iq> stream, StanzaError error) {
					for (ISubscriptionListener2 listener : listeners) {
						listener.occurred(new SubscriptionError2(Reason.UNSUBSCRIBED_ERROR, error));
					}
					
					return true;
				}

				@Override
				public boolean processTimeout(IUnidirectionalStream<Iq> stream, Iq stanza) {
					for (ISubscriptionListener2 listener : listeners) {
						listener.occurred(new SubscriptionError2(Reason.UNSUBSCRIBED_TIMEOUT, iq));
					}
					
					return true;
				}

				@Override
				public void interrupted() {}
				
			});
			
			for (ISubscriptionListener2 listener : listeners) {
				listener.revoked(iq.getFrom());
			}
		} else if (iq.getObject() instanceof Subscribed) {
			for (ISubscriptionListener2 listener : listeners) {
				listener.approved(iq.getFrom());
			}
		} else { // (iq.getObject() instanceof Unsubscribed)
			Unsubscribed unsubscribed = (Unsubscribed)iq.getObject();
			for (ISubscriptionListener2 listener : listeners) {
				listener.refused(iq.getFrom(), unsubscribed.getReason());
			}
		}
	}

	private boolean isSubscriptionObject(Object object) {
		if (object == null)
			return false;
		
		return object instanceof Subscribe ||
				object instanceof Subscribed ||
				object instanceof Unsubscribe ||
				object instanceof Unsubscribed;
	}

	@Override
	public List<ISubscriptionListener2> getSubscriptionListeners() {
		return listeners;
	}

}
