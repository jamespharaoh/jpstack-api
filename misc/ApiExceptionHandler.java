package wbs.api.misc;

import static wbs.utils.etc.NumberUtils.fromJavaInteger;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.context.RequestContext;
import wbs.web.mvc.WebExceptionHandler;

@SingletonComponent ("exceptionHandler")
public
class ApiExceptionHandler
	implements WebExceptionHandler {

	// dependencies

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	ExceptionUtils exceptionLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// implementation

	@Override
	public
	void handleExceptionRetry (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long attempt,
			@NonNull Throwable throwable) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handleException");

		) {

			// log via task logger

			taskLogger.errorFormatException (
				throwable,
				"Error at %s",
				requestContext.requestUri ());

			// log via exception logger

			try {

				StringBuilder stringBuilder =
					new StringBuilder ();

				stringBuilder.append (
					exceptionLogic.throwableDump (
						taskLogger,
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
					taskLogger,
					"webapi",
					requestContext.requestUri (),
					exceptionLogic.throwableSummary (
						taskLogger,
						throwable),
					stringBuilder.toString (),
					optionalAbsent (),
						GenericExceptionResolution.tryAgainNow);

			} catch (RuntimeException exception) {

				taskLogger.fatalFormatException (
					exception,
					"Error creating exception log");

			}

			// set the error code

			requestContext.status (
				fromJavaInteger (
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR));

		}

	}

	@Override
	public
	void handleExceptionFinal (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long attempt,
			@NonNull Throwable throwable) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handleException");

		) {

			// log it via task logger

			taskLogger.errorFormatException (
				throwable,
				"Error at %s",
				requestContext.requestUri ());

			// log it via exception logger

			try {

				StringBuilder stringBuilder =
					new StringBuilder ();

				stringBuilder.append (
					exceptionLogic.throwableDump (
						taskLogger,
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
					taskLogger,
					"webapi",
					requestContext.requestUri (),
					exceptionLogic.throwableSummary (
						taskLogger,
						throwable),
					stringBuilder.toString (),
					optionalAbsent (),
						GenericExceptionResolution.ignoreWithThirdPartyWarning);

			} catch (RuntimeException exception) {

				taskLogger.fatalFormatException (
					exception,
					"Error creating exception log");

			}

			// set the error code

			requestContext.status (
				fromJavaInteger (
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR));

		}

	}

}
