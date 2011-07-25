package org.mashimaru.web.config;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebListener;

import org.springframework.web.context.ConfigurableWebApplicationContext;

@WebListener
public class ContextLoaderListener extends
		org.springframework.web.context.ContextLoaderListener {
	@Override
	protected void customizeContext(ServletContext servletContext,
			ConfigurableWebApplicationContext applicationContext) {
		applicationContext
				.setConfigLocation("/WEB-INF/web-application-context.xml");
	}
}
