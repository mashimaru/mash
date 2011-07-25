package org.mashimaru.jcr.transaction;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jcr.Repository;

import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;
import org.xml.sax.InputSource;

public class RepositoryProvider implements FactoryBean<Repository> {
	private Repository repository;

	private RepositoryConfig repositoryConfig;

	private Resource configuration;

	private String homeDirectory;

	@PostConstruct
	public void init() throws Exception {
		repositoryConfig = RepositoryConfig.create(new InputSource(
				configuration.getInputStream()), homeDirectory);
		repository = RepositoryImpl.create(repositoryConfig);
	}

	@PreDestroy
	public void destroy() {
		if (repository instanceof JackrabbitRepository) {
			((JackrabbitRepository) repository).shutdown();
		}
	}

	@Override
	public Repository getObject() throws Exception {
		return repository;
	}

	@Override
	public Class<?> getObjectType() {
		return Repository.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public void setConfiguration(Resource configuration) {
		this.configuration = configuration;
	}

	public void setHomeDirectory(String homeDirectory) {
		this.homeDirectory = homeDirectory;
	}
}
