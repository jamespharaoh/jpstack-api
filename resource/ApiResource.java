package wbs.api.resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.RequestContext;
import wbs.framework.web.RequestHandler;
import wbs.framework.web.WebFile;

@Accessors (fluent = true)
@PrototypeComponent ("apiResource")
public
class ApiResource
	implements WebFile {

	// dependencies

	@Inject
	RequestContext requestContext;

	// properties

	@Getter @Setter
	Map<Method,RequestHandler> requestHandlers =
		new HashMap<Method,RequestHandler> ();

	// implementation

	public
	void handleGeneric (
			@NonNull Method method)
		throws
			ServletException,
			IOException {

		RequestHandler requestHandler =
			requestHandlers.get (
				method);

		if (requestHandler != null) {
			requestHandler.handle ();
		} else {
			handleMethodNotAllowed ();
		}

	}

	public
	void handleMethodNotAllowed ()
		throws
			ServletException,
			IOException {

		requestContext.sendError (
			HttpServletResponse.SC_METHOD_NOT_ALLOWED,
			requestContext.requestUri ());

	}

	@Override
	public
	void doGet ()
		throws
			ServletException,
			IOException {

		handleGeneric (
			Method.get);

	}

	@Override
	public
	void doPost ()
		throws
			ServletException,
			IOException {

		handleGeneric (
			Method.post);

	}

	@Override
	public
	void doOptions ()
		throws
			ServletException,
			IOException {

	}

	// data types

	public static
	enum Method {
		get,
		post;
	}

}
