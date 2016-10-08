package wbs.api.module;

import static wbs.utils.string.StringUtils.stringFormat;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.TaskLogger;

@Log4j
@Accessors (fluent = true)
public
class ApiModuleSpecFactory
	implements ComponentFactory {

	// dependencies

	@SingletonDependency
	ApiModuleSpecReader apiSpecReader;

	// properties

	@Getter @Setter
	String xmlResourceName;

	// implementation

	@Override
	public
	Object makeComponent (
			@NonNull TaskLogger taskLogger) {

		taskLogger =
			taskLogger.nest (
				this,
				"makeComponent",
				log);

		try {

			return apiSpecReader.readClasspath (
				xmlResourceName);

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error reading api module spec %s",
					xmlResourceName),
				exception);

		}

	}

}
