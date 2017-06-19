package wbs.api.mvc;

import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrElseRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.mvc.WebAction;
import wbs.web.responder.WebResponder;

@Accessors (fluent = true)
@PrototypeComponent ("apiActionWrapper")
public
class ApiActionWrapper
	implements WebAction {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <ApiErrorResponder> errorResponder;

	// properties

	@Getter @Setter
	ComponentProvider <ApiAction> apiActionProvider;

	// state

	ApiAction apiAction;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			apiAction =
				apiActionProvider.provide (
					taskLogger);

		}

	}

	// public implementation

	@Override
	public
	Optional <WebResponder> defaultResponder (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"defaultResponder");

		) {

			return optionalOf (
				optionalOrElseRequired (
					apiAction.defaultResponder (
						taskLogger),
					() -> errorResponder.provide (
						taskLogger)));

		}

	}

	@Override
	public
	WebResponder handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handle");

		) {

			return optionalOrNull (
				apiAction.handle (
					taskLogger));

		}

	}

}
