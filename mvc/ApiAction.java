package wbs.api.mvc;

import static wbs.utils.string.StringUtils.emptyStringIfNull;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
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
			TaskLogger taskLogger);

	// implementation

	@Override
	public final
	Responder handle (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handle");

		try {

			return goApi (
				taskLogger);

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
	Provider <Responder> reusableResponder (
			@NonNull TaskLogger taskLogger,
			@NonNull String name) {

		return new Provider <Responder> () {

			@Override
			public
			Responder get () {

				return componentManager.getComponentRequired (
					taskLogger,
					name,
					Responder.class);

			}

		};

	}

	protected
	Responder responder (
			@NonNull TaskLogger taskLogger,
			@NonNull String name) {

		return componentManager.getComponentRequired (
			taskLogger,
			name,
			Responder.class);

	}

}
