package wbs.api.misc;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.HiddenComponent;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.pathhandler.DelegatingPathHandler;

@SingletonComponent ("rootPathHandler")
@HiddenComponent
public
class ApiPathHandler
	implements ComponentFactory <DelegatingPathHandler> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// unitialized dependencies

	@UninitializedDependency
	Provider <DelegatingPathHandler> delegatingPathHandlerProvider;

	// components

	@Override
	public
	DelegatingPathHandler makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return delegatingPathHandlerProvider.get ();

		}

	}

}
