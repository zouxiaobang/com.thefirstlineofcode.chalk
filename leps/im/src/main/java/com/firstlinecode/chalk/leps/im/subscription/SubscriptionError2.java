package com.firstlinecode.chalk.leps.im.subscription;

public class SubscriptionError2 {
	public enum Reason {
		ROSTER_SET_ERROR,
		ROSTER_SET_TIMEOUT,
		SUBSCRIBE_ERROR,
		SUBSCRIBE_TIMEOUT,
		SUBSCRIBED_ERROR,
		SUBSCRIBED_TIMEOUT,
		UNSUBSCRIBE_ERROR,
		UNSUBSCRIBE_TIMEOUT,
		UNSUBSCRIBED_ERROR,
		UNSUBSCRIBED_TIMEOUT
	}
	
	private Reason reason;
	private Object detail;
	
	public SubscriptionError2(Reason reason) {
		this(reason, null);
	}
	
	public SubscriptionError2(Reason reason, Object detail) {
		this.reason = reason;
		this.detail = detail;
	}

	public Reason getReason() {
		return reason;
	}

	public void setReason(Reason reason) {
		this.reason = reason;
	}

	public Object getDetail() {
		return detail;
	}

	public void setDetail(Object detail) {
		this.detail = detail;
	}
}
