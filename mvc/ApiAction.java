package wbs.api.mvc;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOrEmptyString;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.action.Action;
import wbs.web.context.RequestContext;
import wbs.web.responder.Responder;

public
abstract class ApiAction
	implements Action {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ApiErrorResponder> apiErrorResponder;

	// hooks

	protected abstract
	Responder goApi (
			TaskLogger parentTaskLogger);

	// implementation

	@Override
	public final
	Responder handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handle");

		) {

			try {

				return goApi (
					taskLogger);

			} catch (RuntimeException exception) {

				// record the exception

				String path =
					joinWithoutSeparator (
						requestContext.servletPath (),
						optionalOrEmptyString (
							requestContext.pathInfo ()));

				exceptionLogger.logThrowable (
					taskLogger,
					"webapi",
					path,
					exception,
					optionalAbsent (),
					GenericExceptionResolution.ignoreWithThirdPartyWarning);

				// and show a simple error page

				return apiErrorResponder.get ();

			}

		}

	}

	// utils

	protected
	Provider <Responder> reusableResponder (
			@NonNull String name) {

		return new Provider <Responder> () {

			@Override
			public
			Responder get () {

				try (

					OwnedTaskLogger taskLogger =
						logContext.createTaskLogger (
							"reusableResponder.Provider.get");

				) {

					return componentManager.getComponentRequired (
						taskLogger,
						name,
						Responder.class);

				}

			}

		};

	}

	protected
	Responder responder (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String name) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"responder");

		) {

			return componentManager.getComponentRequired (
				taskLogger,
				name,
				Responder.class);

		}

	}

}
