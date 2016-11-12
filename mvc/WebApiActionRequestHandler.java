package wbs.api.mvc;

import java.io.IOException;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.web.context.RequestContext;
import wbs.web.handler.RequestHandler;
import wbs.web.responder.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("webApiActionRequestHandler")
public
class WebApiActionRequestHandler
	implements RequestHandler {

	// singleton dependencies

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// properties

	@Getter @Setter
	WebApiAction action;

	// public implementation

	@Override
	public
	void handle (
			@NonNull TaskLogger parentTaskLogger)
		throws IOException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handle");

		try {

			Responder responder =
				action.go (
					taskLogger);

			if (responder != null) {

				responder.execute (
					parentTaskLogger);

				return;

			}

		} catch (Exception exception) {

			exceptionLogger.logThrowable (
				"webapi",
				requestContext.requestUri (),
				exception,
				Optional.absent (),
				GenericExceptionResolution.ignoreWithThirdPartyWarning);

		}

		Responder responder =
			action.makeFallbackResponder ()
				.get ();

		responder.execute (
			taskLogger);

	}

}