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
import wbs.framework.logging.TaskLogger;

import wbs.utils.io.RuntimeIoException;

import wbs.web.context.RequestContext;
import wbs.web.responder.Responder;

@PrototypeComponent ("xmlResponder")
public
class XmlResponder
	implements
		Provider <Responder>,
		Responder {

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

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"execute");

		) {

			requestContext.status (
				status);

			requestContext.setHeader (
				"Content-Type",
				"text/xml");

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
	Responder get () {
		return this;
	}

}
