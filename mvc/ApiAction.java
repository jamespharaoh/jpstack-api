package wbs.api.mvc;

import static wbs.framework.utils.etc.StringUtils.emptyStringIfNull;
import static wbs.framework.utils.etc.StringUtils.joinWithoutSeparator;

import javax.inject.Provider;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeDependency;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.web.Action;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;

public
abstract class ApiAction
	implements Action {

	// singleton dependencies

	@SingletonDependency
	ApplicationContext applicationContext;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ApiErrorResponder> apiErrorResponder;

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
					emptyStringIfNull (
						requestContext.pathInfo ()));

			exceptionLogger.logThrowable (
				"webapi",
				path,
				exception,
				Optional.absent (),
				GenericExceptionResolution.ignoreWithThirdPartyWarning);

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

				return applicationContext.getComponentRequired (
					name,
					Responder.class);

			}

		};

	}

}
