package org.mashimaru.jcr.exception;

import org.springframework.dao.UncategorizedDataAccessException;

@SuppressWarnings("serial")
public class JcrSystemException extends UncategorizedDataAccessException {
	public JcrSystemException(String message, Throwable ex) {
		super(message, ex);
	}

	public JcrSystemException(Throwable ex) {
		super("Repository access exception", ex);
	}
}
