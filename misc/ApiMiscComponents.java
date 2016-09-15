package wbs.api.misc;

import javax.inject.Named;
import javax.inject.Provider;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.web.DelegatingPathHandler;
import wbs.framework.web.PathHandler;
import wbs.utils.thread.ThreadManager;
import wbs.utils.thread.ThreadManagerImplementation;

@SingletonComponent ("apiMiscComponents")
public
class ApiMiscComponents {

	// unitialized dependencies

	@UninitializedDependency
	Provider <DelegatingPathHandler> delegatingPathHandlerProvider;

	@UninitializedDependency
	Provider <ThreadManagerImplementation> threadManagerImplementationProvider;

	// components

	@SingletonComponent ("threadManager")
	public
	ThreadManager threadManager () {

		return threadManagerImplementationProvider.get ()

			.exceptionTypeCode (
				"api");

	}

	@SingletonComponent ("rootPathHandler")
	@Named
	public
	PathHandler rootPathHandler () {

		return delegatingPathHandlerProvider.get ();

	}

}
