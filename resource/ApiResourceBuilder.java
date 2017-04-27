package wbs.api.resource;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.joinWithSlash;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.api.module.ApiModuleBuilderHandler;
import wbs.api.module.ApiModuleImplementation;
import wbs.api.module.SimpleApiBuilderContainer;
import wbs.api.module.SimpleApiBuilderContainerImplementation;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("apiResourceBuilder")
@ApiModuleBuilderHandler
public
class ApiResourceBuilder
	implements BuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <SimpleApiBuilderContainerImplementation>
	simpleApiBuilderContainerImplementation;

	// builder

	@BuilderParent
	SimpleApiBuilderContainer container;

	@BuilderSource
	ApiResourceSpec spec;

	@BuilderTarget
	ApiModuleImplementation apiModule;

	// state

	String resourceName;

	SimpleApiBuilderContainer childContainer;

	// build

	@BuildMethod
	@Override
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder builder) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			setDefaults ();
			initContainers ();

			builder.descend (
				taskLogger,
				childContainer,
				spec.builders (),
				apiModule,
				MissingBuilderBehaviour.error);

		}

	}

	// implementation

	void setDefaults () {

		resourceName =
			joinWithSlash (
				container.resourceName (),
				ifNull (
					spec.path (),
					spec.name ()));

	}

	void initContainers () {

		childContainer =
			simpleApiBuilderContainerImplementation.get ()

			.newBeanNamePrefix (
				container.newBeanNamePrefix ())

			.existingBeanNamePrefix (
				container.existingBeanNamePrefix ())

			.resourceName (
				resourceName);

	}

}
