package org.mashimaru.web;

import org.apache.wicket.Page;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.mashimaru.web.page.HomePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebApplication extends
		org.apache.wicket.protocol.http.WebApplication {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory
			.getLogger(WebApplication.class);

	@Override
	protected void init() {
		super.init();

		getComponentInstantiationListeners().add(
				new SpringComponentInjector(this));
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return HomePage.class;
	}
}
