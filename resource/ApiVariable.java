package wbs.api.resource;

import static wbs.utils.etc.Misc.requiredValue;
import static wbs.utils.string.StringUtils.emptyStringIfNull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Provider;
import javax.servlet.ServletException;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
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

	@SingletonDependency
	RequestContext requestContext;

	// indirect dependencies

	@SingletonDependency
	Provider <DelegatingPathHandler> delegatingPathHandlerProvider;

	// properties

	@Getter @Setter
	String resourceName;

	@Getter @Setter
	String variableName;

	// implementation

	@Override
	public
	WebFile processPath (
			@NonNull String localPath)
		throws ServletException {

		// get value from path

		Matcher localPathMatcher =
			pathPattern.matcher (
				localPath);

		if (! localPathMatcher.matches ())
			throw new RuntimeException ();

		String variableValue =
			requiredValue (
				localPathMatcher.group (1));

		requestContext.request (
			variableName,
			variableValue);

		// continue processing request

		String localPathRest =
			requiredValue (
				localPathMatcher.group (2));

		String pathNext =
			stringFormat (
				"%s",
				resourceName,
				"/{%s}",
				variableName,
				"%s",
				emptyStringIfNull (
					localPathRest));

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
