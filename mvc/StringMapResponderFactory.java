package wbs.api.mvc;

import java.util.Map;

import wbs.web.responder.WebResponder;

public
interface StringMapResponderFactory {

	WebResponder makeResponder (
		Map<String,?> map);

}
