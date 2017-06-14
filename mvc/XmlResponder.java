package wbs.api.mvc;

import static wbs.utils.etc.Misc.doNothing;

import java.io.IOException;
import java.io.OutputStream;

import lombok.NonNull;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.io.RuntimeIoException;

import wbs.web.context.RequestContext;
import wbs.web.responder.BufferedResponder;

@PrototypeComponent ("xmlResponder")
public
class XmlResponder
	extends BufferedResponder {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// state

	private final
	Document xmlDocument;

	private final
	Long status;

	public
	XmlResponder (
			@NonNull Document xmlDocument,
			@NonNull Long status) {

		this.xmlDocument =
			xmlDocument;

		this.status =
			status;

	}

	@Override
	protected
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			doNothing ();

		}

	}

	@Override
	protected
	void render (
			@NonNull Transaction parentTransaction,
			@NonNull OutputStream outputStream) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"render");

		) {

			XMLOutputter xmlOutputter =
				new XMLOutputter (
					Format.getPrettyFormat ());

			xmlOutputter.output (
				xmlDocument,
				outputStream);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

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

			requestContext.status (
				status);

			requestContext.contentType (
				"text/xml",
				"utf-8");

		}

	}

}
