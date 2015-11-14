package wbs.api.mvc;

import java.io.IOException;

import javax.inject.Inject;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionLogger.Resolution;
import wbs.framework.web.RequestContext;
import wbs.framework.web.RequestHandler;
import wbs.framework.web.Responder;

@SingletonComponent ("webApiManager")
public
class WebApiManager {

	// dependencies

	@Inject
	RequestContext requestContext;

	@Inject
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
					Optional.<Integer>absent (),
					Resolution.ignoreWithThirdPartyWarning);

			}

			Responder responder =
				action.makeFallbackResponder ()
					.get ();

			responder.execute ();

		}

	}

	public
	RequestHandler makeWebApiActionRequestHandler (
			WebApiAction action) {

		return new WebApiActionRequestHandler (
			action);

	}

}
