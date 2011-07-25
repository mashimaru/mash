package org.mashimaru.web.config;

import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

@WebFilter(urlPatterns = "/*", initParams = {
		@WebInitParam(name = "applicationClassName", value = "org.mashimaru.web.WebApplication"),
		@WebInitParam(name = "filterMappingUrlPattern", value = "/*"),
		@WebInitParam(name = "configuration", value = "development") })
public class WicketFilter extends org.apache.wicket.protocol.http.WicketFilter {
}
