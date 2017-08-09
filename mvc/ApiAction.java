package wbs.api.mvc;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.logging.TaskLogger;

import wbs.web.responder.WebResponder;

public
interface ApiAction {

	default
	Optional <WebResponder> defaultResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return optionalAbsent ();

	}

	Optional <WebResponder> handle (
			TaskLogger parentTaskLogger);

}
