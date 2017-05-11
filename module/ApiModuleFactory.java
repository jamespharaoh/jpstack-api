package wbs.api.module;

import static wbs.utils.string.StringUtils.hyphenToCamel;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
public
class ApiModuleFactory
	implements ComponentFactory <ApiModuleImplementation> {

	// dependencies

	@SingletonDependency
	@NamedDependency
	Builder <TaskLogger> apiModuleBuilder;

	@ClassSingletonDependency
	LogContext logContext;

	// unitialized dependencies

	@UninitializedDependency
	Provider <ApiModuleImplementation> apiModuleImplementationProvider;

	// properties

	@Getter @Setter
	ApiModuleSpec apiModuleSpec;

	// implementation

	@Override
	public
	ApiModuleImplementation makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			ApiModuleImplementation apiModule =
				apiModuleImplementationProvider.get ();

			apiModuleBuilder.descend (
				taskLogger,
				simpleContainerSpec,
				apiModuleSpec.builders (),
				apiModule,
				MissingBuilderBehaviour.error);

			return apiModule;

		}

	}

	// simple container

	public
	SimpleApiBuilderContainer simpleContainerSpec =
		new SimpleApiBuilderContainer () {

		@Override
		public
		String newBeanNamePrefix () {

			return hyphenToCamel (
				apiModuleSpec.name ());

		}

		@Override
		public
		String existingBeanNamePrefix () {

			return hyphenToCamel (
				apiModuleSpec.name ());

		}

		@Override
		public
		String resourceName () {
			return "";
		}

	};

}
