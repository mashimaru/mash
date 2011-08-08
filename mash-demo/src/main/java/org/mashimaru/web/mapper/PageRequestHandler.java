package org.mashimaru.web.mapper;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.handler.IPageProvider;
import org.apache.wicket.request.handler.IPageRequestHandler;
import org.apache.wicket.request.handler.RenderPageRequestHandler;

public class PageRequestHandler extends RenderPageRequestHandler implements
		IRequestHandler, IPageRequestHandler {
	public PageRequestHandler(IPageProvider pageProvider) {
		super(pageProvider);
	}
}
