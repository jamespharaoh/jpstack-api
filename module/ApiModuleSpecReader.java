package wbs.api.module;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.data.tools.DataFromXml;

import com.google.common.collect.ImmutableList;

@Accessors (fluent = true)
@SingletonComponent ("apiModuleSpecReader")
public
class ApiModuleSpecReader {

	// collection dependencies

	@Inject
	@ApiModuleData
	Map<Class<?>,Provider<ApiModuleSpec>> apiModuleSpecProviders;

	// state

	DataFromXml dataFromXml;

	// lifecycle

	@PostConstruct
	public
	void init () {

		dataFromXml =
			new DataFromXml ();

		for (
			Map.Entry<Class<?>,Provider<ApiModuleSpec>> entry
				: apiModuleSpecProviders.entrySet ()
		) {

			dataFromXml.registerBuilder (
				entry.getKey (),
				entry.getValue ());

		}

	}

	// implementation

	public
	ApiModuleSpec readClasspath (
			@NonNull String xmlResourceName) {

		ApiModuleSpec apiModuleSpec =
			(ApiModuleSpec)
			dataFromXml.readClasspath (
				ImmutableList.of (),
				xmlResourceName);

		return apiModuleSpec;

	}

}
