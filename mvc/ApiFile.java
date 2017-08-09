package wbs.api.mvc;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.component.manager.ComponentProvider;
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
	ComponentProvider <WebActionRequestHandler> actionRequestHandlerProvider;

	@StrongPrototypeDependency
	ComponentProvider <WebResponderRequestHandler>
		responderRequestHandlerProvider;

	// properties

	@Getter @Setter
	ComponentProvider <WebRequestHandler> headHandlerProvider;

	@Getter @Setter
	ComponentProvider <WebRequestHandler> getHandlerProvider;

	@Getter @Setter
	ComponentProvider <WebRequestHandler> postHandlerProvider;

	@Getter @Setter
	Map <String, Object> requestParams =
		new LinkedHashMap<> ();

	// utilities

	public
	ApiFile getActionProvider (
			ComponentProvider <WebAction> actionProvider) {

		return getHandlerProvider (
			taskLoggerNested ->
				actionRequestHandlerProvider.provide (
					taskLoggerNested)

			.actionProvider (
				actionProvider)

		);

	}

	public
	ApiFile getActionName (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String actionName) {

		return getActionProvider (
			componentManager.getComponentProviderRequired (
				parentTaskLogger,
				actionName,
				WebAction.class));

	}

	public
	ApiFile getResponderProvider (
			@NonNull ComponentProvider <? extends WebResponder>
				responderProvider) {

		return getHandlerProvider (
			taskLoggerNested ->
				responderRequestHandlerProvider.provide (
					taskLoggerNested)

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

			ComponentProvider <WebResponder> responderProvider =
				componentManager.getComponentProviderRequired (
					parentTaskLogger,
					beanName,
					WebResponder.class);

			return getResponderProvider (
				responderProvider);

		}

	}

	public
	ApiFile postActionProvider (
			ComponentProvider <WebAction> actionProvider) {

		return postHandlerProvider (
			taskLoggerNested ->
				actionRequestHandlerProvider.provide (
					taskLoggerNested)

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
