package wbs.api.mvc;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.file.AbstractFile;
import wbs.web.mvc.WebAction;
import wbs.web.mvc.WebActionRequestHandler;
import wbs.web.mvc.WebRequestHandler;
import wbs.web.mvc.WebResponderRequestHandler;
import wbs.web.responder.WebResponder;

@Accessors (fluent = true)
@PrototypeComponent ("apiFile")
public
class ApiFile
	extends AbstractFile {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@StrongPrototypeDependency
	Provider <WebActionRequestHandler> actionRequestHandlerProvider;

	@StrongPrototypeDependency
	Provider <WebResponderRequestHandler> responderRequestHandlerProvider;

	// properties

	@Getter @Setter
	Provider <WebRequestHandler> headHandlerProvider;

	@Getter @Setter
	Provider <WebRequestHandler> getHandlerProvider;

	@Getter @Setter
	Provider <WebRequestHandler> postHandlerProvider;

	@Getter @Setter
	Map <String, Object> requestParams =
		new LinkedHashMap<> ();

	// utilities

	public
	ApiFile getActionProvider (
			Provider <WebAction> actionProvider) {

		return getHandlerProvider (
			() -> actionRequestHandlerProvider.get ()

			.actionProvider (
				actionProvider)

		);

	}

	public
	ApiFile getActionName (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String actionName) {

		return getHandlerProvider (
			() -> actionRequestHandlerProvider.get ()

			.actionName (
				parentTaskLogger,
				actionName));

	}

	public
	ApiFile getResponderProvider (
			@NonNull Provider <? extends WebResponder> responderProvider) {

		return getHandlerProvider (
			() -> responderRequestHandlerProvider.get ()

			.responderProvider (
				responderProvider)

		);

	}

	public
	ApiFile getResponderName (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String beanName) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"getResponderName");

		) {

			return getHandlerProvider (
				() -> responderRequestHandlerProvider.get ()

				.responderProvider (
					componentManager.getComponentProviderRequired (
						parentTaskLogger,
						beanName,
						WebResponder.class))

			);

		}

	}

	public
	ApiFile postActionProvider (
			Provider <WebAction> actionProvider) {

		return postHandlerProvider (
			() -> actionRequestHandlerProvider.get ()

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
				WebAction.class));


	}

}
