package wbs.api.mvc;

import java.io.IOException;

import javax.inject.Provider;

import lombok.NonNull;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.web.context.RequestContext;
import wbs.web.responder.Responder;

public
class XmlResponder
	implements
		Provider <Responder>,
		Responder {

	@SingletonDependency
	RequestContext requestContext;

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
			@NonNull TaskLogger parentTaskLogger)
		throws IOException {

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

	}

	@Override
	public
	Responder get () {
		return this;
	}

}
