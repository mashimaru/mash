package org.mashimaru.jcr.service;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

public interface JcrCallback {
	Object doInJcr(Session session) throws IOException, RepositoryException;
}
