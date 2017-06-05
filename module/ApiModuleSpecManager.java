package wbs.api.module;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class ApiModuleSpecManager {

	List <ApiModuleSpec> specs;

	Map <String, ApiModuleSpec> specsByName;

}
