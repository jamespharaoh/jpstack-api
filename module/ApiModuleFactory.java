package wbs.api.module;

import static wbs.utils.string.StringUtils.hyphenToCamel;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
public
class ApiModuleFactory
	implements ComponentFactory <ApiModuleImplementation> {

	// singleton dependencies

	@SingletonDependency
	@NamedDependency
	Builder <TaskLogger> apiModuleBuilder;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@StrongPrototypeDependency
	ComponentProvider <ApiModuleImplementation> apiModuleProvider;

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

			return apiModuleProvider.provide (
				taskLogger,
				apiModule -> {

				apiModuleBuilder.descend (
					taskLogger,
					simpleContainerSpec,
					apiModuleSpec.builders (),
					apiModule,
					MissingBuilderBehaviour.error);

			});

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
