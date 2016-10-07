package wbs.api.mvc;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.web.Action;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.utils.string.FormatWriter;
import wbs.utils.string.WriterFormatWriter;

public abstract
class ApiLoggingAction
	implements Action {

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
			String debugLog);

	protected abstract
	void processRequest (
			FormatWriter debugWriter);

	protected abstract
	void updateDatabase ();

	protected abstract
	Responder createResponse (
			FormatWriter debugWriter);

	// implementation

	@Override
	public
	Responder handle () {

		try {

			logRequest ();

			processRequest (
				debugFormatWriter);

			updateDatabase ();

			return createResponse (
				debugFormatWriter);

		} catch (RuntimeException exception) {

			logFailure (
				exception);

			throw exception;

		} finally {

			storeLog (
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
