package wbs.api.mvc;

import java.io.IOException;

import javax.inject.Provider;

import lombok.NonNull;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.io.RuntimeIoException;

import wbs.web.context.RequestContext;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("xmlResponder")
public
class XmlResponder
	implements
		Provider <WebResponder>,
		WebResponder {

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
	public
	void execute (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"execute");

		) {

			requestContext.status (
				status);

			requestContext.contentType (
				"text/xml",
				"utf-8");

			XMLOutputter xmlOutputter =
				new XMLOutputter (
					Format.getPrettyFormat ());

			xmlOutputter.output (
				xmlDocument,
				requestContext.outputStream ());

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	@Override
	public
	WebResponder get () {
		return this;
	}

}
