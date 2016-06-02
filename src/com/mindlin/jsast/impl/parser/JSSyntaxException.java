package com.mindlin.jsast.impl.parser;

public class JSSyntaxException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8236793790942441074L;
	
	public JSSyntaxException(String message) {
		super(message);
	}
	public JSSyntaxException(String message, long position) {
		this(message + " at " + position);
	}
}
