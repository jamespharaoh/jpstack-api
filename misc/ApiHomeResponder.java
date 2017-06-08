package wbs.api.misc;

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
import wbs.web.responder.AbstractResponder;

@PrototypeComponent ("apiHomeResponder")
public
class ApiHomeResponder
	extends AbstractResponder {

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
	void goHeaders (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goHeaders");

		) {

			requestContext.contentType (
				"text/plain",
				"utf-8");

		}

	}

	@Override
	protected
	void goContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goContent");

			FormatWriter formatWriter =
				requestContext.formatWriter ();

		) {

			formatWriter.writeLineFormat (
				"WBS platform API (%s)",
				wbsConfig.name ());

		}

	}

}
