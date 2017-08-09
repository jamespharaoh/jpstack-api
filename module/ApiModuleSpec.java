package wbs.api.module;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "name")
@ToString (of = "name")
@DataClass ("api-module")
@PrototypeComponent ("apiModuleSpec")
public
class ApiModuleSpec
	implements ApiSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataChildren (
		direct = true)
	List <Object> builders =
		new ArrayList<> ();

}
