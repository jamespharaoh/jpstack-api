package wbs.api.mvc;

import java.util.Map;

import wbs.web.responder.Responder;

public
interface StringMapResponderFactory {

	Responder makeResponder (
		Map<String,?> map);

}
