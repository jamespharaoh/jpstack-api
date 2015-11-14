package wbs.api.mvc;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletException;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.web.AbstractFile;
import wbs.framework.web.Action;
import wbs.framework.web.ActionRequestHandler;
import wbs.framework.web.RequestHandler;
import wbs.framework.web.Responder;
import wbs.framework.web.WebFile;

@Accessors (fluent = true)
@PrototypeComponent ("apiFile")
public
class ApiFile
	extends AbstractFile {

	// dependencies

	@Inject
	ApplicationContext applicationContext;

	@Inject
	WebApiManager webApiManager;

	// prototype dependencies

	@Inject
	Provider<ActionRequestHandler> actionRequestHandlerProvider;

	// properties

	@Getter @Setter
	RequestHandler getHandler;

	@Getter @Setter
	RequestHandler postHandler;

	@Getter @Setter
	Map<String,Object> requestParams =
		new LinkedHashMap<String,Object> ();

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
			String actionName) {

		return getHandler (
			actionRequestHandlerProvider.get ()
				.actionName (actionName));

	}

	public
	ApiFile getResponder (
			final Provider<Responder> responderProvider) {

		RequestHandler requestHandler =
			new RequestHandler () {

			@Override
			public
			void handle ()
				throws
					ServletException,
					IOException {

				Responder responder =
					responderProvider.get ();

				responder.execute ();

			}

		};

		return getHandler (
			requestHandler);

	}

	public
	ApiFile getResponderName (
			final String beanName) {

		return getHandler (

			new RequestHandler () {

				@Override
				public
				void handle ()
					throws
						ServletException,
						IOException {

					Responder responder = (Responder)
						applicationContext.getBean (
							beanName,
							Responder.class);

					responder.execute ();

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
			final String beanName) {

		return postAction (
			new Action () {

			@Override
			public
			Responder handle () {

				Action action =
					applicationContext.getBean (
						beanName,
						Action.class);

				return action.handle ();

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
