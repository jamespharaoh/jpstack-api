package wbs.api.mvc;

import java.io.IOException;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.web.RequestContext;
import wbs.framework.web.RequestHandler;
import wbs.framework.web.Responder;

@SingletonComponent ("webApiManager")
public
class WebApiManager {

	// singleton dependencies

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	// request handler

	public
	class WebApiActionRequestHandler
		implements RequestHandler {

		final
		WebApiAction action;

		public
		WebApiActionRequestHandler (
				WebApiAction action) {

			this.action =
				action;

		}

		@Override
		public
		void handle ()
			throws IOException {

			try {

				Responder responder =
					action.go ();

				if (responder != null) {
					responder.execute ();
					return;
				}

			} catch (Exception exception) {

				exceptionLogger.logThrowable (
					"webapi",
					requestContext.requestUri (),
					exception,
					Optional.absent (),
					GenericExceptionResolution.ignoreWithThirdPartyWarning);

			}

			Responder responder =
				action.makeFallbackResponder ()
					.get ();

			responder.execute ();

		}

	}

	public
	RequestHandler makeWebApiActionRequestHandler (
			@NonNull WebApiAction action) {

		return new WebApiActionRequestHandler (
			action);

	}

}
