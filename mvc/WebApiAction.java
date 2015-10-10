package wbs.api.mvc;

import java.io.IOException;

import javax.inject.Provider;

import wbs.framework.web.Responder;

public
interface WebApiAction {

	Responder go ()
		throws IOException;

	Provider<Responder> makeFallbackResponder ();

}
