package wbs.api.mvc;

import java.io.IOException;

import javax.inject.Provider;

import wbs.framework.logging.TaskLogger;
import wbs.web.responder.Responder;

public
interface WebApiAction {

	Responder go (
			TaskLogger taskLogger)
		throws IOException;

	Provider <Responder> makeFallbackResponder ();

}
