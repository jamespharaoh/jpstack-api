package wbs.api.resource;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.api.module.ApiModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "name")
@ToString (of = "name")
@DataClass ("resource")
@PrototypeComponent ("apiResourceSpec")
@ApiModuleData
public
class ApiResourceSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String path;

	@DataChildren (
		direct = true)
	List<Object> builders =
		new ArrayList<Object> ();

}
