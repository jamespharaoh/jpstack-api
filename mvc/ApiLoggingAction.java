package wbs.api.mvc;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.logging.DefaultLogContext;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;
import wbs.utils.string.WriterFormatWriter;

import wbs.web.action.Action;
import wbs.web.context.RequestContext;
import wbs.web.responder.Responder;

public abstract
class ApiLoggingAction
	implements Action {

	private final static
	LogContext logContext =
		DefaultLogContext.forClass (
			ApiLoggingAction.class);

	// dependencies

	@SingletonDependency
	ExceptionUtils exceptionUtils;

	@SingletonDependency
	protected
	RequestContext requestContext;

	// state

	private
	StringWriter debugWriter =
		new StringWriter ();

	private
	FormatWriter debugFormatWriter =
		new WriterFormatWriter (
			debugWriter);

	// hooks

	protected abstract
	void storeLog (
			TaskLogger parentTaskLogger,
			String debugLog);

	protected abstract
	void processRequest (
			TaskLogger parentTaskLogger,
			FormatWriter debugWriter);

	protected abstract
	void updateDatabase (
			TaskLogger parentTaskLogger);

	protected abstract
	Responder createResponse (
			TaskLogger parentTaskLogger,
			FormatWriter debugWriter);

	// implementation

	@Override
	public
	Responder handle (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handle");

		try {

			logRequest ();

			processRequest (
				taskLogger,
				debugFormatWriter);

			updateDatabase (
				taskLogger);

			return createResponse (
				taskLogger,
				debugFormatWriter);

		} catch (RuntimeException exception) {

			logFailure (
				exception);

			throw exception;

		} finally {

			storeLog (
				taskLogger,
				debugWriter.toString ());

		}

	}

	protected
	void logRequest () {

		// output

		debugFormatWriter.writeFormat (
			"%s %s\n",
			requestContext.method (),
			requestContext.requestUri ());

		// output headers

		for (
			Map.Entry <String, List <String>> headerEntry
				: requestContext.headerMap ().entrySet ()
		) {

			for (
				String headerValue
					: headerEntry.getValue ()
			) {

				debugFormatWriter.writeFormat (
					"%s = %s\n",
					headerEntry.getKey (),
					headerValue);

			}

		}

		debugFormatWriter.writeFormat (
			"\n");

		// output params

		for (
			Map.Entry <String, List <String>> parameterEntry
				: requestContext.parameterMap ().entrySet ()
		) {

			for (
				String parameterValue
					: parameterEntry.getValue ()
			) {

				debugFormatWriter.writeFormat (
					"%s = %s\n",
					parameterEntry.getKey (),
					parameterValue);

			}

		}

		debugFormatWriter.writeFormat (
			"\n");

	}

	protected
	void logFailure (
			@NonNull Throwable exception) {

		debugFormatWriter.writeFormat (
			"*** THREW EXCEPTION ***\n",
			"\n");

		debugFormatWriter.writeFormat (
			"%s\n",
			exceptionUtils.throwableDump (
				exception));

	}

}
