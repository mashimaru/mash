package org.mashimaru.jcr.transaction;

import javax.jcr.Session;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class Transaction implements UserTransaction {
	private XAResource xaResource;

	private Xid xid;

	private static byte counter = 0;

	private int status = Status.STATUS_NO_TRANSACTION;

	public Transaction(Session session) {
		xaResource = (XAResource) session;
	}

	@Override
	public void begin() throws NotSupportedException, SystemException {
		if (status != Status.STATUS_NO_TRANSACTION) {
			throw new IllegalStateException("Transaction already active");
		}

		try {
			xid = new XidImp(counter++);
			xaResource.start(xid, XAResource.TMNOFLAGS);
			status = Status.STATUS_ACTIVE;
		} catch (XAException e) {
			throw new SystemException("Unable to begin transaction: "
					+ "XA_ERR=" + e.errorCode);
		}
	}

	@Override
	public void commit() throws RollbackException, HeuristicMixedException,
			HeuristicRollbackException, SecurityException,
			IllegalStateException, SystemException {
		if (status != Status.STATUS_ACTIVE) {
			throw new IllegalStateException("Transaction not active");
		}

		try {
			xaResource.end(xid, XAResource.TMSUCCESS);

			status = Status.STATUS_PREPARING;
			xaResource.prepare(xid);
			status = Status.STATUS_PREPARED;

			status = Status.STATUS_COMMITTING;
			xaResource.commit(xid, false);
			status = Status.STATUS_COMMITTED;
		} catch (XAException e) {
			if ((e.errorCode >= XAException.XA_RBBASE)
					&& (e.errorCode <= XAException.XA_RBEND)) {
				throw new RollbackException(e.toString());
			}

			throw new SystemException("Unable to commit transaction: "
					+ "XA_ERR=" + e.errorCode);
		}
	}

	@Override
	public void rollback() throws IllegalStateException, SecurityException,
			SystemException {
		if ((status != Status.STATUS_ACTIVE)
				&& (status != Status.STATUS_MARKED_ROLLBACK)) {
			throw new IllegalStateException("Transaction not active");
		}

		try {
			xaResource.end(xid, XAResource.TMFAIL);

			status = Status.STATUS_ROLLING_BACK;
			xaResource.rollback(xid);
			status = Status.STATUS_ROLLEDBACK;

		} catch (XAException e) {
			throw new SystemException("Unable to rollback transaction: "
					+ "XA_ERR=" + e.errorCode);
		}
	}

	@Override
	public void setRollbackOnly() throws IllegalStateException, SystemException {
		if (status != Status.STATUS_ACTIVE) {
			throw new IllegalStateException("Transaction not active");
		}

		status = Status.STATUS_MARKED_ROLLBACK;
	}

	@Override
	public int getStatus() throws SystemException {
		return status;
	}

	@Override
	public void setTransactionTimeout(int seconds) throws SystemException {
	}

	private class XidImp implements Xid {
		private final byte[] globalTransactionId;

		public XidImp(byte globalTransactionNumber) {
			globalTransactionId = new byte[] { globalTransactionNumber };
		}

		@Override
		public int getFormatId() {
			return 0;
		}

		@Override
		public byte[] getGlobalTransactionId() {
			return globalTransactionId;
		}

		@Override
		public byte[] getBranchQualifier() {
			return new byte[0];
		}
	}
}
