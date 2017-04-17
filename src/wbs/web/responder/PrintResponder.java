package wbs.web.responder;

import static wbs.utils.string.FormatWriterUtils.setCurrentFormatWriter;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;
import wbs.utils.string.WriterFormatWriter;

public abstract
class PrintResponder
	extends AbstractResponder {

	// singleton components

	@ClassSingletonDependency
	LogContext logContext;

	// state

	protected
	FormatWriter formatWriter;

	// implementation

	@SuppressWarnings ("resource")
	@Override
	protected
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"setup");

		formatWriter =
			new WriterFormatWriter (
				requestContext.printWriter ())

			.indentString (
				"  ");

		setCurrentFormatWriter (
			formatWriter);

		super.setup (
			taskLogger);

	}

}
