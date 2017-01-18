package wbs.api.resource;

import static wbs.utils.etc.NumberUtils.fromJavaInteger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.web.context.RequestContext;
import wbs.web.file.WebFile;
import wbs.web.handler.RequestHandler;

@Accessors (fluent = true)
@PrototypeComponent ("apiResource")
public
class ApiResource
	implements WebFile {

	// dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// properties

	@Getter @Setter
	Map <Method, RequestHandler> requestHandlers =
		new HashMap<> ();

	// implementation

	public
	void handleGeneric (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Method method)
		throws
			ServletException,
			IOException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handleGeneric");

		RequestHandler requestHandler =
			requestHandlers.get (
				method);

		if (requestHandler != null) {

			requestHandler.handle (
				taskLogger);

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
			fromJavaInteger (
				HttpServletResponse.SC_METHOD_NOT_ALLOWED),
			requestContext.requestUri ());

	}

	@Override
	public
	void doGet (
			@NonNull TaskLogger parentTaskLogger)
		throws
			ServletException,
			IOException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"doGet");

		handleGeneric (
			taskLogger,
			Method.get);

	}

	@Override
	public
	void doPost (
			@NonNull TaskLogger parentTaskLogger)
		throws
			ServletException,
			IOException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"doPost");

		handleGeneric (
			taskLogger,
			Method.post);

	}

	@Override
	public
	void doOptions (
			@NonNull TaskLogger taskLogger)
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
