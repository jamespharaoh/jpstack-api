package wbs.api.mvc;

import static wbs.utils.collection.MapUtils.mapIsNotEmpty;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;
import wbs.utils.string.LazyFormatWriter;

import wbs.web.context.RequestContext;
import wbs.web.mvc.WebAction;
import wbs.web.responder.WebResponder;

@Accessors (fluent = true)
public final
class ApiLoggingActionWrapper
	implements WebAction {

	// singleton dependencies

	@SingletonDependency
	ExceptionUtils exceptionUtils;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <ApiErrorResponder> errorResponderProvider;

	// properties

	@Getter @Setter
	ComponentProvider <ApiLoggingAction> apiLoggingActionProvider;

	// state

	ApiLoggingAction apiLoggingAction;

	FormatWriter debugFormatWriter =
		new LazyFormatWriter ();

	// implementation

	@Override
	public
	Optional <WebResponder> defaultResponder (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"defaultResponder");

		) {

			return optionalOf (
				errorResponderProvider.provide (
					taskLogger));

		}

	}

	@Override
	public
	WebResponder handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handle");

		) {

			apiLoggingAction =
				apiLoggingActionProvider.provide (
					taskLogger);

			try {

				logRequest ();

				apiLoggingAction.processRequest (
					taskLogger,
					debugFormatWriter);

				apiLoggingAction.updateDatabase (
					taskLogger);

				return apiLoggingAction.createResponse (
					taskLogger,
					debugFormatWriter);

			} catch (RuntimeException exception) {

				logFailure (
					taskLogger,
					exception);

				throw exception;

			} finally {

				apiLoggingAction.storeLog (
					taskLogger,
					debugFormatWriter.toString ());

			}

		}

	}

	protected
	void logRequest () {

		// output headers

		debugFormatWriter.writeLineFormat (
			"===== REQUEST HEADERS =====");

		debugFormatWriter.writeNewline ();

		debugFormatWriter.writeLineFormat (
			"%s %s",
			requestContext.method (),
			requestContext.requestUri ());

		debugFormatWriter.writeNewline ();

		for (
			Map.Entry <String, List <String>> headerEntry
				: requestContext.headerMap ().entrySet ()
		) {

			for (
				String headerValue
					: headerEntry.getValue ()
			) {

				debugFormatWriter.writeLineFormat (
					"%s: %s",
					headerEntry.getKey (),
					headerValue);

			}

		}

		debugFormatWriter.writeNewline ();

		// output params

		if (
			mapIsNotEmpty (
				requestContext.parameterMap ())
		) {


			debugFormatWriter.writeLineFormat (
				"===== REQUEST PARAMETERS =====");

			debugFormatWriter.writeNewline ();


			for (
				Map.Entry <String, List <String>> parameterEntry
					: requestContext.parameterMap ().entrySet ()
			) {

				for (
					String parameterValue
						: parameterEntry.getValue ()
				) {

					debugFormatWriter.writeLineFormat (
						"%s = %s",
						parameterEntry.getKey (),
						parameterValue);

				}

			}

			debugFormatWriter.writeNewline ();

		}

	}

	protected
	void logFailure (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Throwable exception) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"logFailure");

		) {

			debugFormatWriter.writeLineFormat (
				"===== THREW EXCEPTION =====");

			debugFormatWriter.writeNewline ();

			debugFormatWriter.writeString (
				exceptionUtils.throwableDump (
					taskLogger,
					exception));

		}

	}

}
