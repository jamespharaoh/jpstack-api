package wbs.api.module;

import java.util.Map;

import javax.inject.Provider;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
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
	@ApiModuleData
	Map <Class <?>, Provider <ApiModuleSpec>> apiModuleSpecProviders;

	@PrototypeDependency
	Provider <DataFromXmlBuilder> dataFromXmlBuilderProvider;

	// state

	DataFromXml dataFromXml;

	// lifecycle

	@NormalLifecycleSetup
	public
	void init () {

		dataFromXml =
			dataFromXmlBuilderProvider.get ()

			.registerBuilders (
				apiModuleSpecProviders)

			.build ();

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
