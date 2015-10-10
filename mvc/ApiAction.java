package wbs.api.mvc;

import static wbs.framework.utils.etc.Misc.joinWithoutSeparator;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.context.ApplicationContext;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.web.Action;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;

import com.google.common.base.Optional;

public
abstract class ApiAction
	implements Action {

	// dependencies

	@Inject
	ApplicationContext applicationContext;

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	RequestContext requestContext;

	@Inject
	Provider<ApiErrorResponder> apiErrorResponder;

	// hooks

	protected abstract
	Responder goApi ();

	// implementation

	@Override
	public final
	Responder handle () {

		try {

			return goApi ();

		} catch (RuntimeException exception) {

			// record the exception

			String path =
				joinWithoutSeparator (
					requestContext.servletPath (),
					requestContext.pathInfo () != null
						? requestContext.pathInfo ()
						: "");

			exceptionLogger.logThrowable (
				"webapi",
				path,
				exception,
				Optional.<Integer>absent (),
				false);

			// and show a simple error page

			return apiErrorResponder.get ();

		}

	}

	// utils

	protected
	Provider<Responder> responder (
			final String name) {

		return new Provider<Responder> () {

			@Override
			public
			Responder get () {

				return applicationContext.getBean (
					name,
					Responder.class);

			}

		};

	}

}
