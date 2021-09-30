package com.thefirstlineofcode.chalk.core;

public class AuthFailureException extends Exception {
	private static final long serialVersionUID = 8052873857188360437L;

	public AuthFailureException() {
		super();
	}

	public AuthFailureException(String message, Throwable cause) {
		super(message, cause);
	}

	public AuthFailureException(String message) {
		super(message);
	}

	public AuthFailureException(Throwable cause) {
		super(cause);
	}
}
