package wbs.api.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderFactory;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;

@SingletonComponent ("apiModuleBuilder")
public
class ApiModuleBuilder
	implements Builder {

	// prototype dependencies

	@PrototypeDependency
	Provider <BuilderFactory> builderFactoryProvider;

	// collection dependencies

	@PrototypeDependency
	@ApiModuleBuilderHandler
	Map <Class <?>, Provider <Object>> apiModuleBuilders;

	// state

	Builder builder;

	// init

	@NormalLifecycleSetup
	public
	void init () {

		BuilderFactory builderFactory =
			builderFactoryProvider.get ();

		for (
			Map.Entry <Class <?>, Provider <Object>> entry
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
			@NonNull Object parentObject,
			@NonNull List <?> childObjects,
			@NonNull Object targetObject,
			@NonNull MissingBuilderBehaviour missingBuilderBehaviour) {

		List <Object> firstPass =
			new ArrayList<> ();

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
			targetObject,
			missingBuilderBehaviour);

	}

}
