package wbs.api.resource;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.api.module.ApiModuleBuilderHandler;
import wbs.api.module.ApiModuleImplementation;
import wbs.api.module.SimpleApiBuilderContainer;
import wbs.api.resource.ApiResource.Method;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.mvc.WebActionRequestHandler;
import wbs.web.mvc.WebRequestHandler;

@PrototypeComponent ("apiGetActionBuilder")
@ApiModuleBuilderHandler
public
class ApiGetActionBuilder
	implements BuilderComponent {

	// dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@StrongPrototypeDependency
	Provider <WebActionRequestHandler> actionRequestHandlerProvider;

	// builder

	@BuilderParent
	SimpleApiBuilderContainer container;

	@BuilderSource
	ApiGetActionSpec spec;

	@BuilderTarget
	ApiModuleImplementation apiModule;

	// state

	String localName;
	String resourceName;
	String actionBeanName;

	// build

	@BuildMethod
	@Override
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder <TaskLogger> builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			setDefaults ();

			WebRequestHandler actionRequestHandler =
				actionRequestHandlerProvider.get ()

				.actionName (
					taskLogger,
					actionBeanName);

			apiModule.addRequestHandler (
				resourceName,
				Method.get,
				actionRequestHandler);

		}

	}

	// implementation

	void setDefaults () {

		localName =
			ifNull (
				spec.name (),
				"get");

		resourceName =
			container.resourceName ();

		actionBeanName =
			joinWithoutSeparator (
				container.existingBeanNamePrefix (),
				capitalise (
					localName),
				"Action");

	}

}
