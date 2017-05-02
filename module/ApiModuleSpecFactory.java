package wbs.api.module;

import static wbs.utils.string.StringUtils.stringFormat;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
public
class ApiModuleSpecFactory
	implements ComponentFactory {

	// dependencies

	@SingletonDependency
	ApiModuleSpecReader apiSpecReader;

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	String xmlResourceName;

	// implementation

	@Override
	public
	Object makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return apiSpecReader.readClasspath (
				taskLogger,
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
