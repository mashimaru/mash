package org.mashimaru.jcr.transaction;

import javax.jcr.Session;

import org.springframework.transaction.support.ResourceHolderSupport;

public class SessionHolder extends ResourceHolderSupport {
	private Session session;

	private Transaction transaction;

	public SessionHolder(Session session) {
		this.session = session;

		transaction = new Transaction(session);
	}

	@Override
	public void clear() {
		super.clear();

		session = null;
		transaction = null;
	}

	public Session getSession() {
		return session;
	}

	public Transaction getTransaction() {
		return transaction;
	}
}
