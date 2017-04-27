package wbs.api.mvc;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOrEmptyString;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;

import wbs.web.context.RequestContext;
import wbs.web.handler.WebNotFoundHandler;

@SingletonComponent ("apiNotFoundHandler")
public
class ApiNotFoundHandler
	implements WebNotFoundHandler {

	// singleton dependencies

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// implementation

	@Override
	public
	void handleNotFound (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handleNotFound");

		) {

			// log it normally

			taskLogger.errorFormat (
				"Path not found: %s",
				requestContext.requestUri ());

			// create an exception log

			try {

				String path =
					stringFormat (
						"%s%s",
						requestContext.servletPath (),
						optionalOrEmptyString (
							requestContext.pathInfo ()));

				exceptionLogger.logSimple (
					taskLogger,
					"console",
					path,
					"Not found",
					"The specified path was not found",
					optionalAbsent (),
					GenericExceptionResolution.ignoreWithThirdPartyWarning);

			} catch (RuntimeException exception) {

				taskLogger.fatalFormat (
					"Error creating not found log: %s",
					exception.getMessage ());

			}

			// return an error

			requestContext.sendError (
				404l);

			try (

				FormatWriter formatWriter =
					requestContext.formatWriter ();

			) {

				formatWriter.writeLineFormat (
					"404 Not found");

			}

		}

	}

}
