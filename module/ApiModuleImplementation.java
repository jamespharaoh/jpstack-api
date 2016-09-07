package wbs.api.module;

import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringNotEqualSafe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Provider;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.api.resource.ApiResource;
import wbs.api.resource.ApiResource.Method;
import wbs.api.resource.ApiVariable;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.web.PathHandler;
import wbs.framework.web.RequestHandler;
import wbs.framework.web.WebFile;

@Accessors (fluent = true)
@DataClass ("api-module")
@PrototypeComponent ("apiModuleImplementation")
public
class ApiModuleImplementation
	implements ApiModule {

	// dependencies

	@SingletonDependency
	ComponentManager componentManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <ApiResource> apiResourceProvider;

	@PrototypeDependency
	Provider <ApiVariable> apiVariableProvider;

	// properties

	@DataChildren
	@Getter @Setter
	Map <String, WebFile> files =
		new LinkedHashMap<> ();

	@DataChildren
	@Getter @Setter
	Map <String, PathHandler> paths =
		new LinkedHashMap<> ();

	// state

	Set <String> terminalResourceNames =
		new HashSet<> ();

	Map <Pair <String, Method>, RequestHandler> requestHandlers =
		new HashMap<> ();

	Map <String, String> variableResources =
		new HashMap<> ();

	// lifecycle

	@PostConstruct
	public
	void init () {

		// create files from terminal resource names

		List <String> terminalResourceNamesList =
			new ArrayList<> (
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

				Pair <String, Method> key =
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

		List <String> variableResourceNamesList =
			new ArrayList<> (
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

		// freeze mutable properties

		files =
			ImmutableMap.copyOf (
				files);

		paths =
			ImmutableMap.copyOf (
				paths);

	}

	// implementation

	public
	void addRequestHandler (
			@NonNull String resourceName,
			@NonNull Method method,
			@NonNull RequestHandler requestHandler) {

		Pair <String, Method> key =
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
			@NonNull String resourceName,
			@NonNull String variableName) {

		if (
			variableResources.containsKey (
				resourceName)
		) {

			String existingVariableName =
				variableResources.get (
					resourceName);

			if (
				stringNotEqualSafe (
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
