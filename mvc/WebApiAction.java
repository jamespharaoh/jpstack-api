package wbs.api.mvc;

import javax.inject.Provider;

import wbs.framework.logging.TaskLogger;

import wbs.web.responder.Responder;

public
interface WebApiAction {

	Responder go (
			TaskLogger taskLogger);

	Provider <Responder> makeFallbackResponder ();

}
