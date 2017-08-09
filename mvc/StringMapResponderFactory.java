package wbs.api.mvc;

import java.util.Map;

import wbs.framework.logging.TaskLogger;

import wbs.web.responder.WebResponder;

public
interface StringMapResponderFactory {

	WebResponder makeResponder (
			TaskLogger parentTaskLogger,
			Map <String, ?> map);

}
