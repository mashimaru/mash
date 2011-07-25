package org.mashimaru.jcr.transaction;

import java.io.IOException;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.MergeException;
import javax.jcr.NamespaceException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.PathNotFoundException;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.version.VersionException;

import org.mashimaru.jcr.exception.JcrSystemException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class SessionFactoryUtils {
	public static Session getSession(SessionFactory sessionFactory)
			throws DataAccessException {
		try {
			return doGetSession(sessionFactory);
		} catch (RepositoryException ex) {
			throw new DataAccessResourceFailureException(
					"Could not open Jcr Session", ex);
		}
	}

	public static Session doGetSession(SessionFactory sessionFactory)
			throws RepositoryException {
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager
				.getResource(sessionFactory);

		if ((sessionHolder != null) && (sessionHolder.getSession() != null)) {
			return sessionHolder.getSession();
		}

		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			throw new IllegalStateException(
					"No session bound to thread, "
							+ "and configuration does not allow creation of non-transactional one here");
		}

		Session session = sessionFactory.getSession();

		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			sessionHolder = sessionFactory.getSessionHolder(session);
			sessionHolder.setSynchronizedWithTransaction(true);

			TransactionSynchronizationManager
					.registerSynchronization(new JcrSessionSynchronization(
							sessionHolder, sessionFactory));
			TransactionSynchronizationManager.bindResource(sessionFactory,
					sessionHolder);
		}

		return session;
	}

	public static boolean isSessionThreadBound(Session session,
			SessionFactory sessionFactory) {
		if (sessionFactory == null) {
			return false;
		}

		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager
				.getResource(sessionFactory);

		return ((sessionHolder != null) && (session == sessionHolder
				.getSession()));
	}

	public static void releaseSession(Session session,
			SessionFactory sessionFactory) {
		if (session == null) {
			return;
		}

		if (!isSessionThreadBound(session, sessionFactory)) {
			session.logout();
		}
	}

	public static DataAccessException translateException(RepositoryException e) {
		if (e instanceof AccessDeniedException) {
			return new DataRetrievalFailureException(
					"Access denied to this data", e);
		}
		if (e instanceof ConstraintViolationException) {
			return new DataIntegrityViolationException(
					"Constraint has been violated", e);
		}
		if (e instanceof InvalidItemStateException) {
			return new ConcurrencyFailureException("Invalid item state", e);
		}
		if (e instanceof InvalidQueryException) {
			return new DataRetrievalFailureException("Invalid query", e);
		}
		if (e instanceof InvalidSerializedDataException) {
			return new DataRetrievalFailureException("Invalid serialized data",
					e);
		}
		if (e instanceof ItemExistsException) {
			return new DataIntegrityViolationException(
					"An item already exists", e);
		}
		if (e instanceof ItemNotFoundException) {
			return new DataRetrievalFailureException("Item not found", e);
		}
		if (e instanceof LoginException) {
			return new DataAccessResourceFailureException("Bad login", e);
		}
		if (e instanceof LockException) {
			return new ConcurrencyFailureException("Item is locked", e);
		}
		if (e instanceof MergeException) {
			return new DataIntegrityViolationException("Merge failed", e);
		}
		if (e instanceof NamespaceException) {
			return new InvalidDataAccessApiUsageException(
					"Namespace not registred", e);
		}
		if (e instanceof NoSuchNodeTypeException) {
			return new InvalidDataAccessApiUsageException("No such node type",
					e);
		}
		if (e instanceof NoSuchWorkspaceException) {
			return new DataAccessResourceFailureException(
					"Workspace not found", e);
		}
		if (e instanceof PathNotFoundException) {
			return new DataRetrievalFailureException("Path not found", e);
		}
		if (e instanceof ReferentialIntegrityException) {
			return new DataIntegrityViolationException(
					"Referential integrity violated", e);
		}
		if (e instanceof UnsupportedRepositoryOperationException) {
			return new InvalidDataAccessApiUsageException(
					"Unsupported operation", e);
		}
		if (e instanceof ValueFormatException) {
			return new InvalidDataAccessApiUsageException(
					"Incorrect value format", e);
		}
		if (e instanceof VersionException) {
			return new DataIntegrityViolationException(
					"Invalid version graph operation", e);
		}

		return new JcrSystemException(e);
	}

	public static DataAccessException translateException(IOException e) {
		return new DataAccessResourceFailureException("I/O failure", e);
	}

	private static class JcrSessionSynchronization extends
			TransactionSynchronizationAdapter {
		private final SessionHolder sessionHolder;

		private final SessionFactory sessionFactory;

		private boolean holderActive = true;

		public JcrSessionSynchronization(SessionHolder holder,
				SessionFactory sessionFactory) {
			this.sessionFactory = sessionFactory;
			sessionHolder = holder;
		}

		@Override
		public void suspend() {
			if (holderActive) {
				TransactionSynchronizationManager
						.unbindResource(sessionFactory);
			}
		}

		@Override
		public void resume() {
			if (holderActive) {
				TransactionSynchronizationManager.bindResource(sessionFactory,
						sessionHolder);
			}
		}

		@Override
		public void beforeCompletion() {
			TransactionSynchronizationManager.unbindResource(sessionFactory);
			holderActive = false;

			releaseSession(sessionHolder.getSession(), sessionFactory);
		}
	}
}
