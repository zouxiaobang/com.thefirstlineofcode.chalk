package com.thefirstlineofcode.chalk.leps.im.subscription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thefirstlineofcode.basalt.protocol.core.JabberId;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.chalk.im.roster.IRosterService;
import com.thefirstlineofcode.chalk.im.subscription.ISubscriptionListener;
import com.thefirstlineofcode.chalk.im.subscription.ISubscriptionService;
import com.thefirstlineofcode.chalk.im.subscription.SubscriptionError;
import com.thefirstlineofcode.chalk.im.subscription.SubscriptionService;

public class StandardSubscriptionService implements ISubscriptionService2 {
	private ISubscriptionService subscriptionService;
	private Map<ISubscriptionListener2, ISubscriptionListener> listeners;
	
	public StandardSubscriptionService(IChatServices chatServices, IRosterService rosterService) {
		subscriptionService = new SubscriptionService(chatServices, rosterService);
		listeners = new HashMap<>();
	}

	@Override
	public void subscribe(JabberId contact, String message) {
		subscriptionService.subscribe(contact);
	}

	@Override
	public void unsubscribe(JabberId contact) {
		subscriptionService.unsubscribe(contact);
	}

	@Override
	public void approve(JabberId user) {
		subscriptionService.approve(user);
	}

	@Override
	public void refuse(JabberId user, String reason) {
		subscriptionService.refuse(user);
	}

	@Override
	public synchronized void addSubscriptionListener(ISubscriptionListener2 listener) {
		ISubscriptionListener adaptor = new SubscriptionListenerAdaptor(listener);
		listeners.put(listener, adaptor);
		subscriptionService.addSubscriptionListener(adaptor);
	}

	@Override
	public synchronized void removeSubscriptionListener(ISubscriptionListener2 listener) {
		ISubscriptionListener adaptor = listeners.remove(listener);
		if (adaptor != null) {
			subscriptionService.removeSubscriptionListener(adaptor);
		}
	}
	
	private class SubscriptionListenerAdaptor implements ISubscriptionListener {
		private ISubscriptionListener2 real;
		
		public SubscriptionListenerAdaptor(ISubscriptionListener2 real) {
			this.real = real;
		}

		@Override
		public void asked(JabberId user) {
			real.asked(user, null);
		}

		@Override
		public void approved(JabberId contact) {
			real.approved(contact);
		}

		@Override
		public void refused(JabberId contact) {
			real.refused(contact, null);
		}

		@Override
		public void revoked(JabberId user) {
			real.revoked(user);
		}

		@Override
		public void occurred(SubscriptionError error) {
			real.occurred(getSubscriptionError2(error));
		}

		private SubscriptionError2 getSubscriptionError2(SubscriptionError error) {
			SubscriptionError2 error2;
			if (error.getReason() == SubscriptionError.Reason.ROSTER_SET_ERROR) {
				error2 = new SubscriptionError2(SubscriptionError2.Reason.ROSTER_SET_ERROR);
			} else {
				error2 = new SubscriptionError2(SubscriptionError2.Reason.ROSTER_SET_TIMEOUT);
			}
			
			error2.setDetail(error.getDetail());
			
			return error2;
		}
		
	}

	@Override
	public List<ISubscriptionListener2> getSubscriptionListeners() {
		return Collections.unmodifiableList(new ArrayList<>(listeners.keySet()));
	}

}
