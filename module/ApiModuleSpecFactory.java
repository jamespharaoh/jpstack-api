package wbs.api.module;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.context.InitializedComponentFactory;

@Accessors (fluent = true)
public
class ApiModuleSpecFactory
	implements InitializedComponentFactory {

	// dependencies

	@Inject
	ApiModuleSpecReader apiSpecReader;

	// properties

	@Getter @Setter
	String xmlResourceName;

	// implementation

	@Override
	public
	Object makeComponent () {

		try {

			return apiSpecReader.readClasspath (
				xmlResourceName);

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error reading api module spec %s",
					xmlResourceName),
				exception);

		}

	}

}
