package wbs.api.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderFactory;

@SingletonComponent ("apiModuleBuilder")
public
class ApiModuleBuilder
	implements Builder {

	// prototype dependencies

	@Inject
	Provider<BuilderFactory> builderFactoryProvider;

	// collection dependencies

	@Inject
	@ApiModuleBuilderHandler
	Map<Class<?>,Provider<Object>> apiModuleBuilders;

	// state

	Builder builder;

	// init

	@PostConstruct
	public
	void init () {

		BuilderFactory builderFactory =
			builderFactoryProvider.get ();

		for (
			Map.Entry<Class<?>,Provider<Object>> entry
				: apiModuleBuilders.entrySet ()
		) {

			builderFactory.addBuilder (
				entry.getKey (),
				entry.getValue ());

		}

		builder =
			builderFactory.create ();

	}

	// implementation

	@Override
	public
	void descend (
			Object parentObject,
			List<?> childObjects,
			Object targetObject) {

		List<Object> firstPass =
			new ArrayList<Object> ();

		for (
			Object childObject
				: childObjects
		) {

			firstPass.add (
				childObject);

		}

		builder.descend (
			parentObject,
			firstPass,
			targetObject);

	}

}
