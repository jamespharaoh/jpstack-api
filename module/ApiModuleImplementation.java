package wbs.api.module;

import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import wbs.api.resource.ApiResource;
import wbs.api.resource.ApiVariable;
import wbs.api.resource.ApiResource.Method;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.web.PathHandler;
import wbs.framework.web.RequestHandler;
import wbs.framework.web.WebFile;

@Accessors (fluent = true)
@DataClass ("api-module")
@PrototypeComponent ("apiModuleImpl")
public
class ApiModuleImplementation
	implements ApiModule {

	// dependencies

	@Inject
	ApplicationContext applicationContext;

	// prototype dependencies

	@Inject
	Provider<ApiResource> apiResourceProvider;

	@Inject
	Provider<ApiVariable> apiVariableProvider;

	// properties

	@DataChildren
	@Getter @Setter
	Map<String,WebFile> files =
		new LinkedHashMap<String,WebFile> ();

	@DataChildren
	@Getter @Setter
	Map<String,PathHandler> paths =
		new LinkedHashMap<String,PathHandler> ();

	// state

	Set<String> terminalResourceNames =
		new HashSet<String> ();

	Map<Pair<String,Method>,RequestHandler> requestHandlers =
		new HashMap<Pair<String,Method>,RequestHandler> ();

	Map<String,String> variableResources =
		new HashMap<String,String> ();

	// lifecycle

	@PostConstruct
	public
	void init () {

		// create files from terminal resource names

		List<String> terminalResourceNamesList =
			new ArrayList<String> (
				terminalResourceNames);

		Collections.sort (
			terminalResourceNamesList);

		for (
			String terminalResourceName :
				terminalResourceNamesList
		) {

			ApiResource resource =
				apiResourceProvider.get ();

			for (
				Method method
					: Method.values ()
			) {

				Pair<String,Method> key =
					Pair.of (
						terminalResourceName,
						method);

				RequestHandler requestHandler =
					requestHandlers.get (
						key);

				if (requestHandler == null)
					continue;

				resource.requestHandlers ().put (
					method,
					requestHandler);

			}

			files.put (
				terminalResourceName,
				resource);

		}

		// create path handlers from variable resources

		List<String> variableResourceNamesList =
			new ArrayList<String> (
				variableResources.keySet ());

		Collections.sort (
			variableResourceNamesList);

		for (
			String variableResourceName :
				variableResourceNamesList
		) {

			String variableName =
				variableResources.get (
					variableResourceName);

			ApiVariable variable =
				apiVariableProvider.get ()

				.resourceName (
					variableResourceName)

				.variableName (
					variableName);

			paths.put (
				variableResourceName,
				variable);

		}

	}

	// implementation

	public
	void addRequestHandler (
			String resourceName,
			Method method,
			RequestHandler requestHandler) {

		Pair<String,Method> key =
			Pair.of (
				resourceName,
				method);

		if (requestHandlers.containsKey (key)) {

			throw new RuntimeException (
				stringFormat (
					"Method %s ",
					method,
					"implemented twice for resource %s",
					resourceName));

		}

		terminalResourceNames.add (
			resourceName);

		requestHandlers.put (
			key,
			requestHandler);

	}

	public
	void addVariable (
			String resourceName,
			String variableName) {

		if (
			variableResources.containsKey (
				resourceName)
		) {

			String existingVariableName =
				variableResources.get (
					resourceName);

			if (
				notEqual (
					variableName,
					existingVariableName)
			) {

				throw new RuntimeException ();

			}

		} else {

			variableResources.put (
				resourceName,
				variableName);

		}

	}

}
