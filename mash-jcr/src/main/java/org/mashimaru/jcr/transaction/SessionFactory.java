package org.mashimaru.jcr.transaction;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

public class SessionFactory {
	private Repository repository;

	private SessionHolderProvider sessionHolderProvider;

	protected Session getSession() throws RepositoryException {
		return repository.login(new SimpleCredentials("username", "password"
				.toCharArray()));
	}

	public SessionHolder getSessionHolder(Session session) {
		return sessionHolderProvider.createSessionHolder(session);
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}
}
