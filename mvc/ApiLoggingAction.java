package wbs.api.mvc;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import lombok.NonNull;

import wbs.framework.exception.ExceptionUtils;
import wbs.framework.web.Action;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;

public abstract
class ApiLoggingAction
	implements Action {

	// dependencies

	@Inject
	ExceptionUtils exceptionUtils;

	@Inject
	protected
	RequestContext requestContext;

	// state

	private
	StringBuilder debugLog =
		new StringBuilder ();

	// hooks

	protected abstract
	void storeLog (
			String debugLog);

	protected abstract
	void processRequest ();

	protected abstract
	void updateDatabase ();

	protected abstract
	Responder createResponse ();

	// implementation

	@Override
	public
	Responder handle () {

		try {

			logRequest ();

			processRequest ();

			updateDatabase ();

			return createResponse ();

		} catch (RuntimeException exception) {

			logFailure (
				exception);

			throw exception;

		} finally {

			storeLog (
				debugLog.toString ());

		}

	}

	protected
	void logRequest () {

		// output

		debugLog.append (
			stringFormat (
				"%s %s\n",
				requestContext.method (),
				requestContext.requestUri ()));

		// output headers

		for (
			Map.Entry<String,List<String>> headerEntry
				: requestContext.headerMap ().entrySet ()
		) {

			for (
				String headerValue
					: headerEntry.getValue ()
			) {

				debugLog.append (
					stringFormat (
						"%s = %s\n",
						headerEntry.getKey (),
						headerValue));

			}

		}

		debugLog.append (
			stringFormat (
				"\n"));

		// output params

		for (
			Map.Entry<String,List<String>> parameterEntry
				: requestContext.parameterMap ().entrySet ()
		) {

			for (
				String parameterValue
					: parameterEntry.getValue ()
			) {

				debugLog.append (
					stringFormat (
						"%s = %s\n",
						parameterEntry.getKey (),
						parameterValue));

			}

		}

		debugLog.append (
			stringFormat (
				"\n"));

	}

	protected
	void logFailure (
			@NonNull Throwable exception) {

		debugLog.append (
			stringFormat (
				"*** THREW EXCEPTION ***\n",
				"\n"));

		debugLog.append (
			stringFormat (
				"%s\n",
				exceptionUtils.throwableDump (
					exception)));

	}

}
