package org.mashimaru.jcr.transaction;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.SmartTransactionObject;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SuppressWarnings("serial")
public class TransactionManager extends AbstractPlatformTransactionManager {
	private SessionFactory sessionFactory;

	@Override
	protected Object doGetTransaction() throws TransactionException {
		TransactionObject transactionObject = new TransactionObject();

		if (TransactionSynchronizationManager.hasResource(sessionFactory)) {
			SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager
					.getResource(sessionFactory);

			transactionObject.setSessionHolder(sessionHolder, false);
		}

		return transactionObject;
	}

	@Override
	protected boolean isExistingTransaction(Object transaction)
			throws TransactionException {
		return ((TransactionObject) transaction).hasTransaction();
	}

	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition)
			throws TransactionException {
		if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
			throw new InvalidIsolationLevelException(
					"JCR does not support an isolation level concept");
		}

		Session session = null;

		try {
			TransactionObject transactionObject = (TransactionObject) transaction;

			if (transactionObject.getSessionHolder() == null) {
				Session newSession = sessionFactory.getSession();

				transactionObject.setSessionHolder(
						new SessionHolder(newSession), true);
			}

			SessionHolder sessionHolder = transactionObject.getSessionHolder();
			sessionHolder.setSynchronizedWithTransaction(true);

			session = sessionHolder.getSession();

			sessionHolder.getTransaction().begin();

			if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
				transactionObject.getSessionHolder().setTimeoutInSeconds(
						definition.getTimeout());
			}

			if (transactionObject.isNewSessionHolder()) {
				TransactionSynchronizationManager.bindResource(sessionFactory,
						sessionHolder);
			}
		} catch (Exception ex) {
			SessionFactoryUtils.releaseSession(session, sessionFactory);

			throw new CannotCreateTransactionException(
					"Could not open JCR session for transaction", ex);
		}
	}

	@Override
	protected Object doSuspend(Object transaction) throws TransactionException {
		TransactionObject transactionObject = (TransactionObject) transaction;
		transactionObject.setSessionHolder(null, false);
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager
				.unbindResource(sessionFactory);

		return new SuspendedResourcesHolder(sessionHolder);
	}

	@Override
	protected void doResume(Object transaction, Object suspendedResources)
			throws TransactionException {
		SuspendedResourcesHolder suspendedResourcesHolder = (SuspendedResourcesHolder) suspendedResources;

		if (TransactionSynchronizationManager.hasResource(sessionFactory)) {
			TransactionSynchronizationManager.unbindResource(sessionFactory);
		}

		TransactionSynchronizationManager.bindResource(sessionFactory,
				suspendedResourcesHolder.getSessionHolder());
	}

	@Override
	protected void doCommit(DefaultTransactionStatus status)
			throws TransactionException {
		TransactionObject transactionObject = (TransactionObject) status
				.getTransaction();

		try {
			transactionObject.getSessionHolder().getTransaction().commit();
		} catch (Exception ex) {
			throw new TransactionSystemException(
					"Could not commit JCR transaction", ex);
		}
	}

	@Override
	protected void doRollback(DefaultTransactionStatus status)
			throws TransactionException {
		TransactionObject transactionObject = (TransactionObject) status
				.getTransaction();

		try {
			transactionObject.getSessionHolder().getTransaction().rollback();
		} catch (Exception ex) {
			throw new TransactionSystemException(
					"Could not roll back JCR transaction", ex);
		} finally {
			if (!transactionObject.isNewSessionHolder()) {
				try {
					transactionObject.getSessionHolder().getSession()
							.refresh(false);
				} catch (RepositoryException e) {
				}
			}
		}
	}

	@Override
	protected void doSetRollbackOnly(DefaultTransactionStatus status)
			throws TransactionException {
		TransactionObject transactionObject = (TransactionObject) status
				.getTransaction();

		transactionObject.setRollbackOnly();
	}

	@Override
	protected void doCleanupAfterCompletion(Object transaction) {
		TransactionObject transactionObject = (TransactionObject) transaction;

		if (transactionObject.isNewSessionHolder()) {
			TransactionSynchronizationManager.unbindResource(sessionFactory);
		}

		Session session = transactionObject.getSessionHolder().getSession();

		if (transactionObject.isNewSessionHolder()) {
			SessionFactoryUtils.releaseSession(session, sessionFactory);
		}

		transactionObject.getSessionHolder().clear();
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	private static class TransactionObject implements SmartTransactionObject {
		private SessionHolder sessionHolder;

		private boolean newSessionHolder;

		public void setSessionHolder(SessionHolder sessionHolder,
				boolean newSessionHolder) {
			this.sessionHolder = sessionHolder;
			this.newSessionHolder = newSessionHolder;
		}

		public SessionHolder getSessionHolder() {
			return sessionHolder;
		}

		public boolean isNewSessionHolder() {
			return newSessionHolder;
		}

		public boolean hasTransaction() {
			return ((sessionHolder != null) && (sessionHolder.getTransaction() != null));
		}

		public void setRollbackOnly() {
			getSessionHolder().setRollbackOnly();
		}

		@Override
		public boolean isRollbackOnly() {
			return getSessionHolder().isRollbackOnly();
		}

		@Override
		public void flush() {
		}
	}

	private static class SuspendedResourcesHolder {
		private final SessionHolder sessionHolder;

		public SuspendedResourcesHolder(SessionHolder sessionHolder) {
			this.sessionHolder = sessionHolder;
		}

		private SessionHolder getSessionHolder() {
			return sessionHolder;
		}
	}
}
