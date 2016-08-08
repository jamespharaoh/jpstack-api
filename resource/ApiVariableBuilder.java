package wbs.api.resource;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.api.module.ApiModuleBuilderHandler;
import wbs.api.module.ApiModuleImplementation;
import wbs.api.module.SimpleApiBuilderContainer;
import wbs.api.module.SimpleApiBuilderContainerImplementation;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@PrototypeComponent ("apiVariableBuilder")
@ApiModuleBuilderHandler
public
class ApiVariableBuilder {

	// prototype dependencies

	@Inject
	Provider<SimpleApiBuilderContainerImplementation>
	simpleApiBuilderContainerImplementation;

	// builder

	@BuilderParent
	SimpleApiBuilderContainer container;

	@BuilderSource
	ApiVariableSpec spec;

	@BuilderTarget
	ApiModuleImplementation apiModule;

	// state

	String resourceName;

	SimpleApiBuilderContainer childContainer;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		setDefaults ();
		initContainers ();

		apiModule.addVariable (
			container.resourceName (),
			spec.name ());

		builder.descend (
			childContainer,
			spec.builders (),
			apiModule,
			MissingBuilderBehaviour.error);

	}

	// implementation

	void setDefaults () {

		resourceName =
			stringFormat (
				"%s",
				container.resourceName (),
				"/{%s}",
				spec.name ());

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
