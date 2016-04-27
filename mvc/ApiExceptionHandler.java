package wbs.api.mvc;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.web.RequestContext;
import wbs.framework.web.WebExceptionHandler;

@Log4j
@SingletonComponent ("exceptionHandler")
public
class ApiExceptionHandler
	implements WebExceptionHandler {

	// dependencies

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	ExceptionUtils exceptionLogic;

	@Inject
	RequestContext requestContext;

	// implementation

	@Override
	public
	void handleException (
			Throwable throwable)
		throws
			ServletException,
			IOException {

		// log it the old fashioned way

		log.error (
			stringFormat (
				"Error at %s",
				requestContext.requestUri ()),
			throwable);

		// make an exception log of this calamity

		try {

			StringBuilder stringBuilder =
				new StringBuilder ();

			stringBuilder.append (
				exceptionLogic.throwableDump (
					throwable));

			stringBuilder.append (
				"\n\nHTTP INFO\n\n");

			stringBuilder.append (
				stringFormat (
					"METHOD = %s\n\n",
					requestContext.method ()));

			for (
				Map.Entry<String,List<String>> entry
					: requestContext.parameterMap ().entrySet ()
			) {

				for (
					String value
						: entry.getValue ()
				) {

					stringBuilder.append (
						stringFormat (
							"%s = \"%s\"\n",
							entry.getKey (),
							value));

				}

			}

			exceptionLogger.logSimple (
				"webapi",
				requestContext.requestUri (),
				exceptionLogic.throwableSummary (
					throwable),
				stringBuilder.toString (),
				Optional.absent (),
				GenericExceptionResolution.ignoreWithThirdPartyWarning);

		} catch (RuntimeException exception) {

			log.fatal (
				"Error creating exception log",
				exception);

		}

		// set the error code

		requestContext.status (
			HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

	}

}
