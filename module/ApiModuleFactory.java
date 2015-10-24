package wbs.api.module;

import static wbs.framework.utils.etc.Misc.hyphenToCamel;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.context.BeanFactory;
import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;

@Accessors (fluent = true)
public
class ApiModuleFactory
	implements BeanFactory {

	// dependencies

	@Inject
	@Named
	Builder apiModuleBuilder;

	// prototype dependencies

	@Inject
	Provider<ApiModuleImplementation> apiModuleImplementationProvider;

	// properties

	@Getter @Setter
	ApiModuleSpec apiModuleSpec;

	// implementation

	@Override
	public
	Object instantiate () {

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
