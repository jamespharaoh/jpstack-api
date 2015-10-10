package wbs.api.mvc;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;

public
class XmlResponder
	implements
		Provider<Responder>,
		Responder {

	@Inject
	RequestContext requestContext;

	private final
	Document document;

	private final
	int status;

	public
	XmlResponder (
			Document newDoc,
			int newStatus) {

		document = newDoc;

		status = newStatus;

	}

	@Override
	public
	void execute ()
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
			document,
			requestContext.outputStream ());

	}

	@Override
	public
	Responder get () {
		return this;
	}

}
