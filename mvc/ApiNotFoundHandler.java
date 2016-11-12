package wbs.api.mvc;

import static wbs.utils.string.StringUtils.stringFormat;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
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
			@NonNull TaskLogger parentTaskLogger)
		throws
			ServletException,
			IOException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handleNotFound");

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
					requestContext.pathInfo () != null
						? requestContext.pathInfo ()
						: "");

			exceptionLogger.logSimple (
				"console",
				path,
				"Not found",
				"The specified path was not found",
				Optional.absent (),
				GenericExceptionResolution.ignoreWithThirdPartyWarning);

		} catch (RuntimeException exception) {

			taskLogger.fatalFormat (
				"Error creating not found log: %s",
				exception.getMessage ());

		}

		// return an error

		requestContext.status (404);

		PrintWriter out =
			requestContext.writer ();

		out.print (
			stringFormat (
				"404 Not found\n"));


	}

}
