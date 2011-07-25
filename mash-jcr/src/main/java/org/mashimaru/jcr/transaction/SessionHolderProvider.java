package org.mashimaru.jcr.transaction;

import javax.jcr.Session;

public class SessionHolderProvider {
	public SessionHolder createSessionHolder(Session session) {
		return new SessionHolder(session);
	}
}
