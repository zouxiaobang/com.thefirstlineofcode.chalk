package com.thefirstlineofcode.chalk.leps.im.subscription;

import java.util.List;

import com.thefirstlineofcode.basalt.protocol.core.JabberId;

public interface ISubscriptionService2 {
	void subscribe(JabberId contact, String message);
	void unsubscribe(JabberId contact);
	void approve(JabberId user);
	void refuse(JabberId user, String reason);
	void addSubscriptionListener(ISubscriptionListener2 listener);
	void removeSubscriptionListener(ISubscriptionListener2 listener);
	List<ISubscriptionListener2> getSubscriptionListeners();
}
