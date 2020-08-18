package com.starcut.auth.jwt.exception;

public class NonUniqueJtiException extends Exception {

	private static final long serialVersionUID = -473357958653506137L;

	public NonUniqueJtiException() {
		super();
	}

	public NonUniqueJtiException(String message) {
		super(message);
	}

}
