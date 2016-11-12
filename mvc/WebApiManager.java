package wbs.api.mvc;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.web.handler.RequestHandler;

@SingletonComponent ("webApiManager")
public
class WebApiManager {

	// prototype dependencies

	@PrototypeDependency
	Provider <WebApiActionRequestHandler> webApiActionRequestHandlerProvider;

	// request handler

	public
	RequestHandler makeWebApiActionRequestHandler (
			@NonNull WebApiAction action) {

		return webApiActionRequestHandlerProvider.get ()

			.action (
				action);

	}

}
