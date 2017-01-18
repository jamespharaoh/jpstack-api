package wbs.api.mvc;

import java.io.IOException;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;

import wbs.web.context.RequestContext;
import wbs.web.responder.Responder;

@PrototypeComponent ("prototype")
public
class ApiErrorResponder
	implements Responder {

	// dependencies

	@SingletonDependency
	RequestContext requestContext;

	// implementation

	@Override
	public
	void execute (
			@NonNull TaskLogger parentTaskLogger)
		throws IOException {

		requestContext.sendError (
			500l,
			"Internal error");

		FormatWriter formatWriter =
			requestContext.formatWriter ();

		formatWriter.writeLineFormat (
			"ERROR");

		formatWriter.writeLineFormat (
			"Internal error");

	}

}
