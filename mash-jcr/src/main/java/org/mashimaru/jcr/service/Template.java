package org.mashimaru.jcr.service;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.mashimaru.jcr.transaction.SessionFactory;
import org.mashimaru.jcr.transaction.SessionFactoryUtils;
import org.springframework.dao.DataAccessException;

public class Template {
	private SessionFactory sessionFactory;

	public Object execute(JcrCallback action) throws DataAccessException {
		Session session = getSession();

		try {
			Object result = action.doInJcr(session);

			return result;
		} catch (RepositoryException e) {
			throw SessionFactoryUtils.translateException(e);
		} catch (IOException e) {
			throw SessionFactoryUtils.translateException(e);
		} catch (RuntimeException e) {
			throw e;
		} finally {
			SessionFactoryUtils.releaseSession(session, sessionFactory);
		}
	}

	private Session getSession() {
		return SessionFactoryUtils.getSession(sessionFactory);
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
}
