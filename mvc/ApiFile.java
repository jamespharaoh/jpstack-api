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
	ApiFile getActionProvider (
			Provider <Action> actionProvider) {

		return getHandler (
			actionRequestHandlerProvider.get ()

			.actionProvider (
				actionProvider)

		);

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
	ApiFile getResponderProvider (
			@NonNull Provider <Responder> responderProvider) {

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
	ApiFile postActionProvider (
			Provider <Action> actionProvider) {

		return postHandler (
			actionRequestHandlerProvider.get ()

			.actionProvider (
				actionProvider)

		);

	}

	public
	ApiFile postActionName (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String beanName) {

		return postActionProvider (
			componentManager.getComponentProviderRequired (
				parentTaskLogger,
				beanName,
				Action.class));


	}

	public
	WebFile postApiAction (
			WebApiAction webApiAction) {

		return postHandler (
			webApiManager.makeWebApiActionRequestHandler (
				webApiAction));

	}

}
