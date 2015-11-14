package wbs.api.resource;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletException;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.DelegatingPathHandler;
import wbs.framework.web.PathHandler;
import wbs.framework.web.RequestContext;
import wbs.framework.web.WebFile;

@Accessors (fluent = true)
@PrototypeComponent ("apiVariable")
public
class ApiVariable
	implements PathHandler {

	// dependencies

	@Inject
	RequestContext requestContext;

	// indirect dependencies

	@Inject
	Provider<DelegatingPathHandler> delegatingPathHandlerProvider;

	// properties

	@Getter @Setter
	String resourceName;

	@Getter @Setter
	String variableName;

	// implementation

	@Override
	public
	WebFile processPath (
			String localPath)
		throws ServletException {

		// get value from path

		Matcher localPathMatcher =
			pathPattern.matcher (
				localPath);

		if (! localPathMatcher.matches ())
			throw new RuntimeException ();

		String variableValue =
			localPathMatcher.group (1);

		requestContext.request (
			variableName,
			variableValue);

		// continue processing request

		String localPathRest =
			localPathMatcher.group (2);

		String pathNext =
			stringFormat (
				"%s",
				resourceName,
				"/{%s}",
				variableName,
				"%s",
				localPathRest);

		DelegatingPathHandler delegatingPathHandler =
			delegatingPathHandlerProvider.get ();

		return delegatingPathHandler.processPath (
			pathNext);

	}

	static
	Pattern pathPattern =
		Pattern.compile (
			"^/([^/]+)(/.+)?$");

}
