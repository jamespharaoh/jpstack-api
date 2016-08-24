package wbs.api.mvc;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;

import com.google.common.base.Optional;

import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.web.RequestContext;
import wbs.framework.web.WebNotFoundHandler;

@Log4j
@SingletonComponent ("apiNotFoundHandler")
public
class ApiNotFoundHandler
	implements WebNotFoundHandler {

	// singleton dependencies

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	RequestContext requestContext;

	// implementation

	@Override
	public
	void handleNotFound ()
		throws
			ServletException,
			IOException {

		// log it normally

		log.error (
			"Path not found: " + requestContext.requestUri ());

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

			log.fatal (
				"Error creating not found log: " + exception.getMessage ());

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
