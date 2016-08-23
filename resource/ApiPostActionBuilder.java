package wbs.api.resource;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.joinWithoutSeparator;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.api.module.ApiModuleBuilderHandler;
import wbs.api.module.ApiModuleImplementation;
import wbs.api.module.SimpleApiBuilderContainer;
import wbs.api.resource.ApiResource.Method;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.web.ActionRequestHandler;
import wbs.framework.web.RequestHandler;

@PrototypeComponent ("apiPostActionBuilder")
@ApiModuleBuilderHandler
public
class ApiPostActionBuilder {

	// dependencies

	@Inject
	ApplicationContext applicationContext;

	// prototype dependencies

	@Inject
	Provider<ActionRequestHandler> actionRequestHandlerProvider;

	// builder

	@BuilderParent
	SimpleApiBuilderContainer container;

	@BuilderSource
	ApiPostActionSpec spec;

	@BuilderTarget
	ApiModuleImplementation apiModule;

	// state

	String localName;
	String resourceName;
	String actionBeanName;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		setDefaults ();

		RequestHandler actionRequestHandler =
			actionRequestHandlerProvider.get ()
				.actionName (actionBeanName);

		apiModule.addRequestHandler (
			resourceName,
			Method.post,
			actionRequestHandler);

	}

	// implementation

	void setDefaults () {

		localName =
			ifNull (
				spec.name (),
				"post");

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
