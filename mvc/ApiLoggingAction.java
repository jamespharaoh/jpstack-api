package wbs.api.mvc;

import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;

import wbs.web.responder.WebResponder;

public
interface ApiLoggingAction {

	void storeLog (
			TaskLogger parentTaskLogger,
			String debugLog);

	void processRequest (
			TaskLogger parentTaskLogger,
			FormatWriter debugWriter);

	void updateDatabase (
			TaskLogger parentTaskLogger);

	WebResponder createResponse (
			TaskLogger parentTaskLogger,
			FormatWriter debugWriter);

}
