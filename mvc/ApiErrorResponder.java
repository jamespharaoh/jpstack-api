package wbs.api.mvc;

import java.io.IOException;
import java.io.PrintWriter;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;
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
			500,
			"Internal error");

		PrintWriter out =
			requestContext.writer ();

		out.println (
			"ERROR\nInternal error\n");

	}

}
