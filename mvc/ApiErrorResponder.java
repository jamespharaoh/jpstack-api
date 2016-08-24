package wbs.api.mvc;

import java.io.IOException;
import java.io.PrintWriter;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;

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
	void execute ()
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
