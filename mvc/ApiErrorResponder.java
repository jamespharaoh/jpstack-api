package wbs.api.mvc;

import java.io.IOException;
import java.io.Writer;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.utils.io.RuntimeIoException;
import wbs.utils.string.FormatWriter;
import wbs.utils.string.WriterFormatWriter;

import wbs.web.context.RequestContext;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("prototype")
public
class ApiErrorResponder
	implements WebResponder {

	// dependencies

	@SingletonDependency
	RequestContext requestContext;

	// implementation

	@Override
	public
	void execute (
			@NonNull TaskLogger parentTaskLogger) {

		requestContext.sendError (
			500l,
			"Internal error");

		try (

			Writer writer =
				requestContext.writer ();

			FormatWriter formatWriter =
				new WriterFormatWriter (
					writer);

		) {

			formatWriter.writeLineFormat (
				"ERROR");

			formatWriter.writeLineFormat (
				"Internal error");

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

}
