package wbs.api.misc;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.thread.ThreadManager;
import wbs.utils.thread.ThreadManagerImplementation;

@SingletonComponent ("apiThreadManager")
public
class ApiThreadManager
	implements ComponentFactory <ThreadManager> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// uninitialized dependencies

	@UninitializedDependency
	ComponentProvider <ThreadManagerImplementation>
		threadManagerImplementationProvider;

	// components

	@Override
	public
	ThreadManager makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return threadManagerImplementationProvider.provide (
				taskLogger)

				.exceptionTypeCode (
					"webapi")

			;

		}

	}

}
