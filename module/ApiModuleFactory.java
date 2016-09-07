package wbs.api.module;

import static wbs.framework.utils.etc.StringUtils.hyphenToCamel;

import javax.inject.Named;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.component.tools.ComponentFactory;

@Accessors (fluent = true)
public
class ApiModuleFactory
	implements ComponentFactory {

	// dependencies

	@SingletonDependency
	@Named
	Builder apiModuleBuilder;

	// unitialized dependencies

	@UninitializedDependency
	Provider <ApiModuleImplementation> apiModuleImplementationProvider;

	// properties

	@Getter @Setter
	ApiModuleSpec apiModuleSpec;

	// implementation

	@Override
	public
	Object makeComponent () {

		ApiModuleImplementation apiModule =
			apiModuleImplementationProvider.get ();

		apiModuleBuilder.descend (
			simpleContainerSpec,
			apiModuleSpec.builders (),
			apiModule,
			MissingBuilderBehaviour.error);

		return apiModule;

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
