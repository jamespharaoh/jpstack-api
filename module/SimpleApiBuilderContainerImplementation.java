package wbs.api.module;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;

@Accessors (fluent = true)
@Data
@PrototypeComponent ("simpleApiBuilderContainerImplementation")
public
class SimpleApiBuilderContainerImplementation
	implements SimpleApiBuilderContainer {

	String newBeanNamePrefix;
	String existingBeanNamePrefix;

	String resourceName;

}
