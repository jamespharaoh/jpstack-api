package wbs.api.misc;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.config.WbsSpecialConfig;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("apiSpecialConfig")
public
class ApiSpecialConfig
	implements ComponentFactory <WbsSpecialConfig> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// components

	@Override
	public
	WbsSpecialConfig makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return new WbsSpecialConfig ()

				.assumeNegativeCache (
					false);

		}

	}

}
