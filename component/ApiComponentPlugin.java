package wbs.api.component;

import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.classEqualSafe;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Set;

import javax.inject.Provider;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.api.module.ApiModule;
import wbs.api.module.ApiModuleFactory;
import wbs.api.module.ApiModuleSpec;
import wbs.api.module.ApiModuleSpecManager;
import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.component.registry.ComponentRegistryBuilder;
import wbs.framework.component.scaffold.PluginApiModuleSpec;
import wbs.framework.component.scaffold.PluginManager;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.component.tools.ComponentPlugin;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("apiComponentPlugin")
public
class ApiComponentPlugin
	implements ComponentPlugin {

	// singleton dependencies

	@SingletonDependency
	ApiModuleSpecManager apiModuleSpecManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	PluginManager pluginManager;

	// public implementation

	@Override
	public
	void registerComponents (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginSpec plugin) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerComponents");

		) {

			plugin.apiModules ().forEach (
				apiModuleSpec ->
					registerApiModule (
						taskLogger,
						componentRegistry,
						apiModuleSpec));

		}

	}

	// private implementation

	private
	void registerApiModule (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginApiModuleSpec pluginApiModuleSpec) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerApiModule");

		) {

			// get module spec

			ApiModuleSpec apiModuleSpec =
				mapItemForKeyRequired (
					apiModuleSpecManager.specsByName (),
					pluginApiModuleSpec.name ());

			// register api module

			componentRegistry.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					stringFormat (
						"%sApiModule",
						hyphenToCamel (
							apiModuleSpec.name ())))

				.componentClass (
					ApiModule.class)

				.factoryClass (
					genericCastUnchecked (
						ApiModuleFactory.class))

				.scope (
					"singleton")

				.addValueProperty (
					"apiModuleSpec",
					optionalOf (
						apiModuleSpec))

			);

		}

	}

}
