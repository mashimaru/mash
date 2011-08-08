package org.mashimaru.web.mapper;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;

public class RequestMapper implements IRequestMapper {
	@Override
	public IRequestHandler mapRequest(Request request) {
		Url url = request.getClientUrl();

		System.out.println(url);

		return null;
	}

	@Override
	public int getCompatibilityScore(Request request) {
		return 0;
	}

	@Override
	public Url mapHandler(IRequestHandler requestHandler) {
		return null;
	}
}
