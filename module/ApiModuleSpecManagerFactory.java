package wbs.api.module;

import static wbs.utils.etc.Misc.contains;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;
import static wbs.utils.string.StringUtils.stringReplaceAllSimple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.ComponentInterface;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.component.scaffold.PluginApiModuleSpec;
import wbs.framework.component.scaffold.PluginManager;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@SingletonComponent ("apiModuleSpecManager")
@ComponentInterface (ApiModuleSpecManager.class)
public
class ApiModuleSpecManagerFactory
	implements ComponentFactory <ApiModuleSpecManager> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	PluginManager pluginManager;

	// prototype dependencies

	@PrototypeDependency
	Map <Class <?>, ComponentProvider <ApiSpec>> apiSpecProviders;

	@PrototypeDependency
	ComponentProvider <DataFromXmlBuilder> dataFromXmlBuilderProvider;

	// state

	DataFromXml dataFromXml;

	List <ApiModuleSpec> specs =
		new ArrayList<> ();

	Map <String, ApiModuleSpec> specsByName =
		new HashMap<> ();

	// public implementation

	@Override
	public
	ApiModuleSpecManager makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			dataFromXml =
				dataFromXmlBuilderProvider.provide (
					taskLogger)

				.registerBuilders (
					taskLogger,
					apiSpecProviders)

				.build (
					taskLogger)

			;

			pluginManager.plugins ().forEach (
				pluginSpec ->
					loadSpecsForPlugin (
						taskLogger,
						pluginSpec));

			taskLogger.makeException ();

			Collections.sort (
				specs,
				Ordering.natural ().onResultOf (
					ApiModuleSpec::name));

			return new ApiModuleSpecManager ()

				.specs (
					ImmutableList.copyOf (
						specs))

				.specsByName (
					ImmutableMap.copyOf (
						specsByName))

			;

		}

	}

	// private implementation

	private
	void loadSpecsForPlugin (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull PluginSpec pluginSpec) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"loadSpecsForPlugin");

		) {

			pluginSpec.apiModules ().forEach (
				pluginApiModuleSpec ->
					loadSpec (
						parentTaskLogger,
						pluginSpec,
						pluginApiModuleSpec));

		}

	}

	private
	void loadSpec (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull PluginSpec pluginSpec,
			@NonNull PluginApiModuleSpec pluginApiModuleSpec) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"loadSpec");

		) {

			String apiModuleResourseName =
				stringFormat (
					"/%s/api/%s-api.xml",
					stringReplaceAllSimple (
						".",
						"/",
						pluginSpec.packageName ()),
					pluginApiModuleSpec.name ());

			Optional <ApiModuleSpec> apiModuleSpecOptional =
				loadFromResource (
					taskLogger,
					apiModuleResourseName);

			if (
				optionalIsNotPresent (
					apiModuleSpecOptional)
			) {
				return;
			}

			ApiModuleSpec apiModuleSpec =
				optionalGetRequired (
					apiModuleSpecOptional);

			if (
				stringNotEqualSafe (
					pluginApiModuleSpec.name (),
					apiModuleSpec.name ())
			) {

				taskLogger.errorFormat (
					"API module %s ",
					apiModuleResourseName,
					"has name %s ",
					apiModuleSpec.name (),
					"but expected %s",
					pluginApiModuleSpec.name ());

				return;

			}

			if (
				contains (
					specsByName,
					apiModuleSpec.name ())
			) {

				taskLogger.errorFormat (
					"Duplicated API module name: %s",
					apiModuleSpec.name ());

				return;

			}

			specs.add (
				apiModuleSpec);

			specsByName.put (
				apiModuleSpec.name (),
				apiModuleSpec);

		}

	}

	private
	Optional <ApiModuleSpec> loadFromResource (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String xmlResourceName) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"readClasspath");

		) {

			return genericCastUnchecked (
				dataFromXml.readClasspath (
					taskLogger,
					xmlResourceName));

		}

	}

}
