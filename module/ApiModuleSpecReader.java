package wbs.api.module;

import java.util.Map;

import javax.inject.Provider;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@SingletonComponent ("apiModuleSpecReader")
public
class ApiModuleSpecReader {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Map <Class <?>, ComponentProvider <ApiModuleSpec>> apiModuleSpecProviders;

	@PrototypeDependency
	Provider <DataFromXmlBuilder> dataFromXmlBuilderProvider;

	// state

	DataFromXml dataFromXml;

	// lifecycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			dataFromXml =
				dataFromXmlBuilderProvider.get ()

				.registerBuilders (
					taskLogger,
					apiModuleSpecProviders)

				.build (
					taskLogger)

			;

		}

	}

	// implementation

	public
	ApiModuleSpec readClasspath (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String xmlResourceName) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"readClasspath");

		) {

			ApiModuleSpec apiModuleSpec =
				(ApiModuleSpec)
				dataFromXml.readClasspathRequired (
					taskLogger,
					xmlResourceName);

			return apiModuleSpec;

		}

	}

}
