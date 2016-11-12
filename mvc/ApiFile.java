package wbs.api.mvc;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Provider;
import javax.servlet.ServletException;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.logging.TaskLogger;
import wbs.web.action.Action;
import wbs.web.action.ActionRequestHandler;
import wbs.web.file.AbstractFile;
import wbs.web.file.WebFile;
import wbs.web.handler.RequestHandler;
import wbs.web.responder.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("apiFile")
public
class ApiFile
	extends AbstractFile {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	WebApiManager webApiManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <ActionRequestHandler> actionRequestHandlerProvider;

	// properties

	@Getter @Setter
	RequestHandler getHandler;

	@Getter @Setter
	RequestHandler postHandler;

	@Getter @Setter
	Map <String, Object> requestParams =
		new LinkedHashMap<> ();

	// utilities

	public
	ApiFile getAction (
			Action action) {

		return getHandler (
			actionRequestHandlerProvider.get ()
				.action (action));

	}

	public
	ApiFile getActionName (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String actionName) {

		return getHandler (
			actionRequestHandlerProvider.get ()

			.actionName (
				parentTaskLogger,
				actionName));

	}

	public
	ApiFile getResponder (
			final Provider<Responder> responderProvider) {

		RequestHandler requestHandler =
			new RequestHandler () {

			@Override
			public
			void handle (
					@NonNull TaskLogger taskLogger)
				throws
					ServletException,
					IOException {

				Responder responder =
					responderProvider.get ();

				responder.execute (
					taskLogger);

			}

		};

		return getHandler (
			requestHandler);

	}

	public
	ApiFile getResponderName (
			String beanName) {

		return getHandler (

			new RequestHandler () {

				@Override
				public
				void handle (
						@NonNull TaskLogger taskLogger)
					throws
						ServletException,
						IOException {

					Responder responder =
						componentManager.getComponentRequired (
							taskLogger,
							beanName,
							Responder.class);

					responder.execute (
						taskLogger);

				}

			});

	}

	public
	ApiFile postAction (
			Action action) {

		return postHandler (
			actionRequestHandlerProvider.get ()
				.action (action));

	}

	public
	ApiFile postActionName (
			@NonNull String beanName) {

		return postAction (
			new Action () {

			@Override
			public
			Responder handle (
					@NonNull TaskLogger taskLogger) {

				Action action =
					componentManager.getComponentRequired (
						taskLogger,
						beanName,
						Action.class);

				return action.handle (
					taskLogger);

			}

		});

	}

	public
	WebFile postApiAction (
			WebApiAction webApiAction) {

		return postHandler (
			webApiManager.makeWebApiActionRequestHandler (
				webApiAction));

	}

}
