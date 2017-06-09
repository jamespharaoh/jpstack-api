package wbs.api.misc;

import static wbs.utils.etc.Misc.doNothing;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

import wbs.web.context.RequestContext;
import wbs.web.responder.BufferedTextResponder;

@PrototypeComponent ("apiHomeResponder")
public
class ApiHomeResponder
	extends BufferedTextResponder {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	WbsConfig wbsConfig;

	// implementation

	@Override
	protected
	void prepare (
			@NonNull Transaction parentTransaction) {

		doNothing ();

	}

	@Override
	protected
	void headers (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"headers");

		) {

			requestContext.contentType (
				"text/plain",
				"utf-8");

		}

	}

	@Override
	protected
	void render (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"render");

		) {

			formatWriter.writeLineFormat (
				"WBS platform API (%s)",
				wbsConfig.name ());

		}

	}

}
